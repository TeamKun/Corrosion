package net.kunmc.lab.corrosion.game;

import org.bukkit.entity.Player;

public class GameManager {
    // 動作中のモード保持
    public static GameMode runningMode = GameMode.MODE_NEUTRAL;


    public static void controller(GameMode runningMode, Player p) {
        // モードを設定
        GameManager.runningMode = runningMode;

        switch (runningMode) {
            case MODE_START:
                CorrosionBlockManager.initBlock(p);
                CorrosionManager.updateBlock();
                break;
            case MODE_NEUTRAL:
                CorrosionManager.stopUpdateBlock();
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


}