package com.transfer_pipes.item;

import com.transfer_pipes.Transfer_pipes;
import com.transfer_pipes.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Transfer_pipes.MODID);

    public static final RegistryObject<Item> ITEM_PIPE = ITEMS.register("item_pipe",
            () -> new BlockItem(ModBlocks.ITEM_PIPE.get(), new Item.Properties()));

    public static final RegistryObject<Item> FLUID_PIPE = ITEMS.register("fluid_pipe",
            () -> new BlockItem(ModBlocks.FLUID_PIPE.get(), new Item.Properties()));

    public static final RegistryObject<Item> ENERGY_PIPE = ITEMS.register("energy_pipe",
            () -> new BlockItem(ModBlocks.ENERGY_PIPE.get(), new Item.Properties()));
}