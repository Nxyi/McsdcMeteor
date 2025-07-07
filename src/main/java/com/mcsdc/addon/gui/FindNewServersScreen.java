package com.mcsdc.addon.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcsdc.addon.Main;
import com.mcsdc.addon.MultiplayerScreenUtils;
import com.mcsdc.addon.system.MOTD;
import com.mcsdc.addon.system.McsdcSystem;
import com.mcsdc.addon.system.ServerSearchBuilder;
import com.mcsdc.addon.system.ServerStorage;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FindNewServersScreen extends WindowScreen {
    private static FindNewServersScreen instance = null;
    private MultiplayerScreen multiplayerScreen;
    private Screen parent;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();
    private boolean searching = false;
    List<ServerStorage> extractedServers;

    private final Setting<Boolean> hideOfflineSetting = sg.add(new BoolSetting.Builder()
        .name("hide offline")
        .description("")
        .defaultValue(true)
        .build()
    );

    private final Setting<Flags> visitedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("visited")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> moddedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("modded")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> whitelistSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("whitelist")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> crackedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("cracked")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> griefedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("griefed")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> savedSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("saved")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> activeSetting = sg.add(new EnumSetting.Builder<Flags>()
        .name("active")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> hasHistory = sg.add(new EnumSetting.Builder<Flags>()
        .name("hasHistory")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Flags> hasNotes = sg.add(new EnumSetting.Builder<Flags>()
        .name("hasNotes")
        .description("")
        .defaultValue(Flags.ANY)
        .build()
    );

    private final Setting<Boolean> advancedMOTD = sg.add(new BoolSetting.Builder()
        .name("MOTD-Searching")
        .description("")
        .defaultValue(false)
        .onChanged((v) -> {
            reload();
            if (extractedServers != null && !extractedServers.isEmpty()){
                bruh();
            }


        })
        .build()
    );

    private final Setting<Flags> DEFAULT = sg.add(new EnumSetting.Builder<Flags>()
        .name("default-MOTD")
        .description("")
        .defaultValue(Flags.ANY)
        .visible(advancedMOTD::get)
        .build()
    );

    private final Setting<Flags> COMMUNITY = sg.add(new EnumSetting.Builder<Flags>()
        .name("community-MOTD")
        .description("")
        .defaultValue(Flags.ANY)
        .visible(advancedMOTD::get)
        .build()
    );

    private final Setting<Flags> CREATIVE = sg.add(new EnumSetting.Builder<Flags>()
        .name("creative-MOTD")
        .description("")
        .defaultValue(Flags.ANY)
        .visible(advancedMOTD::get)
        .build()
    );

    private final Setting<Flags> BIGOTRY = sg.add(new EnumSetting.Builder<Flags>()
        .name("bigotry-MOTD")
        .description("")
        .defaultValue(Flags.ANY)
        .visible(advancedMOTD::get)
        .build()
    );

    private final Setting<Flags> FURRY = sg.add(new EnumSetting.Builder<Flags>()
        .name("furry-MOTD")
        .description("")
        .defaultValue(Flags.ANY)
        .visible(advancedMOTD::get)
        .build()
    );

    private final Setting<Flags> LGBT = sg.add(new EnumSetting.Builder<Flags>()
        .name("lgbt-MOTD")
        .description("")
        .defaultValue(Flags.ANY)
        .visible(advancedMOTD::get)
        .build()
    );

    private final Setting<Boolean> advancedVersionSetting = sg.add(new BoolSetting.Builder()
        .name("advanced version")
        .description("")
        .defaultValue(false)
        .onChanged((v) -> {
            reload();
            if (extractedServers != null && !extractedServers.isEmpty()){
                bruh();
            }


        })
        .build()
    );

    private final Setting<Boolean> vanilla = sg.add(new BoolSetting.Builder()
        .name("vanilla")
        .description("")
        .defaultValue(false)
        .visible(() -> !advancedVersionSetting.get())
        .build()
    );

    private final Setting<VersionEnum> versionEnumSetting = sg.add(new EnumSetting.Builder<VersionEnum>()
        .name("version")
        .description("")
        .defaultValue(VersionEnum.ANY)
        .visible(() -> !advancedVersionSetting.get())
        .build()
    );


    private final Setting<String> versionStringSetting = sg.add(new StringSetting.Builder()
        .name("version")
        .description("")
        .defaultValue("Paper 1.21.4")
        .visible(advancedVersionSetting::get)
        .build()
    );

    WContainer settingsContainer;

    public static FindNewServersScreen instance(MultiplayerScreen multiplayerScreen, Screen parent) {
        if (instance == null) {
            instance = new FindNewServersScreen();
        }
        instance.setMultiplayerScreen(multiplayerScreen);
        instance.setParent(parent);
        return instance;
    }

    public void setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        this.multiplayerScreen = multiplayerScreen;
    }

    public void setParent(Screen parent) {
        this.parent = parent;
    }

    public FindNewServersScreen() {
        super(GuiThemes.get(), "Find Servers");
    }

    @Override
    public void initWidgets() {
        WContainer settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("search")).expandX().widget().action = () -> {
            if (searching) return;
            reload();

            if (!advancedMOTD.get() && visitedSetting.get().bool == null && griefedSetting.get().bool  == null && moddedSetting.get().bool  == null && savedSetting.get().bool  == null && whitelistSetting.get().bool  == null && activeSetting.get().bool == null && crackedSetting.get().bool  == null){
                add(theme.label("Everything searches are not allowed.")).expandX().widget();
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                searching = true;
                add(theme.label("Searching...")).expandX().widget();

                Object ver;
                if (!advancedVersionSetting.get()){

                    int number = versionEnumSetting.get().number;
                    boolean isVanilla = vanilla.get();

                    if (isVanilla && number != -1) {
                        ver = versionEnumSetting.get().version; // Use the string version
                    } else if (number != -1) {
                        ver = number; // Use protocol number if valid
                    } else {
                        ver = null; // Set to null only if explicitly invalid
                    }
                } else {
                    if (versionStringSetting.get().isEmpty()){
                        return null;
                    }
                    ver = versionStringSetting.get();
                }

                ServerSearchBuilder.Version versionString = new ServerSearchBuilder.Version(ver);
                HashMap<MOTD, Boolean> motds = null;
                if (advancedMOTD.get()) {
                    motds = new HashMap<>();

                    motds.put(MOTD.DEFAULT, DEFAULT.get().bool);
                    motds.put(MOTD.COMMUNITY, COMMUNITY.get().bool);
                    motds.put(MOTD.CREATIVE, CREATIVE.get().bool);
                    motds.put(MOTD.BIGOTRY, BIGOTRY.get().bool);
                    motds.put(MOTD.FURRY, FURRY.get().bool);
                    motds.put(MOTD.LGBT, LGBT.get().bool);

                    for (MOTD motd : MOTD.values()) {
                        motd.setSearch(motds.get(motd));
                    }
                }

                ServerSearchBuilder.Extra extra = new ServerSearchBuilder.Extra(hasHistory.get().bool, hasNotes.get().bool, motds);
                ServerSearchBuilder.Flags flags = new ServerSearchBuilder.Flags(visitedSetting.get().bool, griefedSetting.get().bool, moddedSetting.get().bool, savedSetting.get().bool, whitelistSetting.get().bool, activeSetting.get().bool, crackedSetting.get().bool);
                ServerSearchBuilder.Search searchString = new ServerSearchBuilder.Search(versionString, flags, extra);

                JsonObject jsonString = ServerSearchBuilder.createJson(searchString);
                Main.LOG.info(jsonString.toString());

                return Http.post(Main.mainEndpoint).bodyString(jsonString.toString()).header("authorization", "Bearer " + McsdcSystem.get().getToken()).sendString();
            }).thenAccept(response -> {
                Main.mc.execute(() -> {
                    searching = false;
                    reload();

                    // some janky shit because it complains
                    String res = response;
                    if (response.endsWith(",]")){ // depending on the search, response can be slightly malformed.
                        res = response.substring(0, response.length() - 2) + "]";
                    }

                    extractedServers = extractServerInfo(res);
                    if (hideOfflineSetting.get()){
                        extractedServers.removeIf(server -> server.lastScanned() - server.lastSeen() > 40 * 60 * 1000);
                    }

                    if (response == null || extractedServers.isEmpty()){
                        add(theme.label("No servers found.")).expandX().widget();
                        return;
                    }
                    bruh();

                });

            });
        };
    }

    public void bruh(){
        WHorizontalList buttons = add(theme.horizontalList()).expandX().widget();

        buttons.add(theme.button("add all")).expandX().widget().action = () -> {
            extractedServers.forEach((server) -> {
                ServerInfo info = new ServerInfo("Mcsdc " + server.ip(), server.ip(), ServerInfo.ServerType.OTHER);
                multiplayerScreen.getServerList().add(info, false);
            });
            MultiplayerScreenUtils.save(this.multiplayerScreen);
            MultiplayerScreenUtils.reload(this.multiplayerScreen);
        };

        buttons.add(theme.button("randomize")).expandX().widget().action = () -> {
            reload();

            Collections.shuffle(extractedServers);
            bruh();
        };

        generateWidgets(extractedServers);
    }

    public void generateWidgets(List<ServerStorage> extractedServers){
        Main.mc.execute(() -> {
            WTable table = add(theme.table()).widget();
            table.clear();

            table.add(theme.label("Server IP"));
            table.add(theme.label("Version"));
            table.row();
            table.add(theme.horizontalSeparator()).expandX();
            table.row();

            // Iterate through the extracted server data
            extractedServers.forEach((server) -> {
                String serverIP = server.ip();
                String serverVersion = server.version();

                table.add(theme.label(serverIP));
                table.add(theme.label(serverVersion));

                WButton addServerButton = theme.button("Add Server");
                addServerButton.action = () -> {
                    ServerInfo info = new ServerInfo("Mcsdc " + serverIP, serverIP, ServerInfo.ServerType.OTHER);
                    multiplayerScreen.getServerList().add(info, false);
                    MultiplayerScreenUtils.save(this.multiplayerScreen);
                    MultiplayerScreenUtils.reload(this.multiplayerScreen);
                    addServerButton.visible = false;
                };

                WButton joinServerButton = theme.button("Join Server");
                joinServerButton.action = () ->
                    ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), Main.mc,
                        ServerAddress.parse(serverIP), new ServerInfo("", serverIP, ServerInfo.ServerType.OTHER), false, null);

                WButton serverInfoButton = theme.button("Server Info");
                serverInfoButton.action = () -> {
                    Main.mc.setScreen(new ServerInfoScreen(serverIP));
                };

                table.add(addServerButton);
                table.add(joinServerButton);
                table.add(serverInfoButton);
                table.row();
            });
        });
    }

    public static List<ServerStorage> extractServerInfo(String jsonResponse) {
        List<ServerStorage> serverStorageList = new ArrayList<>();
        JsonArray jsonObject = JsonParser.parseString(jsonResponse).getAsJsonArray();

        jsonObject.forEach(node -> {
            String address = node.getAsJsonObject().get("address").getAsString();
            String version = node.getAsJsonObject().get("version").getAsString();
            long lastscanned = node.getAsJsonObject().get("last_scanned").getAsLong();
            long lastseen = node.getAsJsonObject().get("last_seen_online").getAsLong();
            serverStorageList.add(new ServerStorage(address, version, lastscanned, lastseen));
        });

        return serverStorageList;
    }

    public enum Flags{
        YES(true),
        NO(false),
        ANY(null);

        Boolean bool;
        Flags(Boolean bool){
            this.bool =bool;
        }
    }

    public enum VersionEnum {
        ANY("any", -1),
        _1_21_7("1.21.7", 772),
        _1_21_6("1.21.6", 771),
        _1_21_5("1.21.5", 770),
        _1_21_4("1.21.4", 769),
        _1_21_2("1.21.2", 768),
        _1_21("1.21", 767),
        _1_20_5("1.20.5", 766),
        _1_20_3("1.20.3", 765),
        _1_20_2("1.20.2", 764),
        _1_20("1.20", 763),
        _1_19_4("1.19.4", 762),
        _1_19_3("1.19.3", 761),
        _1_19_2("1.19.2", 760),
        _1_19("1.19", 759),
        _1_18_2("1.18.2", 758),
        _1_18_1("1.18.1", 757),
        _1_17_1("1.17.1", 756),
        _1_17("1.17", 755),
        _1_16_5("1.16.5", 754),
        _1_16_3("1.16.3", 753),
        _1_16_2("1.16.2", 751),
        _1_16_1("1.16.1", 736),
        _1_16("1.16", 735),
        _1_15_2("1.15.2", 578),
        _1_15_1("1.15.1", 575),
        _1_15("1.15", 573),
        _1_14_4("1.14.4", 498),
        _1_14_3("1.14.3", 490),
        _1_14_2("1.14.2", 485),
        _1_14_1("1.14.1", 480),
        _1_14("1.14", 477),
        _1_13_2("1.13.2", 404),
        _1_13_1("1.13.1", 401),
        _1_13("1.13", 393),
        _1_12_2("1.12.2", 340),
        _1_12_1("1.12.1", 338),
        _1_12("1.12", 335),
        _1_11_2("1.11.2", 316),
        _1_11("1.11", 315),
        _1_10_2("1.10.2", 210),
        _1_9_4("1.9.4", 110),
        _1_9_1("1.9.1", 108),
        _1_8_9("1.8.9", 47),
        _1_7_10("1.7.10", 5),
        _1_7_5("1.7.5", 4);
        private final String version;
        private final int number;

        VersionEnum(String version, int number) {
            this.version = version;
            this.number = number;
        }

        public String getVersion() {
            return version;
        }

        public int getNumber() {
            return number;
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
