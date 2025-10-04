package com.transfer_pipes.blockentity;

import com.transfer_pipes.block.BasePipeBlock;
import com.transfer_pipes.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

public class FluidPipeBlockEntity extends BlockEntity {
    private int tickCounter = 0;
    private static final Set<BlockPos> currentlyProcessing = new HashSet<>();

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;
        if (tickCounter < ModConfig.fluidPipeCooldown) return;
        tickCounter = 0;

        transferFluids();
    }

    private void transferFluids() {
        if (currentlyProcessing.contains(worldPosition)) return;

        Map<Direction, IFluidHandler> inputs = new HashMap<>();
        Map<Direction, IFluidHandler> outputs = new HashMap<>();

        Set<BlockPos> visited = new HashSet<>();
        gatherNetworkConnections(worldPosition, visited, inputs, outputs);

        if (inputs.isEmpty() || outputs.isEmpty()) return;

        currentlyProcessing.addAll(visited);

        for (IFluidHandler input : inputs.values()) {
            for (IFluidHandler output : outputs.values()) {
                if (input == output) continue;
                if (transferBetween(input, output)) {
                    break;
                }
            }
        }

        currentlyProcessing.removeAll(visited);
    }

    private void gatherNetworkConnections(BlockPos pos, Set<BlockPos> visited, Map<Direction, IFluidHandler> inputs, Map<Direction, IFluidHandler> outputs) {
        if (visited.contains(pos)) return;
        visited.add(pos);

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FluidPipeBlockEntity)) return;

        BlockState state = level.getBlockState(pos);

        for (Direction direction : Direction.values()) {
            BasePipeBlock.ConnectionMode mode = state.getValue(BasePipeBlock.getPropertyForDirection(direction));

            if (mode == BasePipeBlock.ConnectionMode.NONE) continue;

            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor == null) continue;

            if (neighbor instanceof FluidPipeBlockEntity && mode == BasePipeBlock.ConnectionMode.PIPE) {
                gatherNetworkConnections(neighborPos, visited, inputs, outputs);
                continue;
            }

            if (!(neighbor instanceof FluidPipeBlockEntity)) {
                neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(handler -> {
                    if (mode == BasePipeBlock.ConnectionMode.OUTPUT && hasFluid(handler)) {
                        inputs.put(direction, handler);
                    } else if (mode == BasePipeBlock.ConnectionMode.INPUT && canReceiveFluid(handler)) {
                        outputs.put(direction, handler);
                    }
                });
            }
        }
    }

    private boolean transferBetween(IFluidHandler source, IFluidHandler destination) {
        int maxTransfer = ModConfig.fluidPipeTransferRate;

        FluidStack drained = source.drain(maxTransfer, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty()) return false;

        int filled = destination.fill(drained, IFluidHandler.FluidAction.SIMULATE);
        if (filled <= 0) return false;

        FluidStack actualDrain = source.drain(filled, IFluidHandler.FluidAction.EXECUTE);
        destination.fill(actualDrain, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private boolean hasFluid(IFluidHandler handler) {
        for (int i = 0; i < handler.getTanks(); i++) {
            if (!handler.getFluidInTank(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean canReceiveFluid(IFluidHandler handler) {
        for (int i = 0; i < handler.getTanks(); i++) {
            if (handler.getTankCapacity(i) > handler.getFluidInTank(i).getAmount()) {
                return true;
            }
        }
        return false;
    }
}