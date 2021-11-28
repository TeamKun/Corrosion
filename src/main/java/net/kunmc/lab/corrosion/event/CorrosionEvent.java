package net.kunmc.lab.corrosion.event;

import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
import net.kunmc.lab.corrosion.game.CorrosionBlockManager;
import net.kunmc.lab.corrosion.game.GameManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class CorrosionEvent implements Listener {
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!GameManager.isRunning())
            return;

        Block block = e.getBlock();
        if (CorrosionBlockManager.isCorrosionBlock(block)) {
            CorrosionBlockManager.nextSearchCorrosionBlockList.add(CorrosionBlockManager.getPosStringFromBlock(block));
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        if (!GameManager.isRunning() || !ConfigManager.booleanConfig.get(CommandConst.CONFIG_CORROSION_DEATH))
            return;
        Location targetLoc = e.getPlayer().getLocation().add(0, -0.5, 0);
        if (CorrosionBlockManager.isCorrosionBlock(targetLoc.getBlock())) {
            e.getPlayer().damage(10000);
        }
    }
}
