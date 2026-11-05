/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.bytes.ByteCollection
 *  it.unimi.dsi.fastutil.bytes.ByteOpenHashSet
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.slf4j.Logger;

public class TextComponentTagVisitor
implements TagVisitor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INLINE_LIST_THRESHOLD = 8;
    private static final ByteCollection INLINE_ELEMENT_TYPES = new ByteOpenHashSet(Arrays.asList((byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6));
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private static final String LIST_OPEN = "[";
    private static final String LIST_CLOSE = "]";
    private static final String LIST_TYPE_SEPARATOR = ";";
    private static final String ELEMENT_SPACING = " ";
    private static final String STRUCT_OPEN = "{";
    private static final String STRUCT_CLOSE = "}";
    private static final String NEWLINE = "\n";
    private final String indentation;
    private final int depth;
    private Component result = TextComponent.EMPTY;

    public TextComponentTagVisitor(String string, int n) {
        this.indentation = string;
        this.depth = n;
    }

    public Component visit(Tag tag) {
        tag.accept(this);
        return this.result;
    }

    @Override
    public void visitString(StringTag stringTag) {
        String string = StringTag.quoteAndEscape(stringTag.getAsString());
        String string2 = string.substring(0, 1);
        MutableComponent mutableComponent = new TextComponent(string.substring(1, string.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
        this.result = new TextComponent(string2).append(mutableComponent).append(string2);
    }

    @Override
    public void visitByte(ByteTag byteTag) {
        MutableComponent mutableComponent = new TextComponent("b").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(byteTag.getAsNumber())).append(mutableComponent).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitShort(ShortTag shortTag) {
        MutableComponent mutableComponent = new TextComponent("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(shortTag.getAsNumber())).append(mutableComponent).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitInt(IntTag intTag) {
        this.result = new TextComponent(String.valueOf(intTag.getAsNumber())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitLong(LongTag longTag) {
        MutableComponent mutableComponent = new TextComponent("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(longTag.getAsNumber())).append(mutableComponent).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitFloat(FloatTag floatTag) {
        MutableComponent mutableComponent = new TextComponent("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(floatTag.getAsFloat())).append(mutableComponent).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitDouble(DoubleTag doubleTag) {
        MutableComponent mutableComponent = new TextComponent("d").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        this.result = new TextComponent(String.valueOf(doubleTag.getAsDouble())).append(mutableComponent).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public void visitByteArray(ByteArrayTag byteArrayTag) {
        MutableComponent mutableComponent = new TextComponent("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent2 = new TextComponent(LIST_OPEN).append(mutableComponent).append(LIST_TYPE_SEPARATOR);
        byte[] byArray = byteArrayTag.getAsByteArray();
        for (int i = 0; i < byArray.length; ++i) {
            MutableComponent mutableComponent3 = new TextComponent(String.valueOf(byArray[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            mutableComponent2.append(ELEMENT_SPACING).append(mutableComponent3).append(mutableComponent);
            if (i == byArray.length - 1) continue;
            mutableComponent2.append(ELEMENT_SEPARATOR);
        }
        mutableComponent2.append(LIST_CLOSE);
        this.result = mutableComponent2;
    }

    @Override
    public void visitIntArray(IntArrayTag intArrayTag) {
        MutableComponent mutableComponent = new TextComponent("I").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent2 = new TextComponent(LIST_OPEN).append(mutableComponent).append(LIST_TYPE_SEPARATOR);
        int[] nArray = intArrayTag.getAsIntArray();
        for (int i = 0; i < nArray.length; ++i) {
            mutableComponent2.append(ELEMENT_SPACING).append(new TextComponent(String.valueOf(nArray[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
            if (i == nArray.length - 1) continue;
            mutableComponent2.append(ELEMENT_SEPARATOR);
        }
        mutableComponent2.append(LIST_CLOSE);
        this.result = mutableComponent2;
    }

    @Override
    public void visitLongArray(LongArrayTag longArrayTag) {
        MutableComponent mutableComponent = new TextComponent("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent2 = new TextComponent(LIST_OPEN).append(mutableComponent).append(LIST_TYPE_SEPARATOR);
        long[] lArray = longArrayTag.getAsLongArray();
        for (int i = 0; i < lArray.length; ++i) {
            MutableComponent mutableComponent3 = new TextComponent(String.valueOf(lArray[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            mutableComponent2.append(ELEMENT_SPACING).append(mutableComponent3).append(mutableComponent);
            if (i == lArray.length - 1) continue;
            mutableComponent2.append(ELEMENT_SEPARATOR);
        }
        mutableComponent2.append(LIST_CLOSE);
        this.result = mutableComponent2;
    }

    @Override
    public void visitList(ListTag listTag) {
        if (listTag.isEmpty()) {
            this.result = new TextComponent("[]");
            return;
        }
        if (INLINE_ELEMENT_TYPES.contains(listTag.getElementType()) && listTag.size() <= 8) {
            String string = ELEMENT_SEPARATOR + ELEMENT_SPACING;
            TextComponent textComponent = new TextComponent(LIST_OPEN);
            for (int i = 0; i < listTag.size(); ++i) {
                if (i != 0) {
                    textComponent.append(string);
                }
                textComponent.append(new TextComponentTagVisitor(this.indentation, this.depth).visit(listTag.get(i)));
            }
            textComponent.append(LIST_CLOSE);
            this.result = textComponent;
            return;
        }
        TextComponent textComponent = new TextComponent(LIST_OPEN);
        if (!this.indentation.isEmpty()) {
            textComponent.append(NEWLINE);
        }
        for (int i = 0; i < listTag.size(); ++i) {
            TextComponent textComponent2 = new TextComponent(Strings.repeat((String)this.indentation, (int)(this.depth + 1)));
            textComponent2.append(new TextComponentTagVisitor(this.indentation, this.depth + 1).visit(listTag.get(i)));
            if (i != listTag.size() - 1) {
                textComponent2.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? ELEMENT_SPACING : NEWLINE);
            }
            textComponent.append(textComponent2);
        }
        if (!this.indentation.isEmpty()) {
            textComponent.append(NEWLINE).append(Strings.repeat((String)this.indentation, (int)this.depth));
        }
        textComponent.append(LIST_CLOSE);
        this.result = textComponent;
    }

    @Override
    public void visitCompound(CompoundTag compoundTag) {
        Object object;
        if (compoundTag.isEmpty()) {
            this.result = new TextComponent("{}");
            return;
        }
        TextComponent textComponent = new TextComponent(STRUCT_OPEN);
        Collection<String> collection = compoundTag.getAllKeys();
        if (LOGGER.isDebugEnabled()) {
            object = Lists.newArrayList(compoundTag.getAllKeys());
            Collections.sort(object);
            collection = object;
        }
        if (!this.indentation.isEmpty()) {
            textComponent.append(NEWLINE);
        }
        object = collection.iterator();
        while (object.hasNext()) {
            String string = (String)object.next();
            MutableComponent mutableComponent = new TextComponent(Strings.repeat((String)this.indentation, (int)(this.depth + 1))).append(TextComponentTagVisitor.handleEscapePretty(string)).append(NAME_VALUE_SEPARATOR).append(ELEMENT_SPACING).append(new TextComponentTagVisitor(this.indentation, this.depth + 1).visit(compoundTag.get(string)));
            if (object.hasNext()) {
                mutableComponent.append(ELEMENT_SEPARATOR).append(this.indentation.isEmpty() ? ELEMENT_SPACING : NEWLINE);
            }
            textComponent.append(mutableComponent);
        }
        if (!this.indentation.isEmpty()) {
            textComponent.append(NEWLINE).append(Strings.repeat((String)this.indentation, (int)this.depth));
        }
        textComponent.append(STRUCT_CLOSE);
        this.result = textComponent;
    }

    protected static Component handleEscapePretty(String string) {
        if (SIMPLE_VALUE.matcher(string).matches()) {
            return new TextComponent(string).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        }
        String string2 = StringTag.quoteAndEscape(string);
        String string3 = string2.substring(0, 1);
        MutableComponent mutableComponent = new TextComponent(string2.substring(1, string2.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        return new TextComponent(string3).append(mutableComponent).append(string3);
    }

    @Override
    public void visitEnd(EndTag endTag) {
        this.result = TextComponent.EMPTY;
    }
}

