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
            Method getProtocolsNewToOldMethod = protocolVersionListClass.getMethod("getProtocolsNewToOld");
            List<?> protocols = (List) getProtocolsNewToOldMethod.invoke(null, new Object[0]);
            for (Object protocol : protocols) {
                Method isSnapshotMethod = protocol.getClass().getMethod("isSnapshot");
                Method getSnapshotVersionMethod = protocol.getClass().getMethod("getSnapshotVersion");
                Method getFullSnapshotVersionMethod = protocol.getClass().getMethod("getFullSnapshotVersion");
                Method getVersionMethod = protocol.getClass().getMethod("getVersion");
                if ((Boolean) isSnapshotMethod.invoke(protocol, new Object[0])) {
                    int snapshotVersion = (Integer) getSnapshotVersionMethod.invoke(protocol, new Object[0]);
                    int fullSnapshotVersion = (Integer) getFullSnapshotVersionMethod.invoke(protocol, new Object[0]);
                    if (snapshotVersion == protocolVersion || fullSnapshotVersion == protocolVersion) {
                        return true;
                    }
                } else {
                    int version = (Integer) getVersionMethod.invoke(protocol, new Object[0]);
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
            Method getProtocolsNewToOldMethod = protocolVersionListClass.getMethod("getProtocolsNewToOld");
            List<?> protocols = (List) getProtocolsNewToOldMethod.invoke(null, new Object[0]);
            Optional<?> protocolVersion = protocols.stream().filter(x -> {
                try {
                    Method isSnapshotMethod = x.getClass().getMethod("isSnapshot");
                    Method getSnapshotVersionMethod = x.getClass().getMethod("getSnapshotVersion");
                    Method getFullSnapshotVersionMethod = x.getClass().getMethod("getFullSnapshotVersion");
                    Method getVersionMethod = x.getClass().getMethod("getVersion");
                    if (((Boolean) isSnapshotMethod.invoke(x, new Object[0]))) {
                        int snapshotVersion = ((Integer) getSnapshotVersionMethod.invoke(x, new Object[0]));
                        int fullSnapshotVersion = ((Integer) getFullSnapshotVersionMethod.invoke(x, new Object[0]));
                        return snapshotVersion == serverProtocolVersion || fullSnapshotVersion == serverProtocolVersion;
                    }
                    int version = ((Integer) getVersionMethod.invoke(x, new Object[0]));
                    return version == serverProtocolVersion;
                } catch (Exception e) {
                    LOGGER.error("Failed to check protocol version: {}", e.getMessage());
                    return false;
                }
            }).findFirst();
            if (protocolVersion.isPresent()) {
                Class<?> protocolVersionClass = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
                Method getNameMethod = protocolVersionClass.getMethod("getName");
                Method getVersionMethod = protocolVersionClass.getMethod("getVersion");
                String versionName = (String) getNameMethod.invoke(protocolVersion.get(), new Object[0]);
                int versionNumber = ((Integer) getVersionMethod.invoke(protocolVersion.get(), new Object[0]));
                Class<?> serverInfoClass = serverInfo.getClass();
                Method forceVersionMethod = serverInfoClass.getMethod("viaFabricPlus$forceVersion", protocolVersionClass);
                forceVersionMethod.invoke(serverInfo, protocolVersion.get());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to force protocol version: {}", e.getMessage());
        }
    }
}
