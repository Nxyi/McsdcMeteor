package com.mcsdc.addon.system;

import org.jetbrains.annotations.Nullable;

public record ServerStorage(String ip, String version, @Nullable Long lastScanned, @Nullable Long lastSeen) {

}
