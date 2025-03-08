package com.mcsdc.addon.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerStorage extends ArrayList<ServerEntry> {
    public ServerStorage() {
    }

    public ServerEntry getServerEntry(String ip) {
        for (ServerEntry entry : this) {
            if (Objects.equals(entry.ip, ip)){
                return entry;
            }
        }

        return null;
    }

    public void setList(List<ServerEntry> list) {
        this.clear();
        this.addAll(list);
    }

    public void setList(ArrayList<ServerEntry> list) {
        this.clear();
        this.addAll(list);
    }
}
