package com.mcsdc.addon;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public class ViaFabricPlusHelper {
    private static final boolean IS_VIAFABRICPLUS_LOADED = FabricLoader.getInstance().isModLoaded("viafabricplus");
    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean isViaFabricPlusLoaded() {
        return IS_VIAFABRICPLUS_LOADED;
    }

    public static boolean isProtocolSupported(int protocolVersion) {
        if (!IS_VIAFABRICPLUS_LOADED) {
            return false;
        }
        try {
            Class<?> protocolVersionListClass = Class.forName("com.viaversion.vialoader.util.ProtocolVersionList");
            Method getProtocolsNewToOldMethod = protocolVersionListClass.getMethod("getProtocolsNewToOld", new Class[0]);
            List<?> protocols = (List) getProtocolsNewToOldMethod.invoke(null, new Object[0]);
            for (Object protocol : protocols) {
                Method isSnapshotMethod = protocol.getClass().getMethod("isSnapshot", new Class[0]);
                Method getSnapshotVersionMethod = protocol.getClass().getMethod("getSnapshotVersion", new Class[0]);
                Method getFullSnapshotVersionMethod = protocol.getClass().getMethod("getFullSnapshotVersion", new Class[0]);
                Method getVersionMethod = protocol.getClass().getMethod("getVersion", new Class[0]);
                if (((Boolean) isSnapshotMethod.invoke(protocol, new Object[0])).booleanValue()) {
                    int snapshotVersion = ((Integer) getSnapshotVersionMethod.invoke(protocol, new Object[0])).intValue();
                    int fullSnapshotVersion = ((Integer) getFullSnapshotVersionMethod.invoke(protocol, new Object[0])).intValue();
                    if (snapshotVersion == protocolVersion || fullSnapshotVersion == protocolVersion) {
                        return true;
                    }
                } else {
                    int version = ((Integer) getVersionMethod.invoke(protocol, new Object[0])).intValue();
                    if (version == protocolVersion) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to check protocol version with ViaFabricPlus: " + e.getMessage());
            return false;
        }
    }

    public static void forceProtocolVersion(Object serverInfo, int serverProtocolVersion) {
        if (!IS_VIAFABRICPLUS_LOADED) {
            return;
        }
        try {
            Class<?> protocolVersionListClass = Class.forName("com.viaversion.vialoader.util.ProtocolVersionList");
            Method getProtocolsNewToOldMethod = protocolVersionListClass.getMethod("getProtocolsNewToOld", new Class[0]);
            List<?> protocols = (List) getProtocolsNewToOldMethod.invoke(null, new Object[0]);
            Optional<?> protocolVersion = protocols.stream().filter(x -> {
                try {
                    Method isSnapshotMethod = x.getClass().getMethod("isSnapshot", new Class[0]);
                    Method getSnapshotVersionMethod = x.getClass().getMethod("getSnapshotVersion", new Class[0]);
                    Method getFullSnapshotVersionMethod = x.getClass().getMethod("getFullSnapshotVersion", new Class[0]);
                    Method getVersionMethod = x.getClass().getMethod("getVersion", new Class[0]);
                    if (((Boolean) isSnapshotMethod.invoke(x, new Object[0])).booleanValue()) {
                        int snapshotVersion = ((Integer) getSnapshotVersionMethod.invoke(x, new Object[0])).intValue();
                        int fullSnapshotVersion = ((Integer) getFullSnapshotVersionMethod.invoke(x, new Object[0])).intValue();
                        return snapshotVersion == serverProtocolVersion || fullSnapshotVersion == serverProtocolVersion;
                    }
                    int version = ((Integer) getVersionMethod.invoke(x, new Object[0])).intValue();
                    return version == serverProtocolVersion;
                } catch (Exception e) {
                    LOGGER.error("Failed to check protocol version: {}", e.getMessage());
                    return false;
                }
            }).findFirst();
            if (protocolVersion.isPresent()) {
                Class<?> protocolVersionClass = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
                Method getNameMethod = protocolVersionClass.getMethod("getName", new Class[0]);
                Method getVersionMethod = protocolVersionClass.getMethod("getVersion", new Class[0]);
                String versionName = (String) getNameMethod.invoke(protocolVersion.get(), new Object[0]);
                int versionNumber = ((Integer) getVersionMethod.invoke(protocolVersion.get(), new Object[0])).intValue();
                Class<?> serverInfoClass = serverInfo.getClass();
                Method forceVersionMethod = serverInfoClass.getMethod("viaFabricPlus$forceVersion", protocolVersionClass);
                forceVersionMethod.invoke(serverInfo, protocolVersion.get());
                LOGGER.info("Setting version for server to {} ({})", versionName, Integer.valueOf(versionNumber));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to force protocol version: {}", e.getMessage());
        }
    }
}
