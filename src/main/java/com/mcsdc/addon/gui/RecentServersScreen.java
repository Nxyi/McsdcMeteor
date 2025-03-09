package com.mcsdc.addon.gui;

import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerEntry;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.Collections;
import java.util.List;

public class RecentServersScreen extends WindowScreen {
    MultiplayerScreen multiplayerScreen;

    public RecentServersScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Recent Servers");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        WTable table = add(theme.table()).expandX().widget();

        if (McsdcSystem.get().getRecentServers().isEmpty()){
            table.add(theme.label("Recently joined servers will appear here.")).expandX().widget();
            return;
        }

        table.add(theme.button("Clear")).expandX().widget().action = () -> {
            McsdcSystem.get().getRecentServers().clear();
            reload();
        };

        table.row();

        table.add(theme.label("Server IP"));
        table.add(theme.label("Version"));
        table.row();
        table.add(theme.horizontalSeparator()).expandX();

        // Reverse list so most recent shows at the top
        List<ServerEntry> reversed = McsdcSystem.get().getRecentServers();
        Collections.reverse(reversed);

        reversed.forEach((serverStorage) -> {
            String serverIP = serverStorage.ip;
            String serverVersion = serverStorage.version;

            table.row();
            table.add(theme.label(serverIP)).expandX();
            table.add(theme.label(serverVersion)).expandX();

            WButton addServerButton = theme.button("Add Server");
            addServerButton.action = () -> {
                ServerInfo info = new ServerInfo("Mcsdc " + serverIP, serverIP, ServerInfo.ServerType.OTHER);
                multiplayerScreen.getServerList().add(info, false);
                multiplayerScreen.getServerList().saveFile();
                multiplayerScreen.getServerList().loadFile();
                addServerButton.visible = false;
            };

            WButton joinServerButton = theme.button("Join Server");
            joinServerButton.action = () -> {
                ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(),
                    new ServerAddress(serverIP.split(":")[0], Integer.parseInt(serverIP.split(":")[1])),
                    new ServerInfo("a", serverIP, ServerInfo.ServerType.OTHER), false, null);

            };
            WButton serverInfoButton = theme.button("Server Info");
            serverInfoButton.action = () -> {
                MinecraftClient.getInstance().setScreen(new ServerInfoScreen(serverIP));
            };

            WButton removeServerButton = theme.button("Remove Server");
            removeServerButton.action = () -> {
                McsdcSystem.get().getRecentServers().remove(serverStorage);
                reload();
            };


            table.add(addServerButton);
            table.add(joinServerButton);
            table.add(serverInfoButton);
            table.add(removeServerButton);
        });

    }
}
