package com.mcsdc.addon;

import com.mcsdc.addon.commands.TicketIDCommand;
import com.mcsdc.addon.util.TicketIDGenerator;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import org.meteordev.starscript.value.Value;
import org.slf4j.Logger;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Main extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static String mainEndpoint = "https://interact.mcsdc.online/api";

    @Override
    public void onInitialize() {
        LOG.info("Initializing mcsdc.");
        Commands.add(new TicketIDCommand());
        MeteorStarscript.ss.set("ticketID", () -> Value.string(getTicketID()));
    }

    @Override
    public String getPackage() {
        return "com.mcsdc.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Nxyi", "McsdcMeteor");
    }

    public static String getTicketID(){
        if (mc == null || mc.getNetworkHandler() == null) return "";

        return TicketIDGenerator.generateTicketID(mc.getNetworkHandler().getServerInfo().address);
    }
}
