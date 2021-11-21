package net.kunmc.lab.corrosion.config;

import net.kunmc.lab.corrosion.Corrosion;
import net.kunmc.lab.corrosion.command.CommandConst;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    static FileConfiguration config;

    // コンフィグ管理用のリスト
    public static Map<String, String> stringConfig = new HashMap();
    public static Map<String, Integer> integerConfig = new HashMap();
    public static Map<String, Boolean> booleanConfig = new HashMap();

    public static void loadConfig(boolean isReload) {
        Corrosion.getPlugin().saveDefaultConfig();

        if (isReload) {
            Corrosion.getPlugin().reloadConfig();
        }
        //　コンフィグファイルを取得
        config = Corrosion.getPlugin().getConfig();

        integerConfig.put(CommandConst.CONFIG_UPDATE_BLOCK_TIME, config.getInt(CommandConst.CONFIG_UPDATE_BLOCK_TIME));
        integerConfig.put(CommandConst.CONFIG_START_RANGE, config.getInt(CommandConst.CONFIG_START_RANGE));
        booleanConfig.put(CommandConst.CONFIG_CORROSION_DEATH, false);
        booleanConfig.put(CommandConst.CONFIG_CORROSION_BREAK, false);

        for (String key : config.getStringList("switch")) {
            booleanConfig.put(key, true);
        }
    }

    public static void setConfig(String key) {
        if (integerConfig.containsKey(key)) {
            config.set(key, integerConfig.get(key));
        } else if (booleanConfig.containsKey(key)) {
            ArrayList<String> tmpList = new ArrayList();
            for (String booleanKey: booleanConfig.keySet()) {
                if (booleanConfig.get(booleanKey)) tmpList.add(booleanKey);
            }
            config.set("switch", tmpList);
        }
        Corrosion.getPlugin().saveConfig();
    }
}
