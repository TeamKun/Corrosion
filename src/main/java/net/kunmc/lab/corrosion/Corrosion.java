package net.kunmc.lab.corrosion;

import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.command.CommandController;
import net.kunmc.lab.corrosion.config.ConfigManager;
import net.kunmc.lab.corrosion.event.CorrosionEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Corrosion extends JavaPlugin {

    private static Corrosion plugin;

    public static Corrosion getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new CorrosionEvent(), plugin);
        ConfigManager.loadConfig(false);
        getCommand(CommandConst.MAIN).setExecutor(new CommandController());
    }

    @Override
    public void onDisable() {
        getLogger().info("Corrosion Plugin is disabled");
    }
}
