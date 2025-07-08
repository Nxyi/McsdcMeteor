package com.mcsdc.addon.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcsdc.addon.Main;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.util.TicketIDGenerator;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.text.Text;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EditFlagsScreen extends WindowScreen {

    private final String ip;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.createGroup("Flags");

    private final Setting<String> notesSetting = sg.add(new StringSetting.Builder()
        .name("notes")
        .description("")
        .defaultValue("")
        .build()
    );

    private final Setting<Boolean> griefedSetting = sg.add(new BoolSetting.Builder()
        .name("griefed")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> savedSetting = sg.add(new BoolSetting.Builder()
        .name("saved")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> visitedSetting = sg.add(new BoolSetting.Builder()
        .name("visited")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> moddedSetting = sg.add(new BoolSetting.Builder()
        .name("modded")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> whitelistSetting = sg.add(new BoolSetting.Builder()
        .name("whitelist")
        .description("")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> bannedSetting = sg.add(new BoolSetting.Builder()
        .name("banned")
        .description("")
        .defaultValue(false)
        .build()
    );

    public EditFlagsScreen(String ip) {
        super(GuiThemes.get(), "Edit Flags");
        this.ip = ip;
    }

    @Override
    public void initWidgets() {
        CompletableFuture.supplyAsync(() -> {
            String string =
                "{\"search\":{\"address\":\"%s\"}}".formatted(this.ip);

            HttpResponse<String> response = Http.post(
                Main.mainEndpoint
            )
                .bodyString(string)
                .header(
                    "authorization",
                    "Bearer " + McsdcSystem.get().getToken()
                )
                .sendStringResponse();

            return response.body();
        }).thenAccept(response -> {
            if (response == null || response.isEmpty()){
                add(theme.label("Not Valid"));
                return;
            }

            Main.mc.execute(() -> {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

                if (jsonObject.has("error")) {
                    add(theme.label("Not Valid"));
                    return;
                }

                WTable table = add(theme.table()).widget();
                table.minWidth = 300;

                if (jsonObject.has("notes")) {
                    notesSetting.set(jsonObject.get("notes").getAsString());
                }
                griefedSetting.set(jsonObject.get("status").getAsJsonObject().get("griefed").getAsBoolean());
                savedSetting.set(jsonObject.get("status").getAsJsonObject().get("save_for_later").getAsBoolean());
                visitedSetting.set(jsonObject.get("status").getAsJsonObject().get("visited").getAsBoolean());
                moddedSetting.set(jsonObject.get("status").getAsJsonObject().get("modded").getAsBoolean());
                whitelistSetting.set(jsonObject.get("status").getAsJsonObject().get("whitelist").getAsBoolean());
                bannedSetting.set(jsonObject.get("status").getAsJsonObject().get("banned").getAsBoolean());
                table.add(theme.settings(settings)).expandX();
                table.row();
                table.add(theme.button("Save")).expandX().widget().action = this::setMarked;
                table.row();
            });
        });
    }

    public void setMarked(){
        String ip = this.ip;
        JsonObject mainJson = new JsonObject();
        JsonObject innerJson = new JsonObject();
        JsonObject flagJson = new JsonObject();

        flagJson.addProperty("visited", visitedSetting.get());
        flagJson.addProperty("griefed", griefedSetting.get());
        flagJson.addProperty("modded", moddedSetting.get());
        flagJson.addProperty("save_for_later", savedSetting.get());
        flagJson.addProperty("whitelist", whitelistSetting.get());
        flagJson.addProperty("banned", bannedSetting.get());

        innerJson.addProperty("address", ip);

        innerJson.addProperty("notes", notesSetting.get());

        innerJson.add("flags", flagJson);
        innerJson.addProperty("joined", true);
        mainJson.add("update", innerJson);

        Http.post(Main.mainEndpoint).bodyJson(mainJson).header(
                "authorization",
                "Bearer " + McsdcSystem.get().getToken()
            )
            .sendString();
    }
}
