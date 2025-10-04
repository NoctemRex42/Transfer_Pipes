package com.transfer_pipes.blockentity;

import com.transfer_pipes.block.BasePipeBlock;
import com.transfer_pipes.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.*;

public class EnergyPipeBlockEntity extends BlockEntity {
    private int tickCounter = 0;
    private static final Set<BlockPos> currentlyProcessing = new HashSet<>();

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
        if (currentlyProcessing.contains(worldPosition)) return;

        Map<Direction, IEnergyStorage> inputs = new HashMap<>();
        Map<Direction, IEnergyStorage> outputs = new HashMap<>();

        Set<BlockPos> visited = new HashSet<>();
        gatherNetworkConnections(worldPosition, visited, inputs, outputs);

        if (inputs.isEmpty() || outputs.isEmpty()) return;

        currentlyProcessing.addAll(visited);

        for (IEnergyStorage input : inputs.values()) {
            for (IEnergyStorage output : outputs.values()) {
                if (input == output) continue;
                if (transferBetween(input, output)) {
                    break;
                }
            }
        }

        currentlyProcessing.removeAll(visited);
    }

    private void gatherNetworkConnections(BlockPos pos, Set<BlockPos> visited, Map<Direction, IEnergyStorage> inputs, Map<Direction, IEnergyStorage> outputs) {
        if (visited.contains(pos)) return;
        visited.add(pos);

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof EnergyPipeBlockEntity)) return;

        BlockState state = level.getBlockState(pos);

        for (Direction direction : Direction.values()) {
            BasePipeBlock.ConnectionMode mode = state.getValue(BasePipeBlock.getPropertyForDirection(direction));

            if (mode == BasePipeBlock.ConnectionMode.NONE) continue;

            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor == null) continue;

            if (neighbor instanceof EnergyPipeBlockEntity && mode == BasePipeBlock.ConnectionMode.PIPE) {
                gatherNetworkConnections(neighborPos, visited, inputs, outputs);
                continue;
            }

            if (!(neighbor instanceof EnergyPipeBlockEntity)) {
                neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(handler -> {
                    if (mode == BasePipeBlock.ConnectionMode.OUTPUT && canExtractEnergy(handler)) {
                        inputs.put(direction, handler);
                    } else if (mode == BasePipeBlock.ConnectionMode.INPUT && canReceiveEnergy(handler)) {
                        outputs.put(direction, handler);
                    }
                });
            }
        }
    }

    private boolean transferBetween(IEnergyStorage source, IEnergyStorage destination) {
        int maxTransfer = ModConfig.energyPipeTransferRate;

        int extracted = source.extractEnergy(maxTransfer, true);
        if (extracted <= 0) return false;

        int received = destination.receiveEnergy(extracted, true);
        if (received <= 0) return false;

        int actualExtracted = source.extractEnergy(received, false);
        destination.receiveEnergy(actualExtracted, false);
        return true;
    }

    private boolean canExtractEnergy(IEnergyStorage storage) {
        return storage.canExtract() && storage.extractEnergy(1, true) > 0;
    }

    private boolean canReceiveEnergy(IEnergyStorage storage) {
        return storage.canReceive() && storage.receiveEnergy(1, true) > 0;
    }
}