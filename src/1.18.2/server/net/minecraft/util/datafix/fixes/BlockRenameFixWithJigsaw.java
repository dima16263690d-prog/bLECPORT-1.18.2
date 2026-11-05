/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.BlockRenameFix;
import net.minecraft.util.datafix.fixes.References;

public abstract class BlockRenameFixWithJigsaw
extends BlockRenameFix {
    private final String name;

    public BlockRenameFixWithJigsaw(Schema schema, String string) {
        super(schema, string);
        this.name = string;
    }

    @Override
    public TypeRewriteRule makeRule() {
        DSL.TypeReference typeReference = References.BLOCK_ENTITY;
        String string = "minecraft:jigsaw";
        OpticFinder opticFinder = DSL.namedChoice((String)"minecraft:jigsaw", (Type)this.getInputSchema().getChoiceType(typeReference, "minecraft:jigsaw"));
        TypeRewriteRule typeRewriteRule = this.fixTypeEverywhereTyped(this.name + " for jigsaw state", this.getInputSchema().getType(typeReference), this.getOutputSchema().getType(typeReference), typed2 -> typed2.updateTyped(opticFinder, this.getOutputSchema().getChoiceType(typeReference, "minecraft:jigsaw"), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("final_state", dynamic2 -> (Dynamic)DataFixUtils.orElse(dynamic2.asString().result().map(string -> {
            int n = string.indexOf(91);
            int n2 = string.indexOf(123);
            int n3 = string.length();
            if (n > 0) {
                n3 = Math.min(n3, n);
            }
            if (n2 > 0) {
                n3 = Math.min(n3, n2);
            }
            String string2 = string.substring(0, n3);
            String string3 = this.fixBlock(string2);
            return string3 + string.substring(n3);
        }).map(arg_0 -> ((Dynamic)dynamic).createString(arg_0)), (Object)dynamic2)))));
        return TypeRewriteRule.seq((TypeRewriteRule)super.makeRule(), (TypeRewriteRule)typeRewriteRule);
    }

    public static DataFix create(Schema schema, String string, final Function<String, String> function) {
        return new BlockRenameFixWithJigsaw(schema, string){

            @Override
            protected String fixBlock(String string) {
                return (String)function.apply(string);
            }
        };
    }
}

