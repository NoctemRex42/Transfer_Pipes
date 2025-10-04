package com.transfer_pipes.item;

import com.transfer_pipes.block.BasePipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchItem extends Item {

    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        Direction face = context.getClickedFace();

        if (!(state.getBlock() instanceof BasePipeBlock)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && player != null) {
            BasePipeBlock pipe = (BasePipeBlock) state.getBlock();

            if (player.isShiftKeyDown()) {
                // Shift + Right Click: Cycle connection mode for the clicked face
                BasePipeBlock.ConnectionMode currentMode = pipe.getConnectionMode(state, face);

                // Don't allow cycling if it's a PIPE connection (automatic only)
                if (!currentMode.isPlayerConfigurable()) {
                    player.displayClientMessage(
                            Component.literal(face.getName() + " side is a pipe connection (automatic)"),
                            true
                    );
                    return InteractionResult.SUCCESS;
                }

                BasePipeBlock.ConnectionMode newMode = currentMode.next();

                BlockState newState = pipe.setConnectionMode(state, face, newMode);
                level.setBlock(pos, newState, 3);

                player.displayClientMessage(
                        Component.literal(face.getName() + " side set to: " + newMode.getDisplayName()),
                        true
                );
            } else {
                // Right Click: Display current mode for the clicked face
                BasePipeBlock.ConnectionMode mode = pipe.getConnectionMode(state, face);
                String displayText = mode == BasePipeBlock.ConnectionMode.PIPE
                        ? face.getName() + " side: Pipe (automatic)"
                        : face.getName() + " side: " + mode.getDisplayName();
                player.displayClientMessage(
                        Component.literal(displayText),
                        true
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}