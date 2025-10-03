package com.transfer_pipes.item;

import com.transfer_pipes.Transfer_pipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Transfer_pipes.MODID);

    public static final RegistryObject<CreativeModeTab> TRANSFER_PIPES_TAB = CREATIVE_MODE_TABS.register("transfer_pipes_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.transfer_pipes"))
                    .icon(() -> new ItemStack(ModItems.ITEM_PIPE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ITEM_PIPE.get());
                        output.accept(ModItems.FLUID_PIPE.get());
                        output.accept(ModItems.ENERGY_PIPE.get());
                    })
                    .build());
}