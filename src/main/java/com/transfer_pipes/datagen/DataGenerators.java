package com.transfer_pipes.datagen;

import com.transfer_pipes.Transfer_pipes;
import com.transfer_pipes.block.ModBlocks;
import com.transfer_pipes.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Transfer_pipes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));
        generator.addProvider(event.includeServer(), new ModLootTableProvider(packOutput));
    }

    private static class ModRecipeProvider extends RecipeProvider {
        public ModRecipeProvider(PackOutput output) {
            super(output);
        }

        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
            // Item Pipe Recipe
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ITEM_PIPE.get(), 8)
                    .pattern("IGI")
                    .pattern("GCG")
                    .pattern("IGI")
                    .define('I', Items.IRON_INGOT)
                    .define('G', Items.GLASS)
                    .define('C', Items.CHEST)
                    .unlockedBy("has_iron", has(Items.IRON_INGOT))
                    .save(consumer);

            // Fluid Pipe Recipe
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FLUID_PIPE.get(), 8)
                    .pattern("IGI")
                    .pattern("GBG")
                    .pattern("IGI")
                    .define('I', Items.IRON_INGOT)
                    .define('G', Items.GLASS)
                    .define('B', Items.BUCKET)
                    .unlockedBy("has_iron", has(Items.IRON_INGOT))
                    .save(consumer);

            // Energy Pipe Recipe
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ENERGY_PIPE.get(), 8)
                    .pattern("IGI")
                    .pattern("GRG")
                    .pattern("IGI")
                    .define('I', Items.IRON_INGOT)
                    .define('G', Items.GLASS)
                    .define('R', Items.REDSTONE_BLOCK)
                    .unlockedBy("has_iron", has(Items.IRON_INGOT))
                    .save(consumer);
        }
    }

    private static class ModLootTableProvider extends net.minecraft.data.loot.LootTableProvider {
        public ModLootTableProvider(PackOutput output) {
            super(output, java.util.Set.of(),
                    java.util.List.of(
                            new SubProviderEntry(ModBlockLootTables::new,
                                    net.minecraft.data.loot.LootTableProvider.LootTableType.BLOCK)
                    ));
        }

        private static class ModBlockLootTables extends net.minecraft.data.loot.BlockLootSubProvider {
            protected ModBlockLootTables() {
                super(java.util.Set.of(), net.minecraftforge.registries.ForgeRegistries.ITEMS);
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