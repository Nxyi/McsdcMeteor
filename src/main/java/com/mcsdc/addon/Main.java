package com.mcsdc.addon;

import com.mcsdc.addon.system.ServerEntry;
import com.mcsdc.addon.system.ServerStorage;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Main extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static String mainEndpoint = "https://interact.mcsdc.online/api";

    private static final ServerStorage serverStorage = new ServerStorage();

    public static ServerStorage getServerStorage() {
        return serverStorage;
    }

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon Template");
    }

    @Override
    public String getPackage() {
        return "com.mcsdc.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
    }
}
