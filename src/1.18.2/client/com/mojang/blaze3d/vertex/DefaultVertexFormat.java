/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class DefaultVertexFormat {
    public static final VertexFormatElement ELEMENT_POSITION = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
    public static final VertexFormatElement ELEMENT_COLOR = new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);
    public static final VertexFormatElement ELEMENT_UV0 = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2);
    public static final VertexFormatElement ELEMENT_UV1 = new VertexFormatElement(1, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
    public static final VertexFormatElement ELEMENT_UV2 = new VertexFormatElement(2, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
    public static final VertexFormatElement ELEMENT_NORMAL = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3);
    public static final VertexFormatElement ELEMENT_PADDING = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.PADDING, 1);
    public static final VertexFormatElement ELEMENT_UV = ELEMENT_UV0;
    public static final VertexFormat BLIT_SCREEN = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"UV", (Object)ELEMENT_UV).put((Object)"Color", (Object)ELEMENT_COLOR).build());
    public static final VertexFormat BLOCK = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"Color", (Object)ELEMENT_COLOR).put((Object)"UV0", (Object)ELEMENT_UV0).put((Object)"UV2", (Object)ELEMENT_UV2).put((Object)"Normal", (Object)ELEMENT_NORMAL).put((Object)"Padding", (Object)ELEMENT_PADDING).build());
    public static final VertexFormat NEW_ENTITY = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"Color", (Object)ELEMENT_COLOR).put((Object)"UV0", (Object)ELEMENT_UV0).put((Object)"UV1", (Object)ELEMENT_UV1).put((Object)"UV2", (Object)ELEMENT_UV2).put((Object)"Normal", (Object)ELEMENT_NORMAL).put((Object)"Padding", (Object)ELEMENT_PADDING).build());
    public static final VertexFormat PARTICLE = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"UV0", (Object)ELEMENT_UV0).put((Object)"Color", (Object)ELEMENT_COLOR).put((Object)"UV2", (Object)ELEMENT_UV2).build());
    public static final VertexFormat POSITION = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).build());
    public static final VertexFormat POSITION_COLOR = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"Color", (Object)ELEMENT_COLOR).build());
    public static final VertexFormat POSITION_COLOR_NORMAL = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"Color", (Object)ELEMENT_COLOR).put((Object)"Normal", (Object)ELEMENT_NORMAL).put((Object)"Padding", (Object)ELEMENT_PADDING).build());
    public static final VertexFormat POSITION_COLOR_LIGHTMAP = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"Color", (Object)ELEMENT_COLOR).put((Object)"UV2", (Object)ELEMENT_UV2).build());
    public static final VertexFormat POSITION_TEX = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"UV0", (Object)ELEMENT_UV0).build());
    public static final VertexFormat POSITION_COLOR_TEX = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"Color", (Object)ELEMENT_COLOR).put((Object)"UV0", (Object)ELEMENT_UV0).build());
    public static final VertexFormat POSITION_TEX_COLOR = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"UV0", (Object)ELEMENT_UV0).put((Object)"Color", (Object)ELEMENT_COLOR).build());
    public static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"Color", (Object)ELEMENT_COLOR).put((Object)"UV0", (Object)ELEMENT_UV0).put((Object)"UV2", (Object)ELEMENT_UV2).build());
    public static final VertexFormat POSITION_TEX_LIGHTMAP_COLOR = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"UV0", (Object)ELEMENT_UV0).put((Object)"UV2", (Object)ELEMENT_UV2).put((Object)"Color", (Object)ELEMENT_COLOR).build());
    public static final VertexFormat POSITION_TEX_COLOR_NORMAL = new VertexFormat((ImmutableMap<String, VertexFormatElement>)ImmutableMap.builder().put((Object)"Position", (Object)ELEMENT_POSITION).put((Object)"UV0", (Object)ELEMENT_UV0).put((Object)"Color", (Object)ELEMENT_COLOR).put((Object)"Normal", (Object)ELEMENT_NORMAL).put((Object)"Padding", (Object)ELEMENT_PADDING).build());
}

