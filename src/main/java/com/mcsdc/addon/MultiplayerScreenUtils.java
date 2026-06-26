package com.mcsdc.addon;

import com.mcsdc.addon.mixin.MultiplayerScreenAccessor;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

public class MultiplayerScreenUtils {
    public static void save(JoinMultiplayerScreen multiplayerScreen) {
        multiplayerScreen.getServers().save();
    }

    public static void reload(JoinMultiplayerScreen multiplayerScreen){
        MultiplayerScreenAccessor msa = getAccessor(multiplayerScreen);
        msa.getServerListWidget().updateOnlineServers(multiplayerScreen.getServers());
    }


    public static MultiplayerScreenAccessor getAccessor(JoinMultiplayerScreen multiplayerScreen){
        return (MultiplayerScreenAccessor) multiplayerScreen;
    }
}
