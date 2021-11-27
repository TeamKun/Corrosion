package net.kunmc.lab.corrosion.util;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.block.CapturedBlockState;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;

import java.lang.reflect.Field;

public class Utils {
    public static boolean setTypeAndDataWithoutLight(World world, BlockPosition blockposition, IBlockData iblockdata, int i, int j) {
        if (world.captureTreeGeneration) {
            CapturedBlockState capturedBlockState = world.capturedBlockStates.get(blockposition);
            if (capturedBlockState == null) {
                capturedBlockState = CapturedBlockState.getTreeBlockState(world, blockposition, i);
                world.capturedBlockStates.put(blockposition.immutableCopy(), capturedBlockState);
            }

            capturedBlockState.setData(iblockdata);
            return true;
        } else if (world.isOutsideWorld(blockposition)) {
            return false;
        } else {
            net.minecraft.server.v1_16_R3.Chunk chunk = world.getChunkAtWorldCoords(blockposition);
            boolean captured = false;
            if (world.captureBlockStates && !world.capturedBlockStates.containsKey(blockposition)) {
                CapturedBlockState blockstate = CapturedBlockState.getBlockState(world, blockposition, i);
                world.capturedBlockStates.put(blockposition.immutableCopy(), blockstate);
                captured = true;
            }

            IBlockData iblockdata1 = chunk.setType(blockposition, iblockdata, (i & 64) != 0, (i & 1024) == 0);
            if (iblockdata1 == null) {
                if (world.captureBlockStates && captured) {
                    world.capturedBlockStates.remove(blockposition);
                }

                return false;
            } else {
                IBlockData iblockdata2 = world.getType(blockposition);
                if (!world.captureBlockStates) {
                    try {
                        world.notifyAndUpdatePhysics(blockposition, chunk, iblockdata1, iblockdata, iblockdata2, i, j);
                    } catch (StackOverflowError var10) {
                        World.lastPhysicsProblem = new BlockPosition(blockposition);
                        throw var10;
                    }
                }
                return true;
            }
        }
    }

    public static boolean setTypeAndData(Block block, BlockData newBlockData, boolean applyPhysics) {
        CraftBlock craftBlock = ((CraftBlock) block);
        IBlockData blockData = ((CraftBlockData) newBlockData).getState();
        GeneratorAccess world = null;
        try {
            Field fworld = CraftBlock.class.getDeclaredField("world");
            fworld.setAccessible(true);
            world = (GeneratorAccess) fworld.get(craftBlock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (!blockData.isAir() && blockData.getBlock() instanceof BlockTileEntity && blockData.getBlock() != craftBlock.getNMS().getBlock()) {
            if (craftBlock.getWorld() instanceof net.minecraft.server.v1_16_R3.World) {
                ((net.minecraft.server.v1_16_R3.World) craftBlock.getWorld()).removeTileEntity(craftBlock.getPosition());
            } else {
                setTypeAndDataWithoutLight(craftBlock.getCraftWorld().getHandle(), craftBlock.getPosition(), Blocks.AIR.getBlockData(), 0, 0);
            }
        }

        if (applyPhysics) {
            return setTypeAndDataWithoutLight((World) world, craftBlock.getPosition(), blockData, 3, 0);
        } else {
            IBlockData old = world.getType(craftBlock.getPosition());
            boolean success = setTypeAndDataWithoutLight((World) world, craftBlock.getPosition(), blockData, 1042, 0);
            if (success) {
                world.getMinecraftWorld().notify(craftBlock.getPosition(), old, blockData, 3);
            }

            return success;
        }
    }
}
