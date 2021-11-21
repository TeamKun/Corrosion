package net.kunmc.lab.corrosion.task;

import jdk.nashorn.internal.ir.Block;
import net.kunmc.lab.corrosion.Corrosion;
import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
import net.kunmc.lab.corrosion.game.GameManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Task {
    private static BukkitTask task;

    public static void updateBlock() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<String> nextList = new ArrayList<>();
                        for (String pos: GameManager.blockList) {
                            GameManager.updateBlock(GameManager.getBlockFromPosString(pos), nextList);
                        }
                        GameManager.blockList.clear();
                        GameManager.blockList = nextList;
                        System.out.println(GameManager.blockList.size());
                    }
                }.runTask(Corrosion.getPlugin());
            }
        }.runTaskTimerAsynchronously(Corrosion.getPlugin(), 0, ConfigManager.integerConfig.get(CommandConst.CONFIG_UPDATE_BLOCK_TIME) * 20);
    }

    public static void stopUpdateBlock() {
        task = null;
    }
}
