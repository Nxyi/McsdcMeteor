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
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorclient.utils.network.Http;
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

public class ServerInfoScreen extends WindowScreen {

    private final String ip;

    public ServerInfoScreen(String ip) {
        super(GuiThemes.get(), "ServerInfo");
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

                if (jsonObject.has("error")){
                    add(theme.label("Not Valid"));
                    return;
                }

                WTable table = add(theme.table()).widget();

                table.add(theme.horizontalSeparator("Info")).expandX().widget();
                table.row();

                table.add(
                    theme.label("Ip: %s".formatted(this.ip))
                );
                table.add(theme.button("Copy")).widget().action = () -> {
                    Main.mc.keyboard.setClipboard(this.ip);
                };

                table.row();

                String ticketID = TicketIDGenerator.generateTicketID(this.ip);
                table.add(
                    theme.label("ID: %s".formatted(ticketID))
                );
                table.add(theme.button("Copy")).widget().action = () -> {
                    Main.mc.keyboard.setClipboard(ticketID);
                };

                table.row();

                table.add(
                    theme.label("version: %s".formatted(jsonObject.get("version").getAsString()))
                );
                table.row();

                if (jsonObject.has("notes")){
                    table.add(
                        theme.label("Notes: %s".formatted(jsonObject.get("notes").getAsString().strip()))
                    );
                    table.row();
                }

                table.add(theme.horizontalSeparator("Status")).expandX().widget();
                table.row();

                table.add(
                    theme.label("visited: %s".formatted(jsonObject.get("status").getAsJsonObject().get("visited").getAsString()))
                );
                table.row();
                table.add(
                    theme.label("griefed: %s".formatted(jsonObject.get("status").getAsJsonObject().get("griefed").getAsString()))
                );
                table.row();
                table.add(
                    theme.label("modded: %s".formatted(jsonObject.get("status").getAsJsonObject().get("modded").getAsString()))
                );
                table.row();
                table.add(
                    theme.label("whitelist: %s".formatted(jsonObject.get("status").getAsJsonObject().get("whitelist").getAsString()))
                );
                table.row();
                table.add(
                    theme.label("banned: %s".formatted(jsonObject.get("status").getAsJsonObject().get("banned").getAsString()))
                );
                table.row();
                table.add(
                    theme.label(
                        "save for later: %s".formatted(
                            jsonObject
                                .get("status").getAsJsonObject()
                                .get("save_for_later")
                                .getAsString()
                        )
                    )
                );
                table.row();

//            String notes = jsonObject.get("notes").getAsString();
//            Main.LOG.info(notes);
//            if (notes != null && !notes.isEmpty()){
//                table.add(
//                    theme.label("notes: %s".formatted(notes))
//                );
//            }

                table.add(theme.horizontalSeparator("Scanned")).expandX().widget();
                table.row();

                table.row();
                table.add(
                    theme.label("last seen online: %s".formatted(timeAgo(jsonObject.get("last_seen_online").getAsLong())))
                );
                table.row();
                table.add(
                    theme.label("last scanned: %s".formatted(timeAgo(jsonObject.get("last_scanned").getAsLong())))
                );
                table.row();
                table.add(
                    theme.label("last joined: %s".formatted(timeAgo(jsonObject.get("last_joined").getAsLong()))));
                table.row();

                WTable accounts = add(theme.table()).expandX().widget();
                accounts.add(theme.horizontalSeparator("Historical")).expandX().widget();
                accounts.row();

                JsonArray array = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("historical");
                List<PlayerInfo> players = new ArrayList<>();
                for (JsonElement jsonElement : array){
                    String name;
                    try { // some weird response can send the name as a JsonArray. so if thats the case, im just gonna skip it.
                        name = jsonElement.getAsJsonObject().get("name").getAsString();
                    } catch (Exception exception){
                        continue;
                    }

                    String uuid = jsonElement.getAsJsonObject().get("uuid").getAsString();

                    if (uuid.endsWith("0000-000000000000")) { // depending on stuff, uuid can start with "????" so, checking for the end is good enough. as no real uuid should end with that
                        continue;
                    }

                    players.add(new PlayerInfo(name, uuid));
                }

                if (players.isEmpty()) accounts.add(theme.label("No historical players found."));
                else {
                    for (PlayerInfo info : players) {
                        accounts.add(theme.label(info.name)).expandX().widget();
                        accounts.add(theme.button("Login")).expandX().widget().action = () -> {
                            new CrackedAccount(info.name).login();
                        };

                        if (Main.mc.world == null) {
                            accounts.add(theme.button("Login & join")).expandX().widget().action = () -> {
                                new CrackedAccount(info.name).login();

                                ServerInfo serverInfo = new ServerInfo("Mcsdc " + this.ip, this.ip, ServerType.OTHER);
                                ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), Main.mc,
                                    ServerAddress.parse(serverInfo.address), serverInfo, false, null);
                            };
                        } else {
                            accounts.add(theme.button("Login & rejoin")).expandX().widget().action = () -> {
                                new CrackedAccount(info.name).login();

                                ServerInfo serverInfo = Main.mc.getNetworkHandler().getServerInfo();
                                Main.mc.world.disconnect(Text.of(""));
                                ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), Main.mc,
                                    ServerAddress.parse(serverInfo.address), serverInfo, false, null);
                            };
                        }

                        if (players.getLast() != info) accounts.row();
                    }
                }
            });
        });
    }

    public static String timeAgo(long timestampMillis) {
        if (timestampMillis == 0) return "never";

        long currentMillis = System.currentTimeMillis();
        long diffMillis = currentMillis - timestampMillis;

        if (diffMillis < 0) {
            return "In the future"; // Handles cases where timestamp is in the future
        }

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis);
        if (seconds < 60) {
            return seconds + " seconds ago";
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        if (minutes < 60) {
            return minutes + " minutes ago";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
        return hours + " hours ago";
    }

    public record PlayerInfo(String name, String uuid){}
}
