package com.mcsdc.addon.system;

import com.mojang.datafixers.types.templates.CompoundList;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class McsdcSystem extends System<McsdcSystem> {
    public static final int MAX_RECENT_SERVERS = 100;

    private String token = "";
    private String username = "";
    private int level = -1;
    private String lastServer = "";
    private final List<ServerStorage> serverQueue = new ArrayList<>();
    private int currentServerIndex = 0;

    private final LinkedHashMap<String, ServerStorage> recentServers = new LinkedHashMap<>();

    public McsdcSystem() {
        super("McsdcSystem");
    }

    public static McsdcSystem get() {
        return Systems.get(McsdcSystem.class);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<ServerStorage> getRecentServers() {
        return new ArrayList<>(recentServers.values());
    }

    public ServerStorage getRecentServerWithIp(String ip) {
        return recentServers.get(ip);
    }

    public void addRecentServer(ServerStorage server) {
        recentServers.remove(server.ip());
        recentServers.put(server.ip(), server);

        while (recentServers.size() > MAX_RECENT_SERVERS) {
            String oldestKey = recentServers.keySet().iterator().next();
            recentServers.remove(oldestKey);
        }
    }

    public void removeRecentServer(ServerStorage server) {
        recentServers.remove(server.ip());
    }

    public void clearRecentServers() {
        recentServers.clear();
    }

    public String getLastServer() {
        return lastServer;
    }

    public void setLastServer(String lastServer) {
        this.lastServer = lastServer;
    }

    public void setServerQueue(List<ServerStorage> servers) {
        serverQueue.clear();
        serverQueue.addAll(servers);
        currentServerIndex = 0;
    }

    public ServerStorage getNextServer() {
        if (!hasServerQueue()) return null;

        while (currentServerIndex < serverQueue.size()) {
            ServerStorage server = serverQueue.get(currentServerIndex++);
            if (!server.ip().equals(lastServer)) return server;
        }

        clearServerQueue();
        return null;
    }

    public boolean hasServerQueue() {
        return !serverQueue.isEmpty() && currentServerIndex < serverQueue.size();
    }

    public void clearServerQueue() {
        serverQueue.clear();
        currentServerIndex = 0;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag compound = new CompoundTag();
        compound.putString("token", this.token);
        compound.putString("username", this.username);
        compound.putInt("level", this.level);

        ListTag list = new ListTag();

        recentServers.values().forEach((server) -> {
            CompoundTag compound2 = new CompoundTag();
            compound2.putString("ip", server.ip());
            compound2.putString("version", server.version());
            list.add(compound2);
        });

        compound.put("recent", list);

        ListTag queueList = new ListTag();
        serverQueue.forEach((server) -> {
            CompoundTag queueEntry = new CompoundTag();
            queueEntry.putString("ip", server.ip());
            queueEntry.putString("version", server.version());
            if (server.lastScanned() != null) queueEntry.putLong("lastScanned", server.lastScanned());
            if (server.lastSeen() != null) queueEntry.putLong("lastSeen", server.lastSeen());
            queueList.add(queueEntry);
        });
        compound.put("serverQueue", queueList);
        compound.putInt("currentServerIndex", this.currentServerIndex);

        return compound;
    }

    @Override
    public McsdcSystem fromTag(CompoundTag tag) {
        this.token = tag.getString("token").get();
        this.username = tag.getString("username").get();
        this.level = tag.getInt("level").get();

        ListTag list = tag.getList("recent").get();
        List<ServerStorage> tempList = new ArrayList<>();
        for (Tag element : list) {
            CompoundTag compound = (CompoundTag) element;
            String ip = compound.getString("ip").get();
            String ver = compound.getString("version").get();
            tempList.add(new ServerStorage(ip, ver, null, null));
        }

        for (int i = tempList.size() - 1; i >= 0; i--) {
            ServerStorage s = tempList.get(i);
            recentServers.put(s.ip(), s);
        }

        serverQueue.clear();
        if (tag.getList("serverQueue").isPresent()) {
            ListTag queueList = tag.getList("serverQueue").get();
            for (Tag element : queueList) {
                CompoundTag queueEntry = (CompoundTag) element;
                String ip = queueEntry.getString("ip").get();
                String version = queueEntry.getString("version").get();
                Long lastScanned = queueEntry.getLong("lastScanned").orElse(null);
                Long lastSeen = queueEntry.getLong("lastSeen").orElse(null);
                serverQueue.add(new ServerStorage(ip, version, lastScanned, lastSeen));
            }
        }

        this.currentServerIndex = tag.getInt("currentServerIndex").orElse(0);
        if (this.currentServerIndex < 0 || this.currentServerIndex > serverQueue.size()) {
            this.currentServerIndex = 0;
        }

        return super.fromTag(tag);
    }
}
