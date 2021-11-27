package net.kunmc.lab.corrosion.game;

import net.kunmc.lab.corrosion.Corrosion;
import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CorrosionManager {
    private static BukkitTask CorrosionBlock;
    private static BukkitTask DeleteBlock;

    public static void updateBlock() {
        /**
         * 腐食処理等のループメソッド
         */
        CorrosionBlock = new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println(CorrosionBlockManager.currentSearchCorrosionBlockList.size());
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
        }.runTaskTimer(Corrosion.getPlugin(), 0, ConfigManager.integerConfig.get(CommandConst.CONFIG_UPDATE_BLOCK_TIME) * 20);
    }

    public static void deleteBlock(){
        DeleteBlock = new BukkitRunnable() {
            @Override
            public void run() {
                // リセット判定
                CorrosionBlockManager.redirectCorrosion();
                // 腐敗対象を探索
                for (String pos : CorrosionBlockManager.currentSearchCorrosionBlockList) {
                    CorrosionBlockManager.searchAroundCorrosionBlock(CorrosionBlockManager.getBlockFromPosString(pos));
                }
                // 腐敗処理
                CorrosionBlockManager.corrodeBlock();
                // 不要なブロックを削除
                CorrosionBlockManager.deleteCorrosionBlock();
                // リスト更新
                CorrosionBlockManager.updateCurrentCorrosionBlock();
            }
        }.runTaskTimer(Corrosion.getPlugin(), 0, (ConfigManager.integerConfig.get(CommandConst.CONFIG_UPDATE_BLOCK_TIME)+1) * 20);
    }

    public static void stopUpdateBlock() {
        CorrosionBlock = null;
    }

    public static Player getTargetPlayer(){
        /**
         * 腐食ブロックの進行先ターゲットPlayer取得
         */

        // TODO: 更新する
        Player p = Bukkit.getPlayer("POne0301");
        if ( p==null || p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)) return null;

        return p;
    }
}
