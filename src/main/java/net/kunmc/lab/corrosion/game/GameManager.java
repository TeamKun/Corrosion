package net.kunmc.lab.corrosion.game;

public class GameManager {
    // 動作中のモード保持
    public static GameMode runningMode = GameMode.MODE_NEUTRAL;

    public static void controller(GameMode runningMode) {
        // モードを設定
        GameManager.runningMode = runningMode;

        switch (runningMode) {
            case MODE_START:
                String boardName = "Cooties";
                break;
            case MODE_NEUTRAL:
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