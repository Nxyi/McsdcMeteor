package com.mcsdc.addon.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcsdc.addon.system.McsdcSystem;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class LoginScreen extends WindowScreen {

    WindowScreen parent;

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<String> tokenSetting = sg.add(new StringSetting.Builder()
        .name("token")
        .description("The token to use for the API.")
        .defaultValue("")
        .build()
    );

    public LoginScreen(WindowScreen parent) {
        super(GuiThemes.get(), "Login with Token");
        this.parent = parent;
    }
    WContainer settingsContainer;

    @Override
    public void initWidgets() {
        WContainer settingsContainer = add(theme.verticalList()).expandX().widget();
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("Submit")).expandX().widget().action = () -> {
            CompletableFuture.supplyAsync(() -> {
                String request = "{\"auth\":{\"login\":\"%s\"}}".formatted(tokenSetting.get());
                String response = Http.post("https://interact.mcsdc.online/api").bodyJson(request).sendString();

                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

                // Extract the "data" object
                JsonObject data = jsonObject.getAsJsonObject("data");

                // Extract the "name" and "perms" values
                String name = data.get("name").getAsString();
                int perms = data.get("perms").getAsInt();

                // Print the extracted values
                System.out.println("Name: " + name);
                System.out.println("Perms: " + perms);

                Map<String, Integer> map = new HashMap<>();
                map.put(name, perms);

                return map;
            }).thenAccept(response -> {
                String extractedName = response.keySet().iterator().next();
                int extractedPerms = response.get(extractedName);

                McsdcSystem.get().setToken(tokenSetting.get());
                McsdcSystem.get().setUsername(extractedName);
                McsdcSystem.get().setLevel(extractedPerms);

                mc.setScreen(this.parent);
                this.parent.reload();
            });
        };
    }
}
