/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayTag
extends CollectionTag<ByteTag> {
    private static final int SELF_SIZE_IN_BITS = 192;
    public static final TagType<ByteArrayTag> TYPE = new TagType.VariableSize<ByteArrayTag>(){

        @Override
        public ByteArrayTag load(DataInput dataInput, int n, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(192L);
            int n2 = dataInput.readInt();
            nbtAccounter.accountBits(8L * (long)n2);
            byte[] byArray = new byte[n2];
            dataInput.readFully(byArray);
            return new ByteArrayTag(byArray);
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
            int n = dataInput.readInt();
            byte[] byArray = new byte[n];
            dataInput.readFully(byArray);
            return streamTagVisitor.visit(byArray);
        }

        @Override
        public void skip(DataInput dataInput) throws IOException {
            dataInput.skipBytes(dataInput.readInt() * 1);
        }

        @Override
        public String getName() {
            return "BYTE[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, int n, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, n, nbtAccounter);
        }
    };
    private byte[] data;

    public ByteArrayTag(byte[] byArray) {
        this.data = byArray;
    }

    public ByteArrayTag(List<Byte> list) {
        this(ByteArrayTag.toArray(list));
    }

    private static byte[] toArray(List<Byte> list) {
        byte[] byArray = new byte[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            Byte by = list.get(i);
            byArray[i] = by == null ? (byte)0 : by;
        }
        return byArray;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        dataOutput.write(this.data);
    }

    @Override
    public byte getId() {
        return 7;
    }

    public TagType<ByteArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    @Override
    public Tag copy() {
        byte[] byArray = new byte[this.data.length];
        System.arraycopy(this.data, 0, byArray, 0, this.data.length);
        return new ByteArrayTag(byArray);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)object).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitByteArray(this);
    }

    public byte[] getAsByteArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public ByteTag get(int n) {
        return ByteTag.valueOf(this.data[n]);
    }

    @Override
    public ByteTag set(int n, ByteTag byteTag) {
        byte by = this.data[n];
        this.data[n] = byteTag.getAsByte();
        return ByteTag.valueOf(by);
    }

    @Override
    public void add(int n, ByteTag byteTag) {
        this.data = ArrayUtils.add((byte[])this.data, (int)n, (byte)byteTag.getAsByte());
    }

    @Override
    public boolean setTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data[n] = ((NumericTag)tag).getAsByte();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data = ArrayUtils.add((byte[])this.data, (int)n, (byte)((NumericTag)tag).getAsByte());
            return true;
        }
        return false;
    }

    @Override
    public ByteTag remove(int n) {
        byte by = this.data[n];
        this.data = ArrayUtils.remove((byte[])this.data, (int)n);
        return ByteTag.valueOf(by);
    }

    @Override
    public byte getElementType() {
        return 1;
    }

    @Override
    public void clear() {
        this.data = new byte[0];
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visit(this.data);
    }

    @Override
    public /* synthetic */ Tag remove(int n) {
        return this.remove(n);
    }

    @Override
    public /* synthetic */ void add(int n, Tag tag) {
        this.add(n, (ByteTag)tag);
    }

    @Override
    public /* synthetic */ Tag set(int n, Tag tag) {
        return this.set(n, (ByteTag)tag);
    }

    @Override
    public /* synthetic */ Object remove(int n) {
        return this.remove(n);
    }

    @Override
    public /* synthetic */ void add(int n, Object object) {
        this.add(n, (ByteTag)object);
    }

    @Override
    public /* synthetic */ Object set(int n, Object object) {
        return this.set(n, (ByteTag)object);
    }

    @Override
    public /* synthetic */ Object get(int n) {
        return this.get(n);
    }
}

