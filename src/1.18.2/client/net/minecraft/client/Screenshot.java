/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.slf4j.Logger;

public class Screenshot {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private int rowHeight;
    private final DataOutputStream outputStream;
    private final byte[] bytes;
    private final int width;
    private final int height;
    private File file;

    public static void grab(File file, RenderTarget renderTarget, Consumer<Component> consumer) {
        Screenshot.grab(file, null, renderTarget, consumer);
    }

    public static void grab(File file, @Nullable String string, RenderTarget renderTarget, Consumer<Component> consumer) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> Screenshot._grab(file, string, renderTarget, consumer));
        } else {
            Screenshot._grab(file, string, renderTarget, consumer);
        }
    }

    private static void _grab(File file, @Nullable String string, RenderTarget renderTarget, Consumer<Component> consumer) {
        NativeImage nativeImage = Screenshot.takeScreenshot(renderTarget);
        File file2 = new File(file, "screenshots");
        file2.mkdir();
        File file3 = string == null ? Screenshot.getFile(file2) : new File(file2, string);
        Util.ioPool().execute(() -> {
            try {
                nativeImage.writeToFile(file3);
                MutableComponent mutableComponent = new TextComponent(file3.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file3.getAbsolutePath())));
                consumer.accept(new TranslatableComponent("screenshot.success", mutableComponent));
            }
            catch (Exception exception) {
                LOGGER.warn("Couldn't save screenshot", (Throwable)exception);
                consumer.accept(new TranslatableComponent("screenshot.failure", exception.getMessage()));
            }
            finally {
                nativeImage.close();
            }
        });
    }

    public static NativeImage takeScreenshot(RenderTarget renderTarget) {
        int n = renderTarget.width;
        int n2 = renderTarget.height;
        NativeImage nativeImage = new NativeImage(n, n2, false);
        RenderSystem.bindTexture(renderTarget.getColorTextureId());
        nativeImage.downloadTexture(0, true);
        nativeImage.flipY();
        return nativeImage;
    }

    private static File getFile(File file) {
        String string = DATE_FORMAT.format(new Date());
        int n = 1;
        File file2;
        while ((file2 = new File(file, string + (String)(n == 1 ? "" : "_" + n) + ".png")).exists()) {
            ++n;
        }
        return file2;
    }

    public Screenshot(File file, int n, int n2, int n3) throws IOException {
        this.width = n;
        this.height = n2;
        this.rowHeight = n3;
        File file2 = new File(file, "screenshots");
        file2.mkdir();
        String string = "huge_" + DATE_FORMAT.format(new Date());
        int n4 = 1;
        while ((this.file = new File(file2, string + (String)(n4 == 1 ? "" : "_" + n4) + ".tga")).exists()) {
            ++n4;
        }
        byte[] byArray = new byte[18];
        byArray[2] = 2;
        byArray[12] = (byte)(n % 256);
        byArray[13] = (byte)(n / 256);
        byArray[14] = (byte)(n2 % 256);
        byArray[15] = (byte)(n2 / 256);
        byArray[16] = 24;
        this.bytes = new byte[n * n3 * 3];
        this.outputStream = new DataOutputStream(new FileOutputStream(this.file));
        this.outputStream.write(byArray);
    }

    public void addRegion(ByteBuffer byteBuffer, int n, int n2, int n3, int n4) {
        int n5 = n3;
        int n6 = n4;
        if (n5 > this.width - n) {
            n5 = this.width - n;
        }
        if (n6 > this.height - n2) {
            n6 = this.height - n2;
        }
        this.rowHeight = n6;
        for (int i = 0; i < n6; ++i) {
            byteBuffer.position((n4 - n6) * n3 * 3 + i * n3 * 3);
            int n7 = (n + i * this.width) * 3;
            byteBuffer.get(this.bytes, n7, n5 * 3);
        }
    }

    public void saveRow() throws IOException {
        this.outputStream.write(this.bytes, 0, this.width * 3 * this.rowHeight);
    }

    public File close() throws IOException {
        this.outputStream.close();
        return this.file;
    }
}

