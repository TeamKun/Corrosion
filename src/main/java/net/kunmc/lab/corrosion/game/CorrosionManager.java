package net.kunmc.lab.corrosion.game;

import net.kunmc.lab.corrosion.Corrosion;
import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CorrosionManager {
    private static BukkitTask corrosionBlock;
    private static BukkitTask deleteBlock;

    public static void updateBlock() {
        /**
         * 腐食処理等のループメソッド
         */
        corrosionBlock = new BukkitRunnable() {
            @Override
            public void run() {
               // 腐敗対象を探索
                for (String pos : CorrosionBlockManager.currentSearchCorrosionBlockList) {
                    CorrosionBlockManager.searchAroundCorrosionBlock(CorrosionBlockManager.getBlockFromPosString(pos));
                }
                // リスト更新
                CorrosionBlockManager.currentSearchCorrosionBlockList.clear();
                CorrosionBlockManager.updateCurrentCorrosionBlock();
                // 腐敗処理
                CorrosionBlockManager.corrodeBlock();
                // 不要なブロックを削除
                CorrosionBlockManager.deleteCorrosionBlock();
                // リセット判定
                CorrosionBlockManager.redirectCorrosion();
            }
        }.runTaskTimer(Corrosion.getPlugin(), 0, ConfigManager.integerConfig.get(CommandConst.CONFIG_UPDATE_BLOCK_TICK));
    }

    //public static void deleteBlock(){
    //    deleteBlock = new BukkitRunnable() {
    //        @Override
    //        public void run() {
    //            // リセット判定
    //            CorrosionBlockManager.redirectCorrosion();
    //            // 腐敗対象を探索
    //            for (String pos : CorrosionBlockManager.currentSearchCorrosionBlockList) {
    //                CorrosionBlockManager.searchAroundCorrosionBlock(CorrosionBlockManager.getBlockFromPosString(pos));
    //            }
    //            // 腐敗処理
    //            CorrosionBlockManager.corrodeBlock();
    //            // 不要なブロックを削除
    //            CorrosionBlockManager.deleteCorrosionBlock();
    //            // リスト更新
    //            CorrosionBlockManager.updateCurrentCorrosionBlock();
    //        }
    //    }.runTaskTimer(Corrosion.getPlugin(), 0, (ConfigManager.integerConfig.get(CommandConst.CONFIG_UPDATE_BLOCK_TICK) + 20));
    //}

    public static void stopUpdateBlock() {
        if (corrosionBlock != null) {
            corrosionBlock.cancel();
            CorrosionBlockManager.currentSearchCorrosionBlockList.clear();
            CorrosionBlockManager.nextSearchCorrosionBlockList.clear();
            CorrosionBlockManager.targetCorrosionBlockList.clear();
            CorrosionBlockManager.targetDeleteBlockList.clear();
            corrosionBlock = null;
        }
    }

    public static void pauseUpdateBlock() {
        if (corrosionBlock != null) {
            corrosionBlock.cancel();
            corrosionBlock = null;
        }
    }

    public static Player getTargetPlayer(){
        /**
         * 腐食ブロックの進行先ターゲットPlayer取得
         */

        Player p = Bukkit.getPlayer(ConfigManager.stringConfig.get(CommandConst.CONFIG_PLAYER));
        return p;
    }
}
