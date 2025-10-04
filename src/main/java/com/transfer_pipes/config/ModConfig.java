package com.transfer_pipes.config;

import com.transfer_pipes.TransferPipes;
import com.transfer_pipes.TransferPipes;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = TransferPipes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Item Pipe Configuration
    private static final ForgeConfigSpec.IntValue ITEM_PIPE_TRANSFER_RATE = BUILDER
            .comment("Maximum items transferred per operation (default: 2147483647 = Integer.MAX_VALUE)")
            .defineInRange("itemPipeTransferRate", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ITEM_PIPE_COOLDOWN = BUILDER
            .comment("Ticks between item transfer operations (default: 5)")
            .defineInRange("itemPipeCooldown", 5, 1, 200);

    // Fluid Pipe Configuration
    private static final ForgeConfigSpec.IntValue FLUID_PIPE_TRANSFER_RATE = BUILDER
            .comment("Maximum millibuckets transferred per operation (default: 2147483647 = Integer.MAX_VALUE)")
            .defineInRange("fluidPipeTransferRate", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue FLUID_PIPE_COOLDOWN = BUILDER
            .comment("Ticks between fluid transfer operations (default: 5)")
            .defineInRange("fluidPipeCooldown", 5, 1, 200);

    // Energy Pipe Configuration
    private static final ForgeConfigSpec.IntValue ENERGY_PIPE_TRANSFER_RATE = BUILDER
            .comment("Maximum Forge Energy transferred per operation (default: 2147483647 = Integer.MAX_VALUE)")
            .defineInRange("energyPipeTransferRate", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue ENERGY_PIPE_COOLDOWN = BUILDER
            .comment("Ticks between energy transfer operations (default: 5)")
            .defineInRange("energyPipeCooldown", 5, 1, 200);

    // Visual Configuration
    private static final ForgeConfigSpec.BooleanValue ENABLE_PARTICLE_EFFECTS = BUILDER
            .comment("Enable particle effects for transfer simulation (default: true)")
            .define("enableParticleEffects", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // Public static fields for easy access
    public static int itemPipeTransferRate;
    public static int itemPipeCooldown;
    public static int fluidPipeTransferRate;
    public static int fluidPipeCooldown;
    public static int energyPipeTransferRate;
    public static int energyPipeCooldown;
    public static boolean enableParticleEffects;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        itemPipeTransferRate = ITEM_PIPE_TRANSFER_RATE.get();
        itemPipeCooldown = ITEM_PIPE_COOLDOWN.get();
        fluidPipeTransferRate = FLUID_PIPE_TRANSFER_RATE.get();
        fluidPipeCooldown = FLUID_PIPE_COOLDOWN.get();
        energyPipeTransferRate = ENERGY_PIPE_TRANSFER_RATE.get();
        energyPipeCooldown = ENERGY_PIPE_COOLDOWN.get();
        enableParticleEffects = ENABLE_PARTICLE_EFFECTS.get();
    }
}
