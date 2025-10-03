package com.transfer_pipes.blockentity;

import com.transfer_pipes.block.BasePipeBlock;
import com.transfer_pipes.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class ItemPipeBlockEntity extends BlockEntity {
    private int tickCounter = 0;

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
        BlockState state = getBlockState();
        List<IItemHandler> inputs = new ArrayList<>();
        List<IItemHandler> outputs = new ArrayList<>();

        // Gather all connected inventories
        for (Direction direction : Direction.values()) {
            if (!state.getValue(getPropertyForDirection(direction))) continue;

            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor == null) continue;

            // Skip other pipes
            if (neighbor instanceof ItemPipeBlockEntity) continue;

            neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).ifPresent(handler -> {
                // Check if this is an input or output
                if (hasItems(handler)) {
                    inputs.add(handler);
                }
                if (canReceiveItems(handler)) {
                    outputs.add(handler);
                }
            });
        }

        // Transfer from each input to each output
        for (IItemHandler input : inputs) {
            for (IItemHandler output : outputs) {
                if (input == output) continue;
                transferBetween(input, output);
            }
        }
    }

    private void transferBetween(IItemHandler source, IItemHandler destination) {
        int maxTransfer = ModConfig.itemPipeTransferRate;

        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stack = source.extractItem(i, maxTransfer, true);
            if (stack.isEmpty()) continue;

            // Try to insert into destination
            ItemStack remainder = insertItem(destination, stack, false);

            // Extract what was successfully inserted
            int transferred = stack.getCount() - remainder.getCount();
            if (transferred > 0) {
                source.extractItem(i, transferred, false);
                return; // Only transfer one stack per tick to simulate travel
            }
        }
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

    private static BooleanProperty getPropertyForDirection(Direction direction)
    {
        return switch (direction) {
            case NORTH -> BasePipeBlock.NORTH;
            case SOUTH -> BasePipeBlock.SOUTH;
            case EAST -> BasePipeBlock.EAST;
            case WEST -> BasePipeBlock.WEST;
            case UP -> BasePipeBlock.UP;
            case DOWN -> BasePipeBlock.DOWN;
        };
    }
}