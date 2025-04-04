package org.eu.hanana.reimu.hnn.neoloader.app;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.game.LibClassifier;

import java.net.URL;
import java.nio.file.Path;

public enum AppLibrary implements LibClassifier.LibraryType {
    LOG4J_API("org/apache/logging/log4j/LogManager.class"),
    LOG4J_CORE("META-INF/services/org.apache.logging.log4j.spi.Provider", "META-INF/log4j-provider.properties"),
    LOG4J_CONFIG("log4j2.xml"),
    LOG4J_PLUGIN("com/mojang/util/UUIDTypeAdapter.class"), // in authlib
    LOG4J_PLUGIN_2("com/mojang/patchy/LegacyXMLLayout.class"), // in patchy
    LOG4J_PLUGIN_3("net/minecrell/terminalconsole/util/LoggerNamePatternSelector.class"), // in terminalconsoleappender, used by loom's log4j config
    GSON("com/google/gson/TypeAdapter.class"), // used by log4j plugins
    SLF4J_API("org/slf4j/Logger.class"),
    SLF4J_CORE("META-INF/services/org.slf4j.spi.SLF4JServiceProvider");
    public static final AppLibrary[] LOGGING = { LOG4J_API, LOG4J_CORE, LOG4J_CONFIG, LOG4J_PLUGIN, LOG4J_PLUGIN_2, LOG4J_PLUGIN_3, GSON, SLF4J_API, SLF4J_CORE };


    public final EnvType env;
    public final String[] paths;
    AppLibrary(String path) {
        this(null, new String[] { path });
    }

    AppLibrary(String... paths) {
        this(null, paths);
    }

    AppLibrary(EnvType env, String... paths) {
        this.paths = paths;
        this.env = env;
    }
    @Override
    public boolean isApplicable(EnvType env) {
        return this.env == null || this.env == env;
    }

    @Override
    public String[] getPaths() {
        return paths;
    }
}
