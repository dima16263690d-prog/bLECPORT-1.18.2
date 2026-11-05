/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.data.structures;

import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import javax.annotation.Nullable;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public class NbtToSnbt
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator generator;

    public NbtToSnbt(DataGenerator dataGenerator) {
        this.generator = dataGenerator;
    }

    @Override
    public void run(HashCache hashCache) throws IOException {
        Path path2 = this.generator.getOutputFolder();
        for (Path path4 : this.generator.getInputFolders()) {
            Files.walk(path4, new FileVisitOption[0]).filter(path -> path.toString().endsWith(".nbt")).forEach(path3 -> NbtToSnbt.convertStructure(path3, this.getName(path4, (Path)path3), path2));
        }
    }

    @Override
    public String getName() {
        return "NBT to SNBT";
    }

    private String getName(Path path, Path path2) {
        String string = path.relativize(path2).toString().replaceAll("\\\\", "/");
        return string.substring(0, string.length() - ".nbt".length());
    }

    @Nullable
    public static Path convertStructure(Path path, String string, Path path2) {
        try {
            NbtToSnbt.writeSnbt(path2.resolve(string + ".snbt"), NbtUtils.structureToSnbt(NbtIo.readCompressed(Files.newInputStream(path, new OpenOption[0]))));
            LOGGER.info("Converted {} from NBT to SNBT", (Object)string);
            return path2.resolve(string + ".snbt");
        }
        catch (IOException iOException) {
            LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", new Object[]{string, path, iOException});
            return null;
        }
    }

    public static void writeSnbt(Path path, String string) throws IOException {
        Files.createDirectories(path.getParent(), new FileAttribute[0]);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
            bufferedWriter.write(string);
            bufferedWriter.write(10);
        }
    }
}

