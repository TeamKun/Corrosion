package net.kunmc.lab.corrosion.game;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
import net.kunmc.lab.corrosion.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class CorrosionBlockManager {
    /**
     * 腐食の仕組み、愚直にやるとすぐに落ちるので以下のようにする
     *   - controlCorrosionBlockListで腐敗対象のブロックを管理
     *     -
     *   - targetCorrosionBlockListで対象のブロックを腐敗
     *   - targetDeleteBlockListで腐敗伝達済みのブロックを削除
     */
    // Stringにはブロックの座標を持つ(world名,x軸,y軸,z軸がスペース区切り)

    // 腐敗対象に回すブロックを保持
    //   - currentSearchCorrosionBlockList
    //     - 腐敗ブロックにするか、削除対象にするかの処理対象を格納するリスト
    //   - nextSearchCorrosionBlockList
    //     - currentSearchCorrosionBlockListの次の探索対象を管理するリスト
    //     - 定期的にcurrentSearchCorrosionBlockListに内容を移す
    public static Set<String> currentSearchCorrosionBlockList = new HashSet<>();
    public static Set<String> nextSearchCorrosionBlockList = new HashSet<>();

    // 腐敗対象のブロックの座標を保持(Integerには腐食ブロックに変更するまでの遅延(チェック判定回数)を持つ)
    public static Map<String, Integer> targetCorrosionBlockList = new HashMap();

    // 削除対象のブロックの座標を保持(Integerにはブロックを削除するまでの遅延(チェック判定回数)を持つ)
    public static Set<String> targetDeleteBlockList = new HashSet<>();

    private static Random rand = new Random();
    public static void initBlock(Player p) {
        /**
         * プラグインスタート時の腐食ブロックの取得
         * World全部の腐食ブロックを取得するのは無茶なので、start時点の特定プレイヤーの周囲を探索して腐食ブロックを取得する
         */
        int bx = (int) p.getLocation().getX();
        int bz = (int) p.getLocation().getZ();
        int range = ConfigManager.integerConfig.get(CommandConst.CONFIG_START_RANGE);
        for (int x = -1*range; x<range;x++) {
            for (int z = -1*range; z<range;z++) {
                for (int y = 0; y<256;y++) {
                    if (y > p.getWorld().getHighestBlockYAt(x,z)) break;
                    Block block = p.getWorld().getBlockAt(bx+x, y, bz+z);
                    if (!isCorrosionBlock(block)) continue;

                    currentSearchCorrosionBlockList.add(getPosStringFromBlock(block));
                }
            }
        }
    }

    public static void searchAroundCorrosionBlock(Block block){
        /**
         * 腐食対象を探す基本ロジック
         *  - 腐食対象のブロックの上下左右前後に変換可能なブロックがあれば変換対象として追加
         *  - 腐食対象のブロックが関与できるブロックがなければ削除対象として追加
         */

        int index[][] = getCorrosionIndex();
        for (int[] blockIndex: index) {
            Location loc = block.getLocation();
            loc.add(blockIndex[0], blockIndex[1], blockIndex[2]);
            Block locBlock = loc.getBlock();

            // 腐敗させないブロック(腐敗予定のブロック含む)
            String pos = getPosStringFromBlock(locBlock);
            if (locBlock.getType().equals(Material.AIR) || !isCorrosionTarget(pos)) continue;

            targetCorrosionBlockList.put(pos, rand.nextInt(2));
            nextSearchCorrosionBlockList.add(pos);
        }
    }

    public static void corrodeBlock(){
        Set<String> deleteBlockList = new HashSet<>();
        for (String pos: targetCorrosionBlockList.keySet()) {
            int check = targetCorrosionBlockList.get(pos)-1;
            if (check < 0) {
                Block block = getBlockFromPosString(pos);
                if (!block.getType().equals(Material.PURPLE_CONCRETE))
                    Utils.setTypeAndData(getBlockFromPosString(pos), Material.PURPLE_CONCRETE.createBlockData(), false);
                targetDeleteBlockList.add(pos);
                deleteBlockList.add(pos);
            } else {
                targetCorrosionBlockList.put(pos, check);
            }
        }
        for (String pos: deleteBlockList) {
            targetCorrosionBlockList.remove(pos);
        }
    }

    public static void deleteCorrosionBlock() {
        Set<String> deleteBlockList = new HashSet<>();
        for (String pos: targetDeleteBlockList) {
            Block block = getBlockFromPosString(pos);
            if (shouldDeleteBlock(block)) {
                block.setType(Material.AIR);
                deleteBlockList.add(pos);
            }
        }
        for (String pos: deleteBlockList) {
            targetDeleteBlockList.remove(pos);
        }
    }

    private static boolean shouldDeleteBlock(Block block){
        int index[][] = getCorrosionIndex();
        boolean shouldDelete = true;
        for (int[] blockIndex: index) {
            Location loc = block.getLocation();

            loc.add(blockIndex[0], blockIndex[1], blockIndex[2]);
            // 岩盤下からはブロックを取得できないため飛ばす
            if (loc.getY() == -1) continue;

            Block locBlock = loc.getBlock();
            if (locBlock.getType().equals(Material.PURPLE_CONCRETE) || locBlock.getType().equals(Material.AIR)) continue;

            shouldDelete = false;
            break;
        }
        return shouldDelete;
    }

    public static void redirectCorrosion() {
        /**
         * 特定Playerに近い部分だけ残してあとは腐食を消滅させる
         */

        if (targetCorrosionBlockList.size() < ConfigManager.integerConfig.get(CommandConst.CONFIG_UPDATE_BLOCK_MAX_NUM)) return;

        Player p = CorrosionManager.getTargetPlayer();
        if (p==null)return;
        Set<String> nextCorrosion = pruningCorrosionTarget(p);

        for (String pos: currentSearchCorrosionBlockList) {
            if (!nextCorrosion.contains(pos)) {
                // 片付けるパターン
                //getBlockFromPosString(pos).setType(Material.AIR);
                //targetCorrosionBlockList.clear();

                // 残すパターン
                getBlockFromPosString(pos).setType(Material.AIR);
            }
        }
        currentSearchCorrosionBlockList.clear();
        currentSearchCorrosionBlockList.addAll(nextCorrosion);
    }

    private static int[][] getCorrosionIndex() {
        /**
         * 腐食の進行方向を返す
         */
        int[][] index;
        index = new int[][]{
                {0, 0, -1}, {0, 0, 1},
                {0, -1, 0}, {0, 1, 0},
                {-1, 0, 0}, {1, 0, 0}};
        return index;
    }

    private static Set<String> pruningCorrosionTarget(Player p){
        Location pLoc = p.getLocation();
        Map<String, Double> distance = new HashMap<>();

        double px = pLoc.getX();
        double py = pLoc.getY();
        double pz = pLoc.getZ();

        for (String pos: currentSearchCorrosionBlockList) {
            int[] bc = getCoordinateFromPosString(pos);
            double dist = Math.sqrt(
                    (px-bc[0])*(px-bc[0]) +
                    (py-bc[1])*(py-bc[1]) +
                    (pz-bc[2])*(pz-bc[2]));
            distance.put(pos, dist);
        }
        // ソート
        List<Map.Entry<String, Integer>> list = new ArrayList(distance.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Set<String> pruningCorrosion = new HashSet<>();
        int max = (int)(Math.min(ConfigManager.integerConfig.get(CommandConst.CONFIG_UPDATE_BLOCK_MAX_NUM), list.size()) * ConfigManager.doubleConfig.get(CommandConst.CONFIG_UPDATE_BLOCK_PRUNING_RATIO));
        for (int i =0;i < max;i++) {
            pruningCorrosion.add(list.get(i).getKey());
        }

        return pruningCorrosion;
    }



    public static Block getBlockFromPosString(String pos){
        String[] loc = pos.split(" ");
        return Bukkit.getWorld(loc[0]).getBlockAt(
                (int)Double.parseDouble(loc[1]),
                (int)Double.parseDouble(loc[2]),
                (int)Double.parseDouble(loc[3])
        );
    }

    public static void updateCurrentCorrosionBlock(){
        currentSearchCorrosionBlockList.addAll(nextSearchCorrosionBlockList);
        nextSearchCorrosionBlockList.clear();
    }

    public static int[] getCoordinateFromPosString(String pos){
        String[] loc = pos.split(" ");
        int [] coordinate = {(int)Double.parseDouble(loc[1]),
                (int)Double.parseDouble(loc[2]),
                (int)Double.parseDouble(loc[3])};
        return coordinate;
    }

    public static String getPosStringFromBlock(Block block){
        String world = block.getWorld().getName();
        Location loc = block.getLocation();
        return world + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ();
    }

    public static boolean isCorrosionBlock(Block block){
        return block.getType().equals(Material.PURPLE_CONCRETE);
    }

    private static boolean isCorrosionTarget(String pos){
        return !currentSearchCorrosionBlockList.contains(pos) &&
                !targetCorrosionBlockList.containsKey(pos) &&
                !nextSearchCorrosionBlockList.contains(pos) &&
                !targetDeleteBlockList.contains(pos);
    }
}