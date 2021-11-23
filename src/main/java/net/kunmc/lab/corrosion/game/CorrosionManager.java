package net.kunmc.lab.corrosion.game;

import net.kunmc.lab.corrosion.Corrosion;
import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class CorrosionManager {
    private static BukkitTask CorrosionBlock;
    private static BukkitTask DeleteBlock;

    public static void updateBlock() {
        CorrosionBlock = new BukkitRunnable() {
            @Override
            public void run() {
                Set<String> nextList = new HashSet<>();
                for (String pos : CorrosionBlockManager.blockList) {
                    CorrosionBlockManager.searchAroundCorrosionBlock(CorrosionBlockManager.getBlockFromPosString(pos));
                }
                CorrosionBlockManager.blockList.clear();
                CorrosionBlockManager.blockList = nextList;
            }
        }.runTaskTimer(Corrosion.getPlugin(), 0, ConfigManager.integerConfig.get(CommandConst.CONFIG_UPDATE_BLOCK_TIME) * 20);
    }

    public static void stopUpdateBlock() {
        CorrosionBlock = null;
    }

    public static Player getTargetPlayer(){
        /* 腐食ブロックの周辺のTarget取得(クリエ、スペクは対象外) */
        Player p = Bukkit.getPlayer("hogehoge");
        if (p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)) return null;

        return p;
    }
}
