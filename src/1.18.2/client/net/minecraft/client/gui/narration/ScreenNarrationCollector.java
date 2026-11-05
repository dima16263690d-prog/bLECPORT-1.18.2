/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.gui.narration;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationThunk;

public class ScreenNarrationCollector {
    int generation;
    final Map<EntryKey, NarrationEntry> entries = Maps.newTreeMap(Comparator.comparing(entryKey -> entryKey.type).thenComparing(entryKey -> entryKey.depth));

    public void update(Consumer<NarrationElementOutput> consumer) {
        ++this.generation;
        consumer.accept(new Output(0));
    }

    public String collectNarrationText(boolean bl) {
        final StringBuilder stringBuilder = new StringBuilder();
        Consumer<String> consumer = new Consumer<String>(){
            private boolean firstEntry = true;

            @Override
            public void accept(String string) {
                if (!this.firstEntry) {
                    stringBuilder.append(". ");
                }
                this.firstEntry = false;
                stringBuilder.append(string);
            }

            @Override
            public /* synthetic */ void accept(Object object) {
                this.accept((String)object);
            }
        };
        this.entries.forEach((entryKey, narrationEntry) -> {
            if (narrationEntry.generation == this.generation && (bl || !narrationEntry.alreadyNarrated)) {
                narrationEntry.contents.getText(consumer);
                narrationEntry.alreadyNarrated = true;
            }
        });
        return stringBuilder.toString();
    }

    class Output
    implements NarrationElementOutput {
        private final int depth;

        Output(int n) {
            this.depth = n;
        }

        @Override
        public void add(NarratedElementType narratedElementType, NarrationThunk<?> narrationThunk) {
            ScreenNarrationCollector.this.entries.computeIfAbsent(new EntryKey(narratedElementType, this.depth), entryKey -> new NarrationEntry()).update(ScreenNarrationCollector.this.generation, narrationThunk);
        }

        @Override
        public NarrationElementOutput nest() {
            return new Output(this.depth + 1);
        }
    }

    static class NarrationEntry {
        NarrationThunk<?> contents = NarrationThunk.EMPTY;
        int generation = -1;
        boolean alreadyNarrated;

        NarrationEntry() {
        }

        public NarrationEntry update(int n, NarrationThunk<?> narrationThunk) {
            if (!this.contents.equals(narrationThunk)) {
                this.contents = narrationThunk;
                this.alreadyNarrated = false;
            } else if (this.generation + 1 != n) {
                this.alreadyNarrated = false;
            }
            this.generation = n;
            return this;
        }
    }

    static class EntryKey {
        final NarratedElementType type;
        final int depth;

        EntryKey(NarratedElementType narratedElementType, int n) {
            this.type = narratedElementType;
            this.depth = n;
        }
    }
}

