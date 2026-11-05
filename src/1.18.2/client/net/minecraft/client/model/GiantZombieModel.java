/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Giant;

public class GiantZombieModel
extends AbstractZombieModel<Giant> {
    public GiantZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public boolean isAggressive(Giant giant) {
        return false;
    }
}

