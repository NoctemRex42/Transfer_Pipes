package com.transfer_pipes.blockentity;

import com.transfer_pipes.block.BasePipeBlock;
import com.transfer_pipes.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

public class ItemPipeBlockEntity extends BlockEntity {
    private int tickCounter = 0;
    private static final Set<BlockPos> currentlyProcessing = new HashSet<>();

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_PIPE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;
        if (tickCounter < ModConfig.itemPipeCooldown) return;
        tickCounter = 0;

        transferItems();
    }

    private void transferItems() {
        if (currentlyProcessing.contains(worldPosition)) return;

        Map<Direction, IItemHandler> inputs = new HashMap<>();
        Map<Direction, IItemHandler> outputs = new HashMap<>();

        Set<BlockPos> visited = new HashSet<>();
        gatherNetworkConnections(worldPosition, visited, inputs, outputs);

        if (inputs.isEmpty() || outputs.isEmpty()) return;

        currentlyProcessing.addAll(visited);

        for (IItemHandler input : inputs.values()) {
            for (IItemHandler output : outputs.values()) {
                if (input == output) continue;
                if (transferBetween(input, output)) {
                    break;
                }
            }
        }

        currentlyProcessing.removeAll(visited);
    }

    private void gatherNetworkConnections(BlockPos pos, Set<BlockPos> visited, Map<Direction, IItemHandler> inputs, Map<Direction, IItemHandler> outputs) {
        if (visited.contains(pos)) return;
        visited.add(pos);

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ItemPipeBlockEntity)) return;

        BlockState state = level.getBlockState(pos);

        for (Direction direction : Direction.values()) {
            BasePipeBlock.ConnectionMode mode = state.getValue(BasePipeBlock.getPropertyForDirection(direction));

            if (mode == BasePipeBlock.ConnectionMode.NONE) continue;

            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor == null) continue;

            if (neighbor instanceof ItemPipeBlockEntity && mode == BasePipeBlock.ConnectionMode.PIPE) {
                gatherNetworkConnections(neighborPos, visited, inputs, outputs);
                continue;
            }

            if (!(neighbor instanceof ItemPipeBlockEntity)) {
                neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).ifPresent(handler -> {
                    if (mode == BasePipeBlock.ConnectionMode.OUTPUT && hasItems(handler)) {
                        inputs.put(direction, handler);
                    } else if (mode == BasePipeBlock.ConnectionMode.INPUT && canReceiveItems(handler)) {
                        outputs.put(direction, handler);
                    }
                });
            }
        }
    }

    private boolean transferBetween(IItemHandler source, IItemHandler destination) {
        int maxTransfer = ModConfig.itemPipeTransferRate;

        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stack = source.extractItem(i, maxTransfer, true);
            if (stack.isEmpty()) continue;

            ItemStack remainder = insertItem(destination, stack, false);

            int transferred = stack.getCount() - remainder.getCount();
            if (transferred > 0) {
                source.extractItem(i, transferred, false);
                return true;
            }
        }
        return false;
    }

    private ItemStack insertItem(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < handler.getSlots(); i++) {
            remaining = handler.insertItem(i, remaining, simulate);
            if (remaining.isEmpty()) break;
        }

        return remaining;
    }

    private boolean hasItems(IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.extractItem(i, 1, true).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean canReceiveItems(IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack testStack = new ItemStack(net.minecraft.world.item.Items.STONE);
            if (!handler.insertItem(i, testStack, true).equals(testStack)) {
                return true;
            }
        }
        return false;
    }
}