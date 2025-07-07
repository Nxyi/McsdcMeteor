package com.mcsdc.addon.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcsdc.addon.Main;
import com.mcsdc.addon.MultiplayerScreenUtils;
import com.mcsdc.addon.system.FindPlayerSearchBuilder;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerStorage;
import com.mcsdc.addon.util.TicketIDGenerator;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TicketIDScreen extends WindowScreen {
    private static TicketIDScreen instance = null;
    private MultiplayerScreen multiplayerScreen;
    private Screen parent;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<String> TicketIDString = sg.add(new StringSetting.Builder()
        .name("ID")
        .description("")
        .defaultValue("")
        .build()
    );

    public static TicketIDScreen instance(MultiplayerScreen multiplayerScreen, Screen parent) {
        if (instance == null) {
            instance = new TicketIDScreen();
        }
        instance.setMultiplayerScreen(multiplayerScreen);
        instance.setParent(parent);
        return instance;
    }

    public void setParent(Screen parent) {
        this.parent = parent;
    }

    public MultiplayerScreen setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        return this.multiplayerScreen = multiplayerScreen;
    }

    public TicketIDScreen() {
        super(GuiThemes.get(), "TickedID");
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
            if (TicketIDString.get().isEmpty()){
                add(theme.label("Enter a tickedID")).expandX();
                return;
            }



            try { // some error checking since it likes to crash out
                String IP = TicketIDGenerator.decodeTicketID(TicketIDString.get());
                if (!TicketIDGenerator.isValidIPv4WithPort(IP)){
                    add(theme.label("Invalid TicketID")).expandX();
                    return;
                }
                MinecraftClient.getInstance().setScreen(new ServerInfoScreen(IP));
            } catch (Exception e){
                add(theme.label("Error")).expandX();
            }
        };
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
