/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;

public class ZombieModel<T extends Zombie>
extends AbstractZombieModel<T> {
    public ZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public boolean isAggressive(T t) {
        return ((Mob)t).isAggressive();
    }
}

