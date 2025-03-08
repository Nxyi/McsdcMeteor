package com.mcsdc.addon.mixin;

import com.mcsdc.addon.Main;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerEntry;
import com.mcsdc.addon.system.ServerStorage;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.viaversion.viafabricplus.injection.access.base.IServerInfo;
import com.viaversion.viafabricplus.screen.impl.ProtocolSelectionScreen;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import com.viaversion.vialoader.util.ProtocolVersionList;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

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

        if (FabricLoader.getInstance().isModLoaded("viafabricplus")) {
            ServerStorage serverStorage = Main.getServerStorage();
            server = serverStorage.getServerEntry(info.address);

            if(server != null) {
                Integer serverProtocolVersion = extractProtocolVersion(server.version);
                if (serverProtocolVersion != null) {
                    final Optional<ProtocolVersion> protocolVersion = ProtocolVersionList.getProtocolsNewToOld().stream().filter(x -> {
                        if (x.isSnapshot()) {
                            return (x.getSnapshotVersion() == serverProtocolVersion) || (x.getFullSnapshotVersion() == serverProtocolVersion);
                        } else {
                            return x.getVersion() == serverProtocolVersion;
                        }
                    }).findFirst();
                    protocolVersion.ifPresent(version -> {
                        ((IServerInfo) info).viaFabricPlus$forceVersion(version);
                        LOGGER.info("Setting version for server {} to {} ({})", info.address, version.getName(), version.getVersion());
                    });
                }
            }
        }

    }

    @Unique
    private static Integer extractProtocolVersion(String input) {
        Pattern pattern = Pattern.compile("\\((\\d+)\\)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
}
