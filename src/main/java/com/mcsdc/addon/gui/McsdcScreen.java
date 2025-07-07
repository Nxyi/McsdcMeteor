package com.mcsdc.addon.gui;

import com.mcsdc.addon.MultiplayerScreenUtils;
import com.mcsdc.addon.system.McsdcSystem;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

import java.util.concurrent.CompletableFuture;


public class McsdcScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;

    public McsdcScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Mcsdc");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        String authToken = McsdcSystem.get().getToken();

        if (authToken.isEmpty()) {
            this.client.setScreen(new LoginScreen(this));
            return;
        }

        WTable noticeTable = add(theme.table()).expandX().widget();
        CompletableFuture.supplyAsync(() -> {
            return Http.post("https://interact.mcsdc.online/notice.txt").sendString();
        }).thenAccept(notice -> {
            noticeTable.add(theme.label(notice == null ? "No notice" : notice));
        }); // fix freezing that can happen when opening screen, no longer waits for notice before adding other components

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
        WButton tickedIdScreenButton = widgetList.add(this.theme.button("TickedID search")).expandX().widget();


        newServersButton.action = () -> {
            this.client.setScreen(FindNewServersScreen.instance(this.multiplayerScreen, this));
        };

        findPlayersButton.action = () -> {
            this.client.setScreen(FindPlayerScreen.instance(this.multiplayerScreen, this));
        };

        recentServersButton.action = () -> {
            this.client.setScreen(new RecentServersScreen(this.multiplayerScreen));
        };

        removeServersButton.action = () -> {
            for (int i = 0; i < this.multiplayerScreen.getServerList().size(); i++) {
                if (this.multiplayerScreen.getServerList().get(i).name.startsWith("Mcsdc")) {
                    this.multiplayerScreen.getServerList().remove(this.multiplayerScreen.getServerList().get(i));
                    i--;
                }
            }

            MultiplayerScreenUtils.save(this.multiplayerScreen);
            MultiplayerScreenUtils.reload(this.multiplayerScreen);
        };

        tickedIdScreenButton.action = () -> {
            this.client.setScreen(TicketIDScreen.instance(this.multiplayerScreen, this));
        };

//        findPlayersButton.action = () -> {
//            if (this.client == null) return;
//            this.client.setScreen(new FindPlayerScreen(this.multiplayerScreen));
//        };
    }

    @Override
    public void close() {
        super.close();
        MultiplayerScreenUtils.reload(this.multiplayerScreen);
        this.client.setScreen(this.multiplayerScreen);
    }
}
