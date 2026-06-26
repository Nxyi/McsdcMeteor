package com.mcsdc.addon.gui;

import com.mcsdc.addon.Main;
import com.mcsdc.addon.MultiplayerScreenUtils;
import com.mcsdc.addon.system.FindPlayerSearchBuilder;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerStorage;
import com.google.gson.JsonObject;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FindPlayerScreen extends WindowScreen {
    private static FindPlayerScreen instance = null;
    private JoinMultiplayerScreen multiplayerScreen;
    private Screen parent;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<String> playerSetting = sg.add(new StringSetting.Builder()
        .name("name/uuid")
        .description("")
        .defaultValue("popbob")
        .build()
    );

    public static FindPlayerScreen instance(JoinMultiplayerScreen multiplayerScreen, Screen parent) {
        if (instance == null) {
            instance = new FindPlayerScreen();
        }
        instance.setMultiplayerScreen(multiplayerScreen);
        instance.setParent(parent);
        return instance;
    }

    public void setParent(Screen parent) {
        this.parent = parent;
    }

    public JoinMultiplayerScreen setMultiplayerScreen(JoinMultiplayerScreen multiplayerScreen) {
        return this.multiplayerScreen = multiplayerScreen;
    }

    public FindPlayerScreen() {
        super(GuiThemes.get(), "Find Player");
    }
    WContainer settingsContainer;
    @Override
    public void initWidgets() {

        WContainer settingsContainer = add(theme.verticalList()).expandX().widget();
        settingsContainer.minWidth = 300;
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("search")).expandX().widget().action = () -> {
            reload();
            if (playerSetting.get().isEmpty()){
                add(theme.label("Enter a name/uuid")).expandX();
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                JsonObject toSearch = FindPlayerSearchBuilder.create(playerSetting.get());
                return Http.post(Main.mainEndpoint).bodyJson(toSearch).header("authorization", "Bearer " + McsdcSystem.get().getToken()).sendStringResponse().body();
            }).thenAccept(response -> {
                mc.execute(() -> {
                    if (response == null) {
                        add(theme.label("No servers found."));
                        return;
                    }

                    List<ServerStorage> extractedServers;
                    try {
                        extractedServers = ServerStorage.fromJsonArray(response);
                    } catch (Exception e) {
                        add(theme.label("Error parsing response."));
                        return;
                    }

                    if (extractedServers.isEmpty()) {
                        add(theme.label("No servers found."));
                        return;
                    }

                    add(theme.button("add all")).expandX().widget().action = () -> {
                        extractedServers.forEach((server) -> {
                            ServerData info = new ServerData("Mcsdc " + server.ip(), server.ip(), ServerData.Type.OTHER);
                            multiplayerScreen.getServers().add(info, false);
                        });
                        MultiplayerScreenUtils.save(this.multiplayerScreen);
                        MultiplayerScreenUtils.reload(this.multiplayerScreen);
                    };

                    WTable table = add(theme.table()).widget();

                    table.add(theme.label("Server IP"));
                    table.add(theme.label("Version"));
                    table.row();
                    table.add(theme.horizontalSeparator()).expandX();
                    table.row();

                    extractedServers.forEach((server) -> {
                        String serverIP = server.ip();
                        String serverVersion = server.version();

                        table.add(theme.label(serverIP));
                        table.add(theme.label(serverVersion));

                        WButton addServerButton = theme.button("Add Server");
                        addServerButton.action = () -> {
                            ServerData info = new ServerData("Mcsdc " + serverIP, serverIP, ServerData.Type.OTHER);
                            multiplayerScreen.getServers().add(info, false);
                            MultiplayerScreenUtils.save(this.multiplayerScreen);
                            MultiplayerScreenUtils.reload(this.multiplayerScreen);
                            addServerButton.visible = false;
                        };

                        WButton joinServerButton = theme.button("Join Server");
                        joinServerButton.action = () ->
                            ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()), mc,
                                ServerAddress.parseString(serverIP), new ServerData("", serverIP, ServerData.Type.OTHER), false, null);


                        WButton serverInfoButton = theme.button("Server Info");
                        serverInfoButton.action = () -> {
                            mc.setScreen(new ServerInfoScreen(serverIP));
                        };

                        table.add(addServerButton);
                        table.add(joinServerButton);
                        table.add(serverInfoButton);
                        table.row();
                    });
                });
            });
        };
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
