package com.transfer_pipes.blockentity;

import com.transfer_pipes.block.BasePipeBlock;
import com.transfer_pipes.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class EnergyPipeBlockEntity extends BlockEntity {
    private int tickCounter = 0;

    public EnergyPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_PIPE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;
        if (tickCounter < ModConfig.energyPipeCooldown) return;
        tickCounter = 0;

        transferEnergy();
    }

    private void transferEnergy() {
        BlockState state = getBlockState();
        List<IEnergyStorage> inputs = new ArrayList<>();
        List<IEnergyStorage> outputs = new ArrayList<>();

        // Gather all connected energy handlers
        for (Direction direction : Direction.values()) {
            if (!state.getValue(getPropertyForDirection(direction))) continue;

            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor == null) continue;

            // Skip other pipes
            if (neighbor instanceof EnergyPipeBlockEntity) continue;

            neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(handler -> {
                // Check if this is an input or output
                if (canExtractEnergy(handler)) {
                    inputs.add(handler);
                }
                if (canReceiveEnergy(handler)) {
                    outputs.add(handler);
                }
            });
        }

        // Transfer from each input to each output
        for (IEnergyStorage input : inputs) {
            for (IEnergyStorage output : outputs) {
                if (input == output) continue;
                transferBetween(input, output);
            }
        }
    }

    private void transferBetween(IEnergyStorage source, IEnergyStorage destination) {
        int maxTransfer = ModConfig.energyPipeTransferRate;

        // Try to extract from source
        int extracted = source.extractEnergy(maxTransfer, true);
        if (extracted <= 0) return;

        // Try to insert into destination
        int received = destination.receiveEnergy(extracted, true);
        if (received <= 0) return;

        // Perform actual transfer
        int actualExtracted = source.extractEnergy(received, false);
        destination.receiveEnergy(actualExtracted, false);
    }

    private boolean canExtractEnergy(IEnergyStorage storage) {
        return storage.canExtract() && storage.extractEnergy(1, true) > 0;
    }

    private boolean canReceiveEnergy(IEnergyStorage storage) {
        return storage.canReceive() && storage.receiveEnergy(1, true) > 0;
    }

    private static com.transfer_pipes.block.BasePipeBlock.BooleanProperty getPropertyForDirection(Direction direction) {
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