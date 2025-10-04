package com.transfer_pipes;

import com.mojang.logging.LogUtils;
import com.transfer_pipes.block.ModBlocks;
import com.transfer_pipes.blockentity.ModBlockEntities;
import com.transfer_pipes.config.ModConfig;
import com.transfer_pipes.item.ModCreativeTabs;
import com.transfer_pipes.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TransferPipes.MODID)
public class TransferPipes {
    public static final String MODID = "transfer_pipes";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TransferPipes() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        // Register all deferred registers
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        // Register config
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Transfer Pipes mod loaded successfully!");
        LOGGER.info("Item Pipe Transfer Rate: {}", ModConfig.itemPipeTransferRate);
        LOGGER.info("Fluid Pipe Transfer Rate: {}", ModConfig.fluidPipeTransferRate);
        LOGGER.info("Energy Pipe Transfer Rate: {}", ModConfig.energyPipeTransferRate);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Transfer Pipes server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Transfer Pipes client setup");
            LOGGER.info("Player: {}", Minecraft.getInstance().getUser().getName());
        }
    }
}