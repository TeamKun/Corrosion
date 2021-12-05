package net.kunmc.lab.corrosion.event;

import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
import net.kunmc.lab.corrosion.game.CorrosionBlockManager;
import net.kunmc.lab.corrosion.game.CorrosionManager;
import net.kunmc.lab.corrosion.game.GameManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class CorrosionEvent implements Listener {
    @EventHandler
    public void onPlayerPortalEvent(PlayerChangedWorldEvent event) {
        if (!GameManager.isRunning() && !GameManager.isPause())
            return;

        Player p = CorrosionManager.getTargetPlayer();
        if (p == null || !event.getPlayer().getName().equals(p.getName())) return;

        // Taskの処理とぶつからないようにここでは設定だけ変える
        CorrosionBlockManager.saveFlag = true;
        CorrosionBlockManager.fromWorld = event.getFrom().getName();
        CorrosionBlockManager.toWorld = event.getPlayer().getWorld().getName();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!GameManager.isRunning())
            return;

        Block block = e.getBlock();
        Player p = CorrosionManager.getTargetPlayer();
        // 特定プレイヤーがそのワールドにいる場合のみ探索対象に指定
        if (p == null || !p.getWorld().equals(block.getWorld())) return;

        if (CorrosionBlockManager.isCorrosionBlock(block)) {
            CorrosionBlockManager.nextSearchCorrosionBlockList.add(CorrosionBlockManager.getPosStringFromBlock(block));
        }
    }
}
