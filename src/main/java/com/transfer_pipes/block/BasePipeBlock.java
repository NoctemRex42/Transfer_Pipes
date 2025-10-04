package com.transfer_pipes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class BasePipeBlock extends Block implements EntityBlock {

    // Connection mode for each direction
    public enum ConnectionMode implements StringRepresentable {
        NONE("none", "Disabled"),
        PIPE("pipe", "Pipe"),
        INPUT("input", "Input"),
        OUTPUT("output", "Output");

        private final String name;
        private final String displayName;

        ConnectionMode(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ConnectionMode next() {
            return switch (this) {
                case NONE -> INPUT;
                case INPUT -> OUTPUT;
                case OUTPUT -> NONE;
                case PIPE -> PIPE; // PIPE never cycles - it's automatic only
            };
        }

        public boolean isConnected() {
            return this != NONE;
        }

        public boolean isPlayerConfigurable() {
            return this != PIPE;
        }
    }

    // Connection properties for each direction
    public static final EnumProperty<ConnectionMode> NORTH = EnumProperty.create("north", ConnectionMode.class);
    public static final EnumProperty<ConnectionMode> SOUTH = EnumProperty.create("south", ConnectionMode.class);
    public static final EnumProperty<ConnectionMode> EAST = EnumProperty.create("east", ConnectionMode.class);
    public static final EnumProperty<ConnectionMode> WEST = EnumProperty.create("west", ConnectionMode.class);
    public static final EnumProperty<ConnectionMode> UP = EnumProperty.create("up", ConnectionMode.class);
    public static final EnumProperty<ConnectionMode> DOWN = EnumProperty.create("down", ConnectionMode.class);

    // VoxelShapes for pipe rendering
    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape NORTH_SHAPE = Block.box(5, 5, 0, 11, 11, 5);
    private static final VoxelShape SOUTH_SHAPE = Block.box(5, 5, 11, 11, 11, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(11, 5, 5, 16, 11, 11);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 5, 5, 5, 11, 11);
    private static final VoxelShape UP_SHAPE = Block.box(5, 11, 5, 11, 16, 11);
    private static final VoxelShape DOWN_SHAPE = Block.box(5, 0, 5, 11, 5, 11);

    public BasePipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, ConnectionMode.NONE)
                .setValue(SOUTH, ConnectionMode.NONE)
                .setValue(EAST, ConnectionMode.NONE)
                .setValue(WEST, ConnectionMode.NONE)
                .setValue(UP, ConnectionMode.NONE)
                .setValue(DOWN, ConnectionMode.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return defaultBlockState()
                .setValue(NORTH, getConnectionModeForDirection(level, pos, Direction.NORTH))
                .setValue(SOUTH, getConnectionModeForDirection(level, pos, Direction.SOUTH))
                .setValue(EAST, getConnectionModeForDirection(level, pos, Direction.EAST))
                .setValue(WEST, getConnectionModeForDirection(level, pos, Direction.WEST))
                .setValue(UP, getConnectionModeForDirection(level, pos, Direction.UP))
                .setValue(DOWN, getConnectionModeForDirection(level, pos, Direction.DOWN));
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockState newState = state;

            for (Direction direction : Direction.values()) {
                ConnectionMode currentMode = state.getValue(getPropertyForDirection(direction));
                ConnectionMode autoMode = getConnectionModeForDirection(level, pos, direction);

                // Only auto-update if currently set to NONE or PIPE
                if (currentMode == ConnectionMode.NONE || currentMode == ConnectionMode.PIPE) {
                    newState = newState.setValue(getPropertyForDirection(direction), autoMode);
                }
            }

            if (newState != state) {
                level.setBlock(pos, newState, 3);
            }
        }
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape shape = CORE;

        if (state.getValue(NORTH).isConnected()) shape = Shapes.or(shape, NORTH_SHAPE);
        if (state.getValue(SOUTH).isConnected()) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (state.getValue(EAST).isConnected()) shape = Shapes.or(shape, EAST_SHAPE);
        if (state.getValue(WEST).isConnected()) shape = Shapes.or(shape, WEST_SHAPE);
        if (state.getValue(UP).isConnected()) shape = Shapes.or(shape, UP_SHAPE);
        if (state.getValue(DOWN).isConnected()) shape = Shapes.or(shape, DOWN_SHAPE);

        return shape;
    }

    private ConnectionMode getConnectionModeForDirection(Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);

        // Check if neighbor is same type of pipe
        if (isSamePipeType(neighborState.getBlock())) {
            return ConnectionMode.PIPE;
        }

        // Check if can connect to block capability
        if (canConnectTo(level, pos, direction)) {
            return ConnectionMode.INPUT; // Default to input for new connections
        }

        return ConnectionMode.NONE;
    }

    protected abstract boolean canConnectTo(Level level, BlockPos pos, Direction direction);

    protected boolean isSamePipeType(Block block) {
        return block.getClass() == this.getClass();
    }

    public ConnectionMode getConnectionMode(BlockState state, Direction direction) {
        return state.getValue(getPropertyForDirection(direction));
    }

    public BlockState setConnectionMode(BlockState state, Direction direction, ConnectionMode mode) {
        return state.setValue(getPropertyForDirection(direction), mode);
    }

    public static EnumProperty<ConnectionMode> getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
}