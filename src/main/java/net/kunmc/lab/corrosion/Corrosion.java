package net.kunmc.lab.corrosion;

import org.bukkit.plugin.java.JavaPlugin;

public final class Corrosion extends JavaPlugin {

    private static Corrosion plugin;

    public static Corrosion getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
