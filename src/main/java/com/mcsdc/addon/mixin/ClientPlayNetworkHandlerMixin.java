package com.mcsdc.addon.mixin;

import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerStorage;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void onGameJoinTail(ClientboundLoginPacket packet, CallbackInfo ci) {
        McsdcSystem system = McsdcSystem.get();
        ServerData info = MeteorClient.mc.getConnection().getServerData();
        if(info == null) return;

        system.addRecentServer(new ServerStorage(info.ip, info.version.getString(), null, null));
    }

}
