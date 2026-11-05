/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Pattern LINE_PATTERN = Pattern.compile("\\r\\n|\\v");
    private static final Pattern LINE_END_PATTERN = Pattern.compile("(?:\\r\\n|\\v)$");

    public static String formatTickDuration(int n) {
        int n2 = n / 20;
        int n3 = n2 / 60;
        if ((n2 %= 60) < 10) {
            return n3 + ":0" + n2;
        }
        return n3 + ":" + n2;
    }

    public static String stripColor(String string) {
        return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        return StringUtils.isEmpty((CharSequence)string);
    }

    public static String truncateStringIfNecessary(String string, int n, boolean bl) {
        if (string.length() <= n) {
            return string;
        }
        if (bl && n > 3) {
            return string.substring(0, n - 3) + "...";
        }
        return string.substring(0, n);
    }

    public static int lineCount(String string) {
        if (string.isEmpty()) {
            return 0;
        }
        Matcher matcher = LINE_PATTERN.matcher(string);
        int n = 1;
        while (matcher.find()) {
            ++n;
        }
        return n;
    }

    public static boolean endsWithNewLine(String string) {
        return LINE_END_PATTERN.matcher(string).find();
    }
}

