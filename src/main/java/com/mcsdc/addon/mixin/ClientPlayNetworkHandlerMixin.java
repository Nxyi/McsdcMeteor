package com.mcsdc.addon.mixin;

import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerStorage;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo ci) {
        McsdcSystem system = McsdcSystem.get();
        ServerInfo info = MeteorClient.mc.getNetworkHandler().getServerInfo();
        ServerStorage server = system.getRecentServerWithIp(info.address);

        if (system.getRecentServers().contains(server)){
            system.getRecentServers().remove(server);
            system.getRecentServers().add(server);
            return;
        }

        system.getRecentServers().add(new ServerStorage(info.address, info.version.getString(), null, null));

    }

}
