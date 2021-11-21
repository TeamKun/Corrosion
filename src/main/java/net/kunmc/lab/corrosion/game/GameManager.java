package net.kunmc.lab.corrosion.game;

import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
import net.kunmc.lab.corrosion.task.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    // 動作中のモード保持
    public static GameMode runningMode = GameMode.MODE_NEUTRAL;

    public static List<String> blockList = new ArrayList<>();

    public static void controller(GameMode runningMode, Player p) {
        // モードを設定
        GameManager.runningMode = runningMode;

        switch (runningMode) {
            case MODE_START:
                GameManager.initBlock(p);
                Task.updateBlock();
                break;
            case MODE_NEUTRAL:
                Task.stopUpdateBlock();
                break;
        }
    }

    public enum GameMode {
        // ゲーム外の状態
        MODE_NEUTRAL,
        MODE_START
    }

    public static boolean isRunning(){
        return runningMode == GameMode.MODE_START;
    }

    public static void initBlock(Player p) {
        /**
         * プラグインスタート時の腐食ブロックの取得
         * World全部の腐食ブロックを取得するのはいろいろ無茶なので、start時点の特定プレイヤーの周囲を探索して腐食ブロックを取得する
         */
        int bx = (int) p.getLocation().getX();
        int bz = (int) p.getLocation().getZ();
        int range = ConfigManager.integerConfig.get(CommandConst.CONFIG_START_RANGE);
        for (int x = -1*range; x<range;x++) {
            for (int y = 0; y<256;y++) {
                for (int z = -1*range; z<range;z++) {
                    Block block = p.getWorld().getBlockAt(bx+x, y, bz+z);
                    if (isCorrosionBlock(block)){
                        GameManager.blockList.add(GameManager.getPosStringFromBlock(block));
                    }
                }
            }
        }
    }

    public static boolean updateBlock(Block block, List<String> nextList){
        /**
         * 腐食を進めるメインロジック
         * 腐食対象のブロックの上下左右前後に変換可能なブロックがあれば変換
         * 腐食対象のブロックが関与できるブロックがなければ削除(Airに変換)
         * 削除された場合はfalseを返す
         */

        int index[][] = {
                {0,0,-1}, {0,0,1},
                {0,-1,0}, {0,1,0},
                {-1,0,0}, {1,0,0}};
        boolean updated = false;
        for (int[] blockIndex: index) {
            Location loc = block.getLocation();
            Vector v = new Vector(blockIndex[0], blockIndex[1], blockIndex[2]);
            loc.add(v);
            if (!loc.getBlock().getType().equals(Material.PURPLE_CONCRETE) &&
                    !loc.getBlock().getType().equals(Material.AIR)) {
                loc.getBlock().setBlockData(Material.PURPLE_CONCRETE.createBlockData());
                updated = true;
                nextList.add(GameManager.getPosStringFromBlock(loc.getBlock()));
            }
        }
        block.setBlockData(Material.AIR.createBlockData());
        return updated;
    }

    public static Block getBlockFromPosString(String pos){
        String[] loc = pos.split(" ");
        return Bukkit.getWorld(loc[0]).getBlockAt(
                (int)Double.parseDouble(loc[1]),
                (int)Double.parseDouble(loc[2]),
                (int)Double.parseDouble(loc[3])
        );
    }

    public static String getPosStringFromBlock(Block block){
        String world = block.getWorld().getName();
        Location loc = block.getLocation();
        return world + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ();
    }

    public static boolean isCorrosionBlock(Block block){
        return block.getType().equals(Material.PURPLE_CONCRETE);
    }
}