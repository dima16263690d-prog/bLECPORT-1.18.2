/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.geom.builders;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.MaterialDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class LayerDefinition {
    private final MeshDefinition mesh;
    private final MaterialDefinition material;

    private LayerDefinition(MeshDefinition meshDefinition, MaterialDefinition materialDefinition) {
        this.mesh = meshDefinition;
        this.material = materialDefinition;
    }

    public ModelPart bakeRoot() {
        return this.mesh.getRoot().bake(this.material.xTexSize, this.material.yTexSize);
    }

    public static LayerDefinition create(MeshDefinition meshDefinition, int n, int n2) {
        return new LayerDefinition(meshDefinition, new MaterialDefinition(n, n2));
    }
}

