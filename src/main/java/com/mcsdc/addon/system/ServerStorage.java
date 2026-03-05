package com.mcsdc.addon.system;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record ServerStorage(String ip, String version, @Nullable Long lastScanned, @Nullable Long lastSeen) {

    public static List<ServerStorage> fromJsonArray(String jsonResponse) {
        List<ServerStorage> list = new ArrayList<>();
        JsonArray array = JsonParser.parseString(jsonResponse).getAsJsonArray();

        array.forEach(node -> {
            String address = node.getAsJsonObject().get("address").getAsString();
            String version = node.getAsJsonObject().get("version").getAsString();
            long lastScanned = node.getAsJsonObject().get("last_scanned").getAsLong();
            long lastSeen = node.getAsJsonObject().get("last_seen_online").getAsLong();
            list.add(new ServerStorage(address, version, lastScanned, lastSeen));
        });

        return list;
    }
}
