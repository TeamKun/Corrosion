package net.kunmc.lab.corrosion.event;

import net.kunmc.lab.corrosion.Corrosion;
import net.kunmc.lab.corrosion.command.CommandConst;
import net.kunmc.lab.corrosion.config.ConfigManager;
import net.kunmc.lab.corrosion.game.CorrosionBlockManager;
import net.kunmc.lab.corrosion.game.CorrosionManager;
import net.kunmc.lab.corrosion.game.GameManager;
import org.bukkit.Location;
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
        if (p == null || !p.getName().equals(ConfigManager.stringConfig.get(CommandConst.CONFIG_PLAYER))) return;

        String fromWorld = event.getFrom().getName();
        String toWorld = event.getPlayer().getWorld().getName();

        CorrosionBlockManager.saveFlag = true;
        CorrosionBlockManager.fromWorld = event.getFrom().getName();
        CorrosionBlockManager.toWorld = event.getPlayer().getWorld().getName();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!GameManager.isRunning())
            return;

        Block block = e.getBlock();
        if (CorrosionBlockManager.isCorrosionBlock(block)) {
            CorrosionBlockManager.nextSearchCorrosionBlockList.add(CorrosionBlockManager.getPosStringFromBlock(block));
        }
    }
}
