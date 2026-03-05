package com.mcsdc.addon.gui;

import com.mcsdc.addon.MultiplayerScreenUtils;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerStorage;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

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
            McsdcSystem.get().clearRecentServers();
            reload();
        };

        table.row();

        table.add(theme.label("Server IP"));
        table.add(theme.label("Version"));
        table.row();
        table.add(theme.horizontalSeparator()).expandX();

        List<ServerStorage> reversed = McsdcSystem.get().getRecentServers().reversed();

        reversed.forEach((serverStorage) -> {
            String serverIP = serverStorage.ip();
            String serverVersion = serverStorage.version();

            table.row();
            table.add(theme.label(serverIP)).expandX();
            table.add(theme.label(serverVersion)).expandX();

            WButton addServerButton = theme.button("Add Server");
            addServerButton.action = () -> {
                ServerInfo info = new ServerInfo("Mcsdc " + serverIP, serverIP, ServerInfo.ServerType.OTHER);
                multiplayerScreen.getServerList().add(info, false);
                MultiplayerScreenUtils.save(this.multiplayerScreen);
                MultiplayerScreenUtils.reload(this.multiplayerScreen);
                addServerButton.visible = false;
            };

            WButton joinServerButton = theme.button("Join Server");
            joinServerButton.action = () -> {
                ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), mc,
                    ServerAddress.parse(serverIP), new ServerInfo("", serverIP, ServerInfo.ServerType.OTHER), false, null);
            };
            WButton serverInfoButton = theme.button("Server Info");
            serverInfoButton.action = () -> {
                mc.setScreen(new ServerInfoScreen(serverIP));
            };

            WButton removeServerButton = theme.button("Remove Server");
            removeServerButton.action = () -> {
                McsdcSystem.get().removeRecentServer(serverStorage);
                reload();
            };


            table.add(addServerButton);
            table.add(joinServerButton);
            table.add(serverInfoButton);
            table.add(removeServerButton);
        });

    }
}
