/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  javax.annotation.Nullable
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class ShaderInstance
implements Shader,
AutoCloseable {
    private static final String SHADER_PATH = "shaders/core/";
    private static final String SHADER_INCLUDE_PATH = "shaders/include/";
    static final Logger LOGGER = LogUtils.getLogger();
    private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
    private static final boolean ALWAYS_REAPPLY = true;
    private static ShaderInstance lastAppliedShader;
    private static int lastProgramId;
    private final Map<String, Object> samplerMap = Maps.newHashMap();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> samplerLocations = Lists.newArrayList();
    private final List<Uniform> uniforms = Lists.newArrayList();
    private final List<Integer> uniformLocations = Lists.newArrayList();
    private final Map<String, Uniform> uniformMap = Maps.newHashMap();
    private final int programId;
    private final String name;
    private boolean dirty;
    private final BlendMode blend;
    private final List<Integer> attributes;
    private final List<String> attributeNames;
    private final Program vertexProgram;
    private final Program fragmentProgram;
    private final VertexFormat vertexFormat;
    @Nullable
    public final Uniform MODEL_VIEW_MATRIX;
    @Nullable
    public final Uniform PROJECTION_MATRIX;
    @Nullable
    public final Uniform INVERSE_VIEW_ROTATION_MATRIX;
    @Nullable
    public final Uniform TEXTURE_MATRIX;
    @Nullable
    public final Uniform SCREEN_SIZE;
    @Nullable
    public final Uniform COLOR_MODULATOR;
    @Nullable
    public final Uniform LIGHT0_DIRECTION;
    @Nullable
    public final Uniform LIGHT1_DIRECTION;
    @Nullable
    public final Uniform FOG_START;
    @Nullable
    public final Uniform FOG_END;
    @Nullable
    public final Uniform FOG_COLOR;
    @Nullable
    public final Uniform FOG_SHAPE;
    @Nullable
    public final Uniform LINE_WIDTH;
    @Nullable
    public final Uniform GAME_TIME;
    @Nullable
    public final Uniform CHUNK_OFFSET;

    public ShaderInstance(ResourceProvider resourceProvider, String string, VertexFormat vertexFormat) throws IOException {
        this.name = string;
        this.vertexFormat = vertexFormat;
        ResourceLocation resourceLocation = new ResourceLocation(SHADER_PATH + string + ".json");
        Resource resource = null;
        try {
            JsonArray jsonArray;
            JsonArray jsonArray2;
            resource = resourceProvider.getResource(resourceLocation);
            JsonObject jsonObject = GsonHelper.parse(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            String string2 = GsonHelper.getAsString(jsonObject, "vertex");
            String string3 = GsonHelper.getAsString(jsonObject, "fragment");
            JsonArray jsonArray3 = GsonHelper.getAsJsonArray(jsonObject, "samplers", null);
            if (jsonArray3 != null) {
                int n = 0;
                for (Object object : jsonArray3) {
                    try {
                        this.parseSamplerNode((JsonElement)object);
                    }
                    catch (Exception exception) {
                        ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                        chainedJsonException.prependJsonKey("samplers[" + n + "]");
                        throw chainedJsonException;
                    }
                    ++n;
                }
            }
            if ((jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "attributes", null)) != null) {
                int n = 0;
                this.attributes = Lists.newArrayListWithCapacity((int)jsonArray2.size());
                this.attributeNames = Lists.newArrayListWithCapacity((int)jsonArray2.size());
                for (Iterator iterator : jsonArray2) {
                    try {
                        this.attributeNames.add(GsonHelper.convertToString((JsonElement)iterator, "attribute"));
                    }
                    catch (Exception exception) {
                        ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                        chainedJsonException.prependJsonKey("attributes[" + n + "]");
                        throw chainedJsonException;
                    }
                    ++n;
                }
            } else {
                this.attributes = null;
                this.attributeNames = null;
            }
            if ((jsonArray = GsonHelper.getAsJsonArray(jsonObject, "uniforms", null)) != null) {
                int n = 0;
                for (Object object : jsonArray) {
                    try {
                        this.parseUniformNode((JsonElement)object);
                    }
                    catch (Exception exception) {
                        ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                        chainedJsonException.prependJsonKey("uniforms[" + n + "]");
                        throw chainedJsonException;
                    }
                    ++n;
                }
            }
            this.blend = ShaderInstance.parseBlendNode(GsonHelper.getAsJsonObject(jsonObject, "blend", null));
            this.vertexProgram = ShaderInstance.getOrCreate(resourceProvider, Program.Type.VERTEX, string2);
            this.fragmentProgram = ShaderInstance.getOrCreate(resourceProvider, Program.Type.FRAGMENT, string3);
            this.programId = ProgramManager.createProgram();
            if (this.attributeNames != null) {
                int n = 0;
                for (Object object : vertexFormat.getElementAttributeNames()) {
                    Uniform.glBindAttribLocation(this.programId, n, (CharSequence)object);
                    this.attributes.add(n);
                    ++n;
                }
            }
            ProgramManager.linkShader(this);
            this.updateLocations();
        }
        catch (Exception exception) {
            ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
            chainedJsonException.setFilenameAndFlush(resourceLocation.getPath());
            throw chainedJsonException;
        }
        finally {
            IOUtils.closeQuietly((Closeable)resource);
        }
        this.markDirty();
        this.MODEL_VIEW_MATRIX = this.getUniform("ModelViewMat");
        this.PROJECTION_MATRIX = this.getUniform("ProjMat");
        this.INVERSE_VIEW_ROTATION_MATRIX = this.getUniform("IViewRotMat");
        this.TEXTURE_MATRIX = this.getUniform("TextureMat");
        this.SCREEN_SIZE = this.getUniform("ScreenSize");
        this.COLOR_MODULATOR = this.getUniform("ColorModulator");
        this.LIGHT0_DIRECTION = this.getUniform("Light0_Direction");
        this.LIGHT1_DIRECTION = this.getUniform("Light1_Direction");
        this.FOG_START = this.getUniform("FogStart");
        this.FOG_END = this.getUniform("FogEnd");
        this.FOG_COLOR = this.getUniform("FogColor");
        this.FOG_SHAPE = this.getUniform("FogShape");
        this.LINE_WIDTH = this.getUniform("LineWidth");
        this.GAME_TIME = this.getUniform("GameTime");
        this.CHUNK_OFFSET = this.getUniform("ChunkOffset");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Program getOrCreate(final ResourceProvider resourceProvider, Program.Type type, String string) throws IOException {
        Program program;
        Program program2 = type.getPrograms().get(string);
        if (program2 == null) {
            String string2 = SHADER_PATH + string + type.getExtension();
            ResourceLocation resourceLocation = new ResourceLocation(string2);
            Resource resource = resourceProvider.getResource(resourceLocation);
            final String string3 = FileUtil.getFullResourcePath(string2);
            try {
                program = Program.compileShader(type, string, resource.getInputStream(), resource.getSourceName(), new GlslPreprocessor(){
                    private final Set<String> importedPaths = Sets.newHashSet();

                    @Override
                    public String applyImport(boolean bl, String string) {
                        String string2;
                        block9: {
                            string = FileUtil.normalizeResourcePath((bl ? string3 : ShaderInstance.SHADER_INCLUDE_PATH) + string);
                            if (!this.importedPaths.add(string)) {
                                return null;
                            }
                            ResourceLocation resourceLocation = new ResourceLocation(string);
                            Resource resource = resourceProvider.getResource(resourceLocation);
                            try {
                                string2 = IOUtils.toString((InputStream)resource.getInputStream(), (Charset)StandardCharsets.UTF_8);
                                if (resource == null) break block9;
                            }
                            catch (Throwable throwable) {
                                try {
                                    if (resource != null) {
                                        try {
                                            resource.close();
                                        }
                                        catch (Throwable throwable2) {
                                            throwable.addSuppressed(throwable2);
                                        }
                                    }
                                    throw throwable;
                                }
                                catch (IOException iOException) {
                                    LOGGER.error("Could not open GLSL import {}: {}", (Object)string, (Object)iOException.getMessage());
                                    return "#error " + iOException.getMessage();
                                }
                            }
                            resource.close();
                        }
                        return string2;
                    }
                });
            }
            finally {
                IOUtils.closeQuietly((Closeable)resource);
            }
        } else {
            program = program2;
        }
        return program;
    }

    public static BlendMode parseBlendNode(JsonObject jsonObject) {
        if (jsonObject == null) {
            return new BlendMode();
        }
        int n = 32774;
        int n2 = 1;
        int n3 = 0;
        int n4 = 1;
        int n5 = 0;
        boolean bl = true;
        boolean bl2 = false;
        if (GsonHelper.isStringValue(jsonObject, "func") && (n = BlendMode.stringToBlendFunc(jsonObject.get("func").getAsString())) != 32774) {
            bl = false;
        }
        if (GsonHelper.isStringValue(jsonObject, "srcrgb") && (n2 = BlendMode.stringToBlendFactor(jsonObject.get("srcrgb").getAsString())) != 1) {
            bl = false;
        }
        if (GsonHelper.isStringValue(jsonObject, "dstrgb") && (n3 = BlendMode.stringToBlendFactor(jsonObject.get("dstrgb").getAsString())) != 0) {
            bl = false;
        }
        if (GsonHelper.isStringValue(jsonObject, "srcalpha")) {
            n4 = BlendMode.stringToBlendFactor(jsonObject.get("srcalpha").getAsString());
            if (n4 != 1) {
                bl = false;
            }
            bl2 = true;
        }
        if (GsonHelper.isStringValue(jsonObject, "dstalpha")) {
            n5 = BlendMode.stringToBlendFactor(jsonObject.get("dstalpha").getAsString());
            if (n5 != 0) {
                bl = false;
            }
            bl2 = true;
        }
        if (bl) {
            return new BlendMode();
        }
        if (bl2) {
            return new BlendMode(n2, n3, n4, n5, n);
        }
        return new BlendMode(n2, n3, n);
    }

    @Override
    public void close() {
        for (Uniform uniform : this.uniforms) {
            uniform.close();
        }
        ProgramManager.releaseProgram(this);
    }

    public void clear() {
        RenderSystem.assertOnRenderThread();
        ProgramManager.glUseProgram(0);
        lastProgramId = -1;
        lastAppliedShader = null;
        int n = GlStateManager._getActiveTexture();
        for (int i = 0; i < this.samplerLocations.size(); ++i) {
            if (this.samplerMap.get(this.samplerNames.get(i)) == null) continue;
            GlStateManager._activeTexture(33984 + i);
            GlStateManager._bindTexture(0);
        }
        GlStateManager._activeTexture(n);
    }

    public void apply() {
        RenderSystem.assertOnRenderThread();
        this.dirty = false;
        lastAppliedShader = this;
        this.blend.apply();
        if (this.programId != lastProgramId) {
            ProgramManager.glUseProgram(this.programId);
            lastProgramId = this.programId;
        }
        int n = GlStateManager._getActiveTexture();
        for (int i = 0; i < this.samplerLocations.size(); ++i) {
            String object = this.samplerNames.get(i);
            if (this.samplerMap.get(object) == null) continue;
            int n2 = Uniform.glGetUniformLocation(this.programId, object);
            Uniform.uploadInteger(n2, i);
            RenderSystem.activeTexture(33984 + i);
            RenderSystem.enableTexture();
            Object object2 = this.samplerMap.get(object);
            int n3 = -1;
            if (object2 instanceof RenderTarget) {
                n3 = ((RenderTarget)object2).getColorTextureId();
            } else if (object2 instanceof AbstractTexture) {
                n3 = ((AbstractTexture)object2).getId();
            } else if (object2 instanceof Integer) {
                n3 = (Integer)object2;
            }
            if (n3 == -1) continue;
            RenderSystem.bindTexture(n3);
        }
        GlStateManager._activeTexture(n);
        for (Uniform uniform : this.uniforms) {
            uniform.upload();
        }
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Nullable
    public Uniform getUniform(String string) {
        RenderSystem.assertOnRenderThread();
        return this.uniformMap.get(string);
    }

    public AbstractUniform safeGetUniform(String string) {
        RenderSystem.assertOnGameThread();
        Uniform uniform = this.getUniform(string);
        return uniform == null ? DUMMY_UNIFORM : uniform;
    }

    private void updateLocations() {
        int n;
        RenderSystem.assertOnRenderThread();
        IntArrayList intArrayList = new IntArrayList();
        for (n = 0; n < this.samplerNames.size(); ++n) {
            String string = this.samplerNames.get(n);
            int n2 = Uniform.glGetUniformLocation(this.programId, string);
            if (n2 == -1) {
                LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", (Object)this.name, (Object)string);
                this.samplerMap.remove(string);
                intArrayList.add(n);
                continue;
            }
            this.samplerLocations.add(n2);
        }
        for (n = intArrayList.size() - 1; n >= 0; --n) {
            int n3 = intArrayList.getInt(n);
            this.samplerNames.remove(n3);
        }
        for (Uniform uniform : this.uniforms) {
            String string = uniform.getName();
            int n4 = Uniform.glGetUniformLocation(this.programId, string);
            if (n4 == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", (Object)this.name, (Object)string);
                continue;
            }
            this.uniformLocations.add(n4);
            uniform.setLocation(n4);
            this.uniformMap.put(string, uniform);
        }
    }

    private void parseSamplerNode(JsonElement jsonElement) {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "sampler");
        String string = GsonHelper.getAsString(jsonObject, "name");
        if (!GsonHelper.isStringValue(jsonObject, "file")) {
            this.samplerMap.put(string, null);
            this.samplerNames.add(string);
            return;
        }
        this.samplerNames.add(string);
    }

    public void setSampler(String string, Object object) {
        this.samplerMap.put(string, object);
        this.markDirty();
    }

    private void parseUniformNode(JsonElement jsonElement) throws ChainedJsonException {
        Object object2;
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "uniform");
        String string = GsonHelper.getAsString(jsonObject, "name");
        int n = Uniform.getTypeFromString(GsonHelper.getAsString(jsonObject, "type"));
        int n2 = GsonHelper.getAsInt(jsonObject, "count");
        float[] fArray = new float[Math.max(n2, 16)];
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
        if (jsonArray.size() != n2 && jsonArray.size() > 1) {
            throw new ChainedJsonException("Invalid amount of values specified (expected " + n2 + ", found " + jsonArray.size() + ")");
        }
        int n3 = 0;
        for (Object object2 : jsonArray) {
            try {
                fArray[n3] = GsonHelper.convertToFloat((JsonElement)object2, "value");
            }
            catch (Exception exception) {
                ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                chainedJsonException.prependJsonKey("values[" + n3 + "]");
                throw chainedJsonException;
            }
            ++n3;
        }
        if (n2 > 1 && jsonArray.size() == 1) {
            while (n3 < n2) {
                fArray[n3] = fArray[0];
                ++n3;
            }
        }
        int n4 = n2 > 1 && n2 <= 4 && n < 8 ? n2 - 1 : 0;
        object2 = new Uniform(string, n + n4, n2, this);
        if (n <= 3) {
            ((Uniform)object2).setSafe((int)fArray[0], (int)fArray[1], (int)fArray[2], (int)fArray[3]);
        } else if (n <= 7) {
            ((Uniform)object2).setSafe(fArray[0], fArray[1], fArray[2], fArray[3]);
        } else {
            ((Uniform)object2).set(Arrays.copyOfRange(fArray, 0, n2));
        }
        this.uniforms.add((Uniform)object2);
    }

    @Override
    public Program getVertexProgram() {
        return this.vertexProgram;
    }

    @Override
    public Program getFragmentProgram() {
        return this.fragmentProgram;
    }

    @Override
    public void attachToProgram() {
        this.fragmentProgram.attachToShader(this);
        this.vertexProgram.attachToShader(this);
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getId() {
        return this.programId;
    }

    static {
        lastProgramId = -1;
    }
}

