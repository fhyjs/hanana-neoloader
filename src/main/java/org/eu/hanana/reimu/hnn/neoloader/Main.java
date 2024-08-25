package org.eu.hanana.reimu.hnn.neoloader;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.fabricmc.loader.impl.util.Arguments;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main implements Runnable {

    private static Main INSTEANCE;
    public String[] args;
    public static Main getInstance() {
        return INSTEANCE;
    }
    public static void main(String[] args) throws IOException {
        System.setProperty("fabric.log.level","debug");
        INSTEANCE = new Main();
        INSTEANCE.args=args;

        Arguments arguments = new Arguments();
        arguments.parse(args);
        String orDefault = arguments.getOrDefault("cp-mods", null);
        if (orDefault!=null){
            for (String s : orDefault.split(" ")) {
                var fileS = new File(s);
                Path mods = Path.of("mods", fileS.getName());
                if (mods.toFile().exists())
                    Files.delete(mods);
                Files.copy(fileS.toPath(), mods);
            }

        }

        INSTEANCE.run();
    }

    @Override
    public void run() {
        Knot.launch(args, EnvType.CLIENT);
    }
}