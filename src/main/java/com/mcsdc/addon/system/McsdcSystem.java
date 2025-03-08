package com.mcsdc.addon.system;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class McsdcSystem extends System<McsdcSystem> {
    private String token = "";
    private String username = "";
    private int level = -1;
    private List<ServerEntry> recentServers = new ArrayList<>();

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

    public List<ServerEntry> getRecentServers() {
        return recentServers;
    }

    public ServerEntry getRecentServerWithIp(String ip){
        for (ServerEntry server : recentServers){
            if (Objects.equals(server.ip, ip)){
                return server;
            }
        }
        return null;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound compound = new NbtCompound();
        compound.putString("token", this.token);
        compound.putString("username", this.username);
        compound.putInt("level", this.level);

        NbtList list = new NbtList();

        recentServers.forEach((server) -> {
            NbtCompound compound2 = new NbtCompound();
            compound2.putString("ip", server.ip);
            compound2.putString("version", server.version);
            list.add(compound2);
        });

        compound.put("recent", list);


        return compound;
    }

    @Override
    public McsdcSystem fromTag(NbtCompound tag) {
        this.token = tag.getString("token");
        this.username = tag.getString("username");
        this.level = tag.getInt("level");

        NbtList list = tag.getList("recent", 10);
        for (NbtElement element : list){
            NbtCompound compound = (NbtCompound) element;
            String ip = compound.getString("ip");
            String ver = compound.getString("version");

            recentServers.add(new ServerEntry(ip, ver));
        }

        // reverse servers to ensure they are in the correct order. or they would flip each time.
        Collections.reverse(recentServers);

        return super.fromTag(tag);
    }
}
