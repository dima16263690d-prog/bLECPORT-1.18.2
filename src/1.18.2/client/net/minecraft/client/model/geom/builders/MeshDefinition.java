/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class MeshDefinition {
    private PartDefinition root = new PartDefinition((List<CubeDefinition>)ImmutableList.of(), PartPose.ZERO);

    public PartDefinition getRoot() {
        return this.root;
    }
}

