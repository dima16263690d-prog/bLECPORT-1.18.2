/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.Arrays
 *  it.unimi.dsi.fastutil.Swapper
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntComparator
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  org.slf4j.Logger
 */
package net.minecraft.client.searchtree;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.slf4j.Logger;

public class SuffixArray<T> {
    private static final boolean DEBUG_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
    private static final boolean DEBUG_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int END_OF_TEXT_MARKER = -1;
    private static final int END_OF_DATA = -2;
    protected final List<T> list = Lists.newArrayList();
    private final IntList chars = new IntArrayList();
    private final IntList wordStarts = new IntArrayList();
    private IntList suffixToT = new IntArrayList();
    private IntList offsets = new IntArrayList();
    private int maxStringLength;

    public void add(T t, String string) {
        this.maxStringLength = Math.max(this.maxStringLength, string.length());
        int n = this.list.size();
        this.list.add(t);
        this.wordStarts.add(this.chars.size());
        for (int i = 0; i < string.length(); ++i) {
            this.suffixToT.add(n);
            this.offsets.add(i);
            this.chars.add((int)string.charAt(i));
        }
        this.suffixToT.add(n);
        this.offsets.add(string.length());
        this.chars.add(-1);
    }

    public void generate() {
        int n3;
        int n4 = this.chars.size();
        int[] nArray = new int[n4];
        int[] nArray2 = new int[n4];
        int[] nArray3 = new int[n4];
        int[] nArray4 = new int[n4];
        IntComparator intComparator = (n, n2) -> {
            if (nArray2[n] == nArray2[n2]) {
                return Integer.compare(nArray3[n], nArray3[n2]);
            }
            return Integer.compare(nArray2[n], nArray2[n2]);
        };
        Swapper swapper = (n, n2) -> {
            if (n != n2) {
                int n3 = nArray2[n];
                nArray[n] = nArray2[n2];
                nArray[n2] = n3;
                n3 = nArray3[n];
                nArray2[n] = nArray3[n2];
                nArray2[n2] = n3;
                n3 = nArray4[n];
                nArray3[n] = nArray4[n2];
                nArray3[n2] = n3;
            }
        };
        for (n3 = 0; n3 < n4; ++n3) {
            nArray[n3] = this.chars.getInt(n3);
        }
        n3 = 1;
        int n5 = Math.min(n4, this.maxStringLength);
        while (n3 * 2 < n5) {
            int n6;
            for (n6 = 0; n6 < n4; ++n6) {
                nArray2[n6] = nArray[n6];
                nArray3[n6] = n6 + n3 < n4 ? nArray[n6 + n3] : -2;
                nArray4[n6] = n6;
            }
            it.unimi.dsi.fastutil.Arrays.quickSort((int)0, (int)n4, (IntComparator)intComparator, (Swapper)swapper);
            for (n6 = 0; n6 < n4; ++n6) {
                nArray[nArray4[n6]] = n6 > 0 && nArray2[n6] == nArray2[n6 - 1] && nArray3[n6] == nArray3[n6 - 1] ? nArray[nArray4[n6 - 1]] : n6;
            }
            n3 *= 2;
        }
        IntList intList = this.suffixToT;
        IntList intList2 = this.offsets;
        this.suffixToT = new IntArrayList(intList.size());
        this.offsets = new IntArrayList(intList2.size());
        for (int i = 0; i < n4; ++i) {
            int n7 = nArray4[i];
            this.suffixToT.add(intList.getInt(n7));
            this.offsets.add(intList2.getInt(n7));
        }
        if (DEBUG_ARRAY) {
            this.print();
        }
    }

    private void print() {
        for (int i = 0; i < this.suffixToT.size(); ++i) {
            LOGGER.debug("{} {}", (Object)i, (Object)this.getString(i));
        }
        LOGGER.debug("");
    }

    private String getString(int n) {
        int n2 = this.offsets.getInt(n);
        int n3 = this.wordStarts.getInt(this.suffixToT.getInt(n));
        StringBuilder stringBuilder = new StringBuilder();
        int n4 = 0;
        while (n3 + n4 < this.chars.size()) {
            int n5;
            if (n4 == n2) {
                stringBuilder.append('^');
            }
            if ((n5 = this.chars.getInt(n3 + n4)) == -1) break;
            stringBuilder.append((char)n5);
            ++n4;
        }
        return stringBuilder.toString();
    }

    private int compare(String string, int n) {
        int n2 = this.wordStarts.getInt(this.suffixToT.getInt(n));
        int n3 = this.offsets.getInt(n);
        for (int i = 0; i < string.length(); ++i) {
            char c;
            int n4 = this.chars.getInt(n2 + n3 + i);
            if (n4 == -1) {
                return 1;
            }
            char c2 = string.charAt(i);
            if (c2 < (c = (char)n4)) {
                return -1;
            }
            if (c2 <= c) continue;
            return 1;
        }
        return 0;
    }

    public List<T> search(String string) {
        int n;
        int n2;
        int n3 = this.suffixToT.size();
        int n4 = 0;
        int n5 = n3;
        while (n4 < n5) {
            n2 = n4 + (n5 - n4) / 2;
            n = this.compare(string, n2);
            if (DEBUG_COMPARISONS) {
                LOGGER.debug("comparing lower \"{}\" with {} \"{}\": {}", new Object[]{string, n2, this.getString(n2), n});
            }
            if (n > 0) {
                n4 = n2 + 1;
                continue;
            }
            n5 = n2;
        }
        if (n4 < 0 || n4 >= n3) {
            return Collections.emptyList();
        }
        n2 = n4;
        n5 = n3;
        while (n4 < n5) {
            n = n4 + (n5 - n4) / 2;
            int n6 = this.compare(string, n);
            if (DEBUG_COMPARISONS) {
                LOGGER.debug("comparing upper \"{}\" with {} \"{}\": {}", new Object[]{string, n, this.getString(n), n6});
            }
            if (n6 >= 0) {
                n4 = n + 1;
                continue;
            }
            n5 = n;
        }
        n = n4;
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
        for (int i = n2; i < n; ++i) {
            intOpenHashSet.add(this.suffixToT.getInt(i));
        }
        int[] nArray = intOpenHashSet.toIntArray();
        Arrays.sort(nArray);
        LinkedHashSet linkedHashSet = Sets.newLinkedHashSet();
        for (int n7 : nArray) {
            linkedHashSet.add(this.list.get(n7));
        }
        return Lists.newArrayList((Iterable)linkedHashSet);
    }
}

