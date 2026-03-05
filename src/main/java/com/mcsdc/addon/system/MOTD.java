package com.mcsdc.addon.system;

public enum MOTD {
    DEFAULT("default"),
    COMMUNITY("community"),
    CREATIVE("creative"),
    BIGOTRY("bigotry"),
    FURRY("furry"),
    LGBT("lgbt");

    private final String name;

    MOTD(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
