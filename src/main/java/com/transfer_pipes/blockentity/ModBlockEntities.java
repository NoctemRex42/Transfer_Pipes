package com.transfer_pipes.blockentity;

import com.transfer_pipes.Transfer_pipes;
import com.transfer_pipes.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Transfer_pipes.MODID);

    public static final RegistryObject<BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE = BLOCK_ENTITIES.register("item_pipe",
            () -> BlockEntityType.Builder.of(ItemPipeBlockEntity::new, ModBlocks.ITEM_PIPE.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE = BLOCK_ENTITIES.register("fluid_pipe",
            () -> BlockEntityType.Builder.of(FluidPipeBlockEntity::new, ModBlocks.FLUID_PIPE.get()).build(null));

    public static final RegistryObject<BlockEntityType<EnergyPipeBlockEntity>> ENERGY_PIPE = BLOCK_ENTITIES.register("energy_pipe",
            () -> BlockEntityType.Builder.of(EnergyPipeBlockEntity::new, ModBlocks.ENERGY_PIPE.get()).build(null));
}