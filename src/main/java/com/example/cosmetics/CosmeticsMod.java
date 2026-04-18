package com.example.cosmetics;

import com.example.cosmetics.client.ClientEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CosmeticsMod.MOD_ID)
public class CosmeticsMod {
    public static final String MOD_ID = "cosmeticsmod";

    public CosmeticsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modBus.addListener(ClientEvents::onClientSetup);
        });
    }
}
