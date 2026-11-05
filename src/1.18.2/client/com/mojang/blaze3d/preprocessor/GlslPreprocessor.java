/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package com.mojang.blaze3d.preprocessor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

public abstract class GlslPreprocessor {
    private static final String C_COMMENT = "/\\*(?:[^*]|\\*+[^*/])*\\*+/";
    private static final String LINE_COMMENT = "//[^\\v]*";
    private static final Pattern REGEX_MOJ_IMPORT = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))");
    private static final Pattern REGEX_VERSION = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(\\d+))\\b");
    private static final Pattern REGEX_ENDS_WITH_WHITESPACE = Pattern.compile("(?:^|\\v)(?:\\s|/\\*(?:[^*]|\\*+[^*/])*\\*+/|(//[^\\v]*))*\\z");

    public List<String> process(String string) {
        Context context = new Context();
        List<String> list = this.processImports(string, context, "");
        list.set(0, this.setVersion(list.get(0), context.glslVersion));
        return list;
    }

    private List<String> processImports(String string, Context context, String string2) {
        String string3;
        int n = context.sourceId;
        int n2 = 0;
        String string4 = "";
        ArrayList arrayList = Lists.newArrayList();
        Matcher matcher = REGEX_MOJ_IMPORT.matcher(string);
        while (matcher.find()) {
            int n3;
            boolean bl;
            if (GlslPreprocessor.isDirectiveDisabled(string, matcher, n2)) continue;
            string3 = matcher.group(2);
            boolean bl2 = bl = string3 != null;
            if (!bl) {
                string3 = matcher.group(3);
            }
            if (string3 == null) continue;
            String string5 = string.substring(n2, matcher.start(1));
            String string6 = string2 + string3;
            Object object = this.applyImport(bl, string6);
            if (!Strings.isNullOrEmpty((String)object)) {
                if (!StringUtil.endsWithNewLine((String)object)) {
                    object = (String)object + System.lineSeparator();
                }
                ++context.sourceId;
                n3 = context.sourceId;
                List<String> list = this.processImports((String)object, context, bl ? FileUtil.getFullResourcePath(string6) : "");
                list.set(0, String.format(Locale.ROOT, "#line %d %d\n%s", 0, n3, this.processVersions(list.get(0), context)));
                if (!StringUtils.isBlank((CharSequence)string5)) {
                    arrayList.add(string5);
                }
                arrayList.addAll(list);
            } else {
                String string7 = bl ? String.format("/*#moj_import \"%s\"*/", string3) : String.format("/*#moj_import <%s>*/", string3);
                arrayList.add(string4 + string5 + string7);
            }
            n3 = StringUtil.lineCount(string.substring(0, matcher.end(1)));
            string4 = String.format(Locale.ROOT, "#line %d %d", n3, n);
            n2 = matcher.end(1);
        }
        string3 = string.substring(n2);
        if (!StringUtils.isBlank((CharSequence)string3)) {
            arrayList.add(string4 + string3);
        }
        return arrayList;
    }

    private String processVersions(String string, Context context) {
        Matcher matcher = REGEX_VERSION.matcher(string);
        if (matcher.find() && GlslPreprocessor.isDirectiveEnabled(string, matcher)) {
            context.glslVersion = Math.max(context.glslVersion, Integer.parseInt(matcher.group(2)));
            return string.substring(0, matcher.start(1)) + "/*" + string.substring(matcher.start(1), matcher.end(1)) + "*/" + string.substring(matcher.end(1));
        }
        return string;
    }

    private String setVersion(String string, int n) {
        Matcher matcher = REGEX_VERSION.matcher(string);
        if (matcher.find() && GlslPreprocessor.isDirectiveEnabled(string, matcher)) {
            return string.substring(0, matcher.start(2)) + Math.max(n, Integer.parseInt(matcher.group(2))) + string.substring(matcher.end(2));
        }
        return string;
    }

    private static boolean isDirectiveEnabled(String string, Matcher matcher) {
        return !GlslPreprocessor.isDirectiveDisabled(string, matcher, 0);
    }

    private static boolean isDirectiveDisabled(String string, Matcher matcher, int n) {
        int n2 = matcher.start() - n;
        if (n2 == 0) {
            return false;
        }
        Matcher matcher2 = REGEX_ENDS_WITH_WHITESPACE.matcher(string.substring(n, matcher.start()));
        if (!matcher2.find()) {
            return true;
        }
        int n3 = matcher2.end(1);
        return n3 == matcher.start();
    }

    @Nullable
    public abstract String applyImport(boolean var1, String var2);

    static final class Context {
        int glslVersion;
        int sourceId;

        Context() {
        }
    }
}

