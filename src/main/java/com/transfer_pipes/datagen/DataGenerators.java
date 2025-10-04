package com.transfer_pipes.datagen;

import com.transfer_pipes.TransferPipes;
import com.transfer_pipes.block.ModBlocks;
import com.transfer_pipes.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = TransferPipes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), new ModLootTableProvider(packOutput, lookupProvider));
    }

    private static class ModRecipeProvider extends RecipeProvider {
        public ModRecipeProvider(PackOutput output) {
            super(output);
        }

        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> writer) {
            // Item Pipe Recipe
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ITEM_PIPE.get(), 8)
                    .pattern("IGI")
                    .pattern("GCG")
                    .pattern("IGI")
                    .define('I', Items.IRON_INGOT)
                    .define('G', Items.GLASS)
                    .define('C', Items.CHEST)
                    .unlockedBy("has_iron", has(Items.IRON_INGOT))
                    .save(writer);

            // Fluid Pipe Recipe
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FLUID_PIPE.get(), 8)
                    .pattern("IGI")
                    .pattern("GBG")
                    .pattern("IGI")
                    .define('I', Items.IRON_INGOT)
                    .define('G', Items.GLASS)
                    .define('B', Items.BUCKET)
                    .unlockedBy("has_iron", has(Items.IRON_INGOT))
                    .save(writer);

            // Energy Pipe Recipe
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ENERGY_PIPE.get(), 8)
                    .pattern("IGI")
                    .pattern("GRG")
                    .pattern("IGI")
                    .define('I', Items.IRON_INGOT)
                    .define('G', Items.GLASS)
                    .define('R', Items.REDSTONE_BLOCK)
                    .unlockedBy("has_iron", has(Items.IRON_INGOT))
                    .save(writer);
        }
    }

    private static class ModLootTableProvider extends LootTableProvider {
        public ModLootTableProvider(PackOutput output, java.util.concurrent.CompletableFuture<net.minecraft.core.HolderLookup.Provider> provider) {
            super(output, Set.of(), List.of(
                    new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)
            ));
        }

        private static class ModBlockLootTables extends BlockLootSubProvider {
            protected ModBlockLootTables() {
                super(Set.of(), FeatureFlags.REGISTRY.allFlags());
            }

            @Override
            protected void generate() {
                dropSelf(ModBlocks.ITEM_PIPE.get());
                dropSelf(ModBlocks.FLUID_PIPE.get());
                dropSelf(ModBlocks.ENERGY_PIPE.get());
            }

            @Override
            protected Iterable<Block> getKnownBlocks() {
                return ModBlocks.BLOCKS.getEntries().stream()
                        .map(RegistryObject::get)
                        ::iterator;
            }
        }
    }
}