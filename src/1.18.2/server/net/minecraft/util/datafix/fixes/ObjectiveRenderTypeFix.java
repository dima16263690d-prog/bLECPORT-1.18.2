/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ObjectiveRenderTypeFix
extends DataFix {
    public ObjectiveRenderTypeFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    private static ObjectiveCriteria.RenderType getRenderType(String string) {
        return string.equals("health") ? ObjectiveCriteria.RenderType.HEARTS : ObjectiveCriteria.RenderType.INTEGER;
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.OBJECTIVE);
        return this.fixTypeEverywhereTyped("ObjectiveRenderTypeFix", type, typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            Optional optional = dynamic.get("RenderType").asString().result();
            if (!optional.isPresent()) {
                String string = dynamic.get("CriteriaName").asString("");
                ObjectiveCriteria.RenderType renderType = ObjectiveRenderTypeFix.getRenderType(string);
                return dynamic.set("RenderType", dynamic.createString(renderType.getId()));
            }
            return dynamic;
        }));
    }
}

