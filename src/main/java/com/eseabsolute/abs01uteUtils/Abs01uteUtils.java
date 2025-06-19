package com.eseabsolute.abs01uteUtils;

import com.eseabsolute.abs01uteUtils.commands.TeleportToCursorCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Abs01uteUtils extends JavaPlugin {
    private static Abs01uteUtils instance;

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic

        this.getCommand("teleporttocursor").setExecutor(new TeleportToCursorCommand());
    }

    public static Abs01uteUtils getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
