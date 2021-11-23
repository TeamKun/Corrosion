package net.kunmc.lab.corrosion.game;

import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
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
    public static Set<String> blockList = new HashSet<>();

    // Stringにはブロックの座標を持つ(world名,x軸,y軸,z軸がスペース区切り)

    // 腐敗対象に回すブロックを保持
    //   - currentSearchCorrosionBlockList
    //     - 腐敗ブロックにするか、削除対象にするかの処理対象を格納するリスト
    //   - nextSearchCorrosionBlockList
    //     - currentSearchCorrosionBlockListの次の探索対象を管理するリスト
    //     - 定期的にcurrentSearchCorrosionBlockListに内容を移す
    public static Set<String> currentSearchCorrosionBlockList = new HashSet<>();
    public static Set<String> nextSearchCorrosionBlockList = new HashSet<>();

    // 腐敗対象のブロックの座標を保持
    public static Set<String> targetCorrosionBlockList = new HashSet<>();

    // 削除対象のブロックの座標を保持
    public static Set<String> targetDeleteBlockList = new HashSet<>();
    public static void initBlock(Player p) {
        /**
         * プラグインスタート時の腐食ブロックの取得
         * World全部の腐食ブロックを取得するのは無茶なので、start時点の特定プレイヤーの周囲を探索して腐食ブロックを取得する
         */
        int bx = (int) p.getLocation().getX();
        int bz = (int) p.getLocation().getZ();
        int range = ConfigManager.integerConfig.get(CommandConst.CONFIG_START_RANGE);
        for (int x = -1*range; x<range;x++) {
            for (int y = 0; y<256;y++) {
                for (int z = -1*range; z<range;z++) {
                    Block block = p.getWorld().getBlockAt(bx+x, y, bz+z);
                    if (isCorrosionBlock(block)){
                        currentSearchCorrosionBlockList.add(getPosStringFromBlock(block));
                    }
                }
            }
        }
    }

    // 作るべき関数(インターフェイスでもいいけど規模的には面倒くさいので関数にしておく)
    //   腐食ロジック
    //     全方向への腐食
    //   腐食リセット
    //     特定プレイヤとブロックの距離を算出、ソートして上位何割を取得する

    public static void searchAroundCorrosionBlock(Block block){
        /**
         * 腐食対象を探すロジック
         *  - 腐食対象のブロックの上下左右前後に変換可能なブロックがあれば変換対象として追加
         *  - 腐食対象のブロックが関与できるブロックがなければ削除対象として追加
         */

        int index[][] = {
                {0,0,-1}, {0,0,1},
                {0,-1,0}, {0,1,0},
                {-1,0,0}, {1,0,0}};
        for (int[] blockIndex: index) {
            Location loc = block.getLocation();
            loc.add(blockIndex[0], blockIndex[1], blockIndex[2]);
            Block locBlock = loc.getBlock();

            // 腐敗させないブロック
            String pos = getPosStringFromBlock(locBlock);
            if (locBlock.getType().equals(Material.AIR) || !isCorrosionTarget(pos)) continue;

            targetCorrosionBlockList.add(pos);
            nextSearchCorrosionBlockList.add(pos);
        }
        targetDeleteBlockList.add(getPosStringFromBlock(block));
    }

    public static void redirectCorrosion() {
        /**
         * 特定Playerに近い部分だけ残してあとは腐食を消滅させる
         */
        Player p = CorrosionManager.getTargetPlayer();
        Set<String> nextCorrosion = pruningCorrosionTarget(p);
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
        for (String pos: nextSearchCorrosionBlockList) {
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
        // TODO: 後で更新
        for (int i =0;i < 1000;i++) {
            pruningCorrosion.add(list.get(i).getKey());
        }

        return pruningCorrosion;
    }




//    public static boolean updateBlock(Block block, Set<String> nextList){
//        /**
//         * 腐食を進めるメインロジック
//         * 腐食対象のブロックの上下左右前後に変換可能なブロックがあれば変換
//         * 腐食対象のブロックが関与できるブロックがなければ削除(Airに変換)
//         * 削除された場合はfalseを返す
//         */
//
//        int index[][] = {
//                {0,0,-1}, {0,0,1},
//                {0,-1,0}, {0,1,0},
//                {-1,0,0}, {1,0,0}};
//        boolean updated = false;
//        for (int[] blockIndex: index) {
//            Location loc = block.getLocation();
//            Vector v = new Vector(blockIndex[0], blockIndex[1], blockIndex[2]);
//            loc.add(v);
//            if (!loc.getBlock().getType().equals(Material.PURPLE_CONCRETE) &&
//                    !loc.getBlock().getType().equals(Material.AIR)) {
//                loc.getBlock().setType(Material.PURPLE_CONCRETE);
//                updated = true;
//                nextList.add(getPosStringFromBlock(loc.getBlock()));
//            }
//        }
//        block.setBlockData(Material.AIR.createBlockData());
//        return updated;
//    }

    public static Block getBlockFromPosString(String pos){
        String[] loc = pos.split(" ");
        return Bukkit.getWorld(loc[0]).getBlockAt(
                (int)Double.parseDouble(loc[1]),
                (int)Double.parseDouble(loc[2]),
                (int)Double.parseDouble(loc[3])
        );
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
                !targetCorrosionBlockList.contains(pos) &&
                !nextSearchCorrosionBlockList.contains(pos) &&
                !targetDeleteBlockList.contains(pos);
    }
}
