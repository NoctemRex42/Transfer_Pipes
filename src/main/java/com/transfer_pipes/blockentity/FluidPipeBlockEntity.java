package com.transfer_pipes.blockentity;

import com.transfer_pipes.block.BasePipeBlock;
import com.transfer_pipes.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

import static com.transfer_pipes.block.BasePipeBlock.*;

public class FluidPipeBlockEntity extends BlockEntity {
    private int tickCounter = 0;

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
        BlockState state = getBlockState();
        List<IFluidHandler> inputs = new ArrayList<>();
        List<IFluidHandler> outputs = new ArrayList<>();

        // Gather all connected fluid handlers
        for (Direction direction : Direction.values()) {
            if (!(boolean) state.getValue(getPropertyForDirection(direction))) continue;

            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor == null) continue;

            // Skip other pipes
            if (neighbor instanceof FluidPipeBlockEntity) continue;

            neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(handler -> {
                // Check if this is an input or output
                if (hasFluid(handler)) {
                    inputs.add(handler);
                }
                if (canReceiveFluid(handler)) {
                    outputs.add(handler);
                }
            });
        }

        // Transfer from each input to each output
        for (IFluidHandler input : inputs) {
            for (IFluidHandler output : outputs) {
                if (input == output) continue;
                transferBetween(input, output);
            }
        }
    }

    private void transferBetween(IFluidHandler source, IFluidHandler destination) {
        int maxTransfer = ModConfig.fluidPipeTransferRate;

        // Try to drain fluid from source
        FluidStack drained = source.drain(maxTransfer, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty()) return;

        // Try to fill destination
        int filled = destination.fill(drained, IFluidHandler.FluidAction.SIMULATE);
        if (filled <= 0) return;

        // Perform actual transfer
        FluidStack actualDrain = source.drain(filled, IFluidHandler.FluidAction.EXECUTE);
        destination.fill(actualDrain, IFluidHandler.FluidAction.EXECUTE);
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


            private static BooleanProperty getPropertyForDirection(Direction direction) {
                return switch (direction) {
                    case NORTH -> BasePipeBlock.NORTH;
                    case SOUTH -> BasePipeBlock.SOUTH;
                    case EAST  -> BasePipeBlock.EAST;
                    case WEST  -> BasePipeBlock.WEST;
                    case UP    -> BasePipeBlock.UP;
                    case DOWN  -> BasePipeBlock.DOWN;
                };
            }

        }