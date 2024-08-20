package org.eu.hanana.reimu.hnn.neoloader.core;

import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.game.minecraft.patch.EntrypointPatch;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.fabricmc.loader.impl.util.Arguments;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import org.eu.hanana.reimu.hnnapp.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Provider implements GameProvider {
    public Path app_jar;
    public Arguments arguments;
    private static Provider Instance;

    public static Provider getInstance() {
        return Instance;
    }
    public Provider(){
        Instance = this;
    }
    private final GameTransformer transformer = new GameTransformer(
            new MainPatch(),
            new RunPatch()
    );
    @Override
    public String getGameId() {
        return "hanana";
    }

    @Override
    public String getGameName() {
        return "hanana";
    }

    @Override
    public String getRawGameVersion() {
        return "1.0.0";
    }

    @Override
    public String getNormalizedGameVersion() {
        return "1.0.0";
    }

    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        BuiltinMod mod = new BuiltinMod(List.of(app_jar), new BuiltinModMetadata.Builder("app", getRawGameVersion()).build());

        return Arrays.asList(mod);
    }

    @Override
    public String getEntrypoint() {
        return "org.eu.hanana.reimu.hnnapp.FabricMain";
    }

    @Override
    public Path getLaunchDirectory() {
        return Path.of(".");
    }

    @Override
    public boolean isObfuscated() {
        return false;
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean locateGame(FabricLauncher launcher, String[] args) {
        arguments = new Arguments();
        arguments.parse(args);

        app_jar= Path.of(arguments.getOrDefault("appjar","./hanana_app-1.0-SNAPSHOT-all.jar"));
        return app_jar.toFile().exists();
    }

    @Override
    public void initialize(FabricLauncher launcher) {
        transformer.locateEntrypoints(launcher, app_jar);
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return transformer;
    }

    @Override
    public void unlockClassPath(FabricLauncher launcher) {
        Log.info(LogCategory.GAME_PROVIDER,"Unlocking app!");
        launcher.addToClassPath(app_jar);

        Log.info(LogCategory.GAME_PROVIDER,"Unlocking legacy mods!");
        String cp = System.getProperty("user.dir");
        File path = new File(cp, "mods");
        if (!path.exists()) path.mkdirs();
        // 创建 Path 对象
        Path folder = Paths.get(path.toURI());
        List<Path> list = new ArrayList<>();
        // 使用 Files.walk 方法递归获取文件夹内的所有文件
        try {
            Files.walk(folder)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        File file = filePath.toFile();
                        if (file.getName().toLowerCase().endsWith(".jar")) {
                            list.add(file.toPath());
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Path path1 : list) {
            launcher.addToClassPath(path1);
        }
        Log.info(LogCategory.GAME_PROVIDER,"Unlocking extra mods!");
        var exMods = arguments.getOrDefault("extra-mods",null);
        if (exMods!=null){
            String[] s = exMods.split(" ");
            for (String string : s) {
                launcher.addToClassPath(Path.of(string));
            }
        }
    }

    @Override
    public void launch(ClassLoader loader) {
        String targetClass = getEntrypoint();

        try {
            Class<?> c = loader.loadClass(targetClass);
            Method m = c.getMethod("main", String[].class, URL[].class);
            m.invoke(null, getArguments().toArray(), ((URLClassLoader) loader).getURLs());
        }
        catch(InvocationTargetException e) {
            throw new FormattedException("The game has crashed!", e.getCause());
        }
        catch(ReflectiveOperationException e) {
            throw new FormattedException("Failed to start the game", e);
        }
    }

    @Override
    public Arguments getArguments() {
        return arguments;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return arguments.toArray();
    }
}
