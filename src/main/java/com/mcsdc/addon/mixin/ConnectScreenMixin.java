package com.mcsdc.addon.mixin;

import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Inject(method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;Lnet/minecraft/client/network/CookieStorage;)V", at = @At("HEAD"), cancellable = true)
    private void onConnect(MinecraftClient client, ServerAddress address, ServerInfo info, CookieStorage cookieStorage, CallbackInfo ci){
        McsdcSystem system = McsdcSystem.get();
        ServerEntry server = system.getRecentServerWithIp(info.address);

        if (system.getRecentServers().contains(server)){
            system.getRecentServers().remove(server);
            system.getRecentServers().add(server);
            return;
        }

        system.getRecentServers().add(new ServerEntry(info.address, info.version.getString()));
    }
}
