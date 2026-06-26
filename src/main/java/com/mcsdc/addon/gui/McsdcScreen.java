package com.mcsdc.addon.gui;

import com.mcsdc.addon.MultiplayerScreenUtils;
import com.mcsdc.addon.system.McsdcSystem;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

import java.util.concurrent.CompletableFuture;

import static meteordevelopment.meteorclient.MeteorClient.mc;


public class McsdcScreen extends WindowScreen {
    private final JoinMultiplayerScreen multiplayerScreen;

    public McsdcScreen(JoinMultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Mcsdc");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        String authToken = McsdcSystem.get().getToken();

        if (authToken.isEmpty()) {
            this.minecraft.setScreen(new LoginScreen(this));
            return;
        }

        WTable noticeTable = add(theme.table()).expandX().widget();
        CompletableFuture.supplyAsync(() -> {
            return Http.post("https://interact.mcsdc.online/notice.txt").sendString();
        }).thenAccept(notice -> {
            mc.execute(() -> noticeTable.add(theme.label(notice == null ? "No notice" : notice)));
        });

        WTable accountList = add(theme.table()).expandX().widget();
        accountList.row();
        accountList.add(theme.label("User: " + McsdcSystem.get().getUsername())).expandX();
        accountList.add(theme.label("Perms: " + McsdcSystem.get().getLevel())).expandX();

        WButton logoutButton = accountList.add(theme.button("Logout")).widget();
        logoutButton.action = () -> {
            McsdcSystem.get().setToken("");
            McsdcSystem.get().setUsername("");
            McsdcSystem.get().setLevel(-1);
            reload();
        };

        WHorizontalList widgetList = add(theme.horizontalList()).expandX().widget();
        WButton newServersButton = widgetList.add(this.theme.button("Find new servers")).expandX().widget();
        WButton findPlayersButton = widgetList.add(this.theme.button("Search players")).expandX().widget();
        WButton recentServersButton = widgetList.add(this.theme.button("Recent Servers")).expandX().widget();
        WButton removeServersButton = widgetList.add(this.theme.button("Remove Servers")).expandX().widget();
        WButton tickedIdScreenButton = widgetList.add(this.theme.button("TicketID search")).expandX().widget();


        newServersButton.action = () -> {
            this.minecraft.setScreen(FindNewServersScreen.instance(this.multiplayerScreen, this));
        };

        findPlayersButton.action = () -> {
            this.minecraft.setScreen(FindPlayerScreen.instance(this.multiplayerScreen, this));
        };

        recentServersButton.action = () -> {
            this.minecraft.setScreen(new RecentServersScreen(this.multiplayerScreen));
        };

        removeServersButton.action = () -> {
            for (int i = 0; i < this.multiplayerScreen.getServers().size(); i++) {
                if (this.multiplayerScreen.getServers().get(i).name.startsWith("Mcsdc")) {
                    this.multiplayerScreen.getServers().remove(this.multiplayerScreen.getServers().get(i));
                    i--;
                }
            }

            MultiplayerScreenUtils.save(this.multiplayerScreen);
            MultiplayerScreenUtils.reload(this.multiplayerScreen);
        };

        tickedIdScreenButton.action = () -> {
            this.minecraft.setScreen(TicketIDScreen.instance(this.multiplayerScreen, this));
        };
    }

    @Override
    public void onClose() {
        super.onClose();
        MultiplayerScreenUtils.reload(this.multiplayerScreen);
        this.minecraft.setScreen(this.multiplayerScreen);
    }
}
