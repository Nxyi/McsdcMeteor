package com.mcsdc.addon.mixin;

import com.mcsdc.addon.ViaFabricPlusHelper;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.net.UnknownHostException;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class ServerEntryMixin extends MultiplayerServerListWidget.Entry {
    @Shadow
    @Final
    @Mutable
    private MultiplayerScreen screen;

    @Shadow
    @Final
    @Mutable
    private MinecraftClient client;

    @Shadow
    @Final
    @Mutable
    private ServerInfo server;

    @Shadow
    private void update() {}

    @Shadow
    public void saveFile() {}


    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/ThreadPoolExecutor;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;"
        ),
        index = 0
    )
    private Runnable modifySubmitArgument(Runnable originalRunnable) {
        return () -> {
            try {
                this.screen.getServerListPinger().add(this.server, () -> this.client.execute(this::saveFile), () -> {
                    if (ViaFabricPlusHelper.isViaFabricPlusLoaded()) {
                        ViaFabricPlusHelper.forceProtocolVersion(this.server, this.server.protocolVersion);
                        if (ViaFabricPlusHelper.isProtocolSupported(this.server.protocolVersion)) {
                            // protocol version is supported
                            this.server.setStatus(ServerInfo.Status.SUCCESSFUL);
                        } else {
                            // protocol version is not supported
                            this.server.setStatus(ServerInfo.Status.INCOMPATIBLE);
                        }
                    } else {
                        // original code
                        this.server.setStatus(this.server.protocolVersion == SharedConstants.getGameVersion().getProtocolVersion() ? ServerInfo.Status.SUCCESSFUL : ServerInfo.Status.INCOMPATIBLE);
                    }
                    this.client.execute(this::update);
                });
            } catch (UnknownHostException var2) {
                this.server.setStatus(ServerInfo.Status.UNREACHABLE);
                this.server.label = MultiplayerServerListWidget.CANNOT_RESOLVE_TEXT;
                this.client.execute(this::update);
            } catch (Exception var3) {
                this.server.setStatus(ServerInfo.Status.UNREACHABLE);
                this.server.label = MultiplayerServerListWidget.CANNOT_CONNECT_TEXT;
                this.client.execute(this::update);
            }
        };
    }
}
