package com.mcsdc.addon.mixin;

import com.mcsdc.addon.system.McsdcSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {

    @Inject(method = "startConnecting", at = @At("HEAD"))
    private static void onConnect(Screen parent, Minecraft client, ServerAddress address, ServerData info, boolean quickPlay, TransferState transferState, CallbackInfo ci){
        McsdcSystem.get().setLastServer(info.ip);
    }

}
