package org.eu.hanana.reimu.hnn.neoloader.core;

import net.fabricmc.loader.impl.game.patch.GamePatch;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Transformer extends GameTransformer {
    public final List<GamePatch> patches;
    public final Map<String, byte[]> patchedClasses;
    public Transformer(GamePatch... patches){
        super(patches);
        this.patches= List.of(patches);
        patchedClasses=new HashMap<>();
    }
    @Override
    public byte[] transform(String className) {
        var res = super.transform(className);
        if (res!=null) {
            patchedClasses.put(className,res);
            return res;
        }
        if (patchedClasses.containsKey(className))
            return patchedClasses.get(className);
        
        try (ZipFile zf = new ZipFile(Provider.getInstance().app_jar.toFile())) {
            Function<String, ClassReader> classSource = name -> {
                byte[] data = patchedClasses.get(name);

                if (data != null) {
                    return new ClassReader(data);
                }

                ZipEntry entry = zf.getEntry(LoaderUtil.getClassFileName(name));
                if (entry == null) return null;

                try (InputStream is = zf.getInputStream(entry)) {
                    return new ClassReader(is);
                } catch (IOException e) {
                    throw new UncheckedIOException(String.format("error reading %s in %s: %s", name, Provider.getInstance().app_jar.toAbsolutePath(), e), e);
                }
            };

            for (GamePatch patch : patches) {
                patch.process(FabricLauncherBase.getLauncher(), classSource, this::addPatchedClass);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("error reading %s: %s", Provider.getInstance().app_jar.toAbsolutePath(), e), e);
        }

        return patchedClasses.get(className);
    }
    public void addPatchedClass(ClassNode node) {
        String key = node.name.replace('/', '.');

        if (patchedClasses.containsKey(key)) {
            return;
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        patchedClasses.put(key, writer.toByteArray());
    }
}
