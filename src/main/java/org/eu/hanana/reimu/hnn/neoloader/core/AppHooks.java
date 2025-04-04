package org.eu.hanana.reimu.hnn.neoloader.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppHooks {
    public static final String INTERNAL_NAME = AppHooks.class.getName().replace('.', '/');
    /** This hook runs Fabric's ModInitializer.onInitialize() from where it is called.
     *  It's recommended that you call them from as late into the game's execution as you can while still being before the game loop,
     *  to allow ModInitializer to allow as many game alterations as possible.
     *
     *
     */
    public static void init() {
        System.out.println("Patched!");
        Log.info(LogCategory.GAME_PROVIDER,"Patched!");
        //net.fabricmc.loader.impl.game.minecraft.Hooks.startClient(Provider.getInstance().getLaunchDirectory().toFile(),);
    }
    public static void run(Object o) {
        System.out.println("Patched run!");
        net.fabricmc.loader.impl.game.minecraft.Hooks.startClient(FabricLoaderImpl.INSTANCE.getGameProvider().getLaunchDirectory().toFile(),o);
    }
}