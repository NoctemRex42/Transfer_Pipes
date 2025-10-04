package com.transfer_pipes.integration.jei;

import com.transfer_pipes.TransferPipes;
import com.transfer_pipes.TransferPipes;
import com.transfer_pipes.item.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(TransferPipes.MODID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Add info descriptions for each pipe type
        registration.addIngredientInfo(
                new ItemStack(ModItems.ITEM_PIPE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.transfer_pipes.item_pipe.description")
        );

        registration.addIngredientInfo(
                new ItemStack(ModItems.FLUID_PIPE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.transfer_pipes.fluid_pipe.description")
        );

        registration.addIngredientInfo(
                new ItemStack(ModItems.ENERGY_PIPE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.transfer_pipes.energy_pipe.description")
        );
    }
}