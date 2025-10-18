package com.mcsdc.addon.system;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class ServerSearchBuilder {
    public static class Version {
        Object value;

        public Version(Object value) {
            this.value = value;
        }

        public JsonElement toJson() {
            if (value == null) {
                return null;
            }
            JsonObject versionObject = new JsonObject();
            if (value instanceof Integer) {
                versionObject.addProperty("protocol", (Integer) value);
            } else {
                versionObject.addProperty("name", value.toString());
            }
            return versionObject;
        }
    }

    public static class Flags {
        Boolean visited, griefed, modded, saved, whitelist, active, cracked;

        public Flags(Boolean visited, Boolean griefed, Boolean modded, Boolean saved,
                     Boolean whitelist, Boolean active, Boolean cracked) {
            this.visited = visited;
            this.griefed = griefed;
            this.modded = modded;
            this.saved = saved;
            this.whitelist = whitelist;
            this.active = active;
            this.cracked = cracked;
        }

        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            if (visited != null) jsonObject.addProperty("visited", visited);
            if (griefed != null) jsonObject.addProperty("griefed", griefed);
            if (modded != null) jsonObject.addProperty("modded", modded);
            if (saved != null) jsonObject.addProperty("saved", saved);
            if (whitelist != null) jsonObject.addProperty("whitelist", whitelist);
            if (active != null) jsonObject.addProperty("active", active);
            if (cracked != null) jsonObject.addProperty("cracked", cracked);
            return jsonObject;
        }
    }

    public static class Extra{
        Boolean hasHistory, hasNotes;
        HashMap<MOTD, Boolean> motds = null;

        public Extra(Boolean hasHistory, Boolean hasNotes, @Nullable HashMap<MOTD, Boolean> motds){
            this.hasHistory = hasHistory;
            this.hasNotes = hasNotes;
            this.motds = motds;
        }

        public JsonObject toJsonObject(){
            JsonObject jsonObject = new JsonObject();
            JsonObject motdJsonObject = new JsonObject();
            if (hasHistory != null) jsonObject.addProperty("has_history", hasHistory);
            if (hasNotes != null) jsonObject.addProperty("has_notes", hasHistory);
            if (motds != null){
                for (MOTD value : motds.keySet()) {
                    motdJsonObject.addProperty(value.getName(), value.shouldSearch());
                }

                jsonObject.add("motd", motdJsonObject);
            }

            return jsonObject;
        }
    }

    public static class Search {
        Version version;
        Flags flags;
        Extra extra;

        public Search(Version version, Flags flags, Extra extra) {
            this.version = version;
            this.flags = flags;
            this.extra = extra;
        }
    }

    public static JsonObject createJson(Search search) {
        JsonObject rootJson = new JsonObject(), searchJson = new JsonObject();

        if (search.version != null) {
            JsonElement versionElement = search.version.toJson();
            searchJson.add("version", versionElement);
        } else {
            searchJson.add("version", null);
        }

        JsonObject extraJson = search.extra != null ? search.extra.toJsonObject() : new JsonObject();
        searchJson.add("extra", extraJson);

        JsonObject flagsJson = search.flags != null ? search.flags.toJsonObject() : new JsonObject();
        searchJson.add("flags", flagsJson);

        rootJson.add("search", searchJson);
        return rootJson;
    }
}

