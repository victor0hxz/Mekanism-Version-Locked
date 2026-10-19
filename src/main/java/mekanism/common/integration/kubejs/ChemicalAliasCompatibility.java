package mekanism.common.integration.kubejs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/** Compatibility for legacy Mekanism class aliases on Rhino versions with broken block const declarations. */
public final class ChemicalAliasCompatibility {
    private static final Pattern ALIAS = Pattern.compile("(?m)^[\\t ]*(const)[\\t ]+(Chemical|ChemicalBuilder)[\\t ]*=[\\t ]*Java\\.loadClass\\([\\t ]*(['\"])(mekanism\\.api\\.chemical\\.(?:Chemical|ChemicalBuilder))\\3[\\t ]*\\)[\\t ]*;?[\\t ]*(?://[^\\r\\n]*)?\\r?$");
    private static final Pattern ASSIGNMENT = Pattern.compile("\\b(?:Chemical|ChemicalBuilder)\\s*(?:\\+\\+|--|(?:[+\\-*/%&|^]|<<|>>>?|&&|\\|\\||\\?\\?)?=(?!=))");
    private static final Pattern PREFIX_WRITE = Pattern.compile("(?:\\+\\+|--)\\s*(?:Chemical|ChemicalBuilder)\\b");
    private static final Pattern DESTRUCTURED_WRITE = Pattern.compile("(?s)(?:\\[[^\\]]*\\b(?:Chemical|ChemicalBuilder)\\b[^\\]]*\\]|\\{[^}]*\\b(?:Chemical|ChemicalBuilder)\\b[^}]*\\})\\s*=");
    private static final Pattern TEMPLATE_ALIAS = Pattern.compile("`(?:\\\\.|[^`])*\\b(?:Chemical|ChemicalBuilder)\\b(?:\\\\.|[^`])*`");


    private ChemicalAliasCompatibility() { }

    public static String normalize(String sourceName, String source) {
        return normalize(sourceName, source, BrokenRhino.DETECTED);
    }

    static String normalize(String sourceName, String source, boolean brokenRhino) {
        String name = sourceName.replace('\\', '/');
        if (!brokenRhino || !name.endsWith(".js") || name.contains("/../") ||
              !(name.startsWith("startup_scripts:mekanism/") || name.startsWith("startup_scripts/mekanism/") || name.contains("/startup_scripts/mekanism/"))) {
            return source;
        }
        var declarations = new ArrayList<Integer>(2);
        var assignments = new ArrayList<Integer>(2);
        boolean chemical = false;
        boolean builder = false;
        var matcher = ALIAS.matcher(source);
        while (matcher.find()) {
            if (!isCodeAt(source, matcher.start(1))) {
                continue;
            }
            String alias = matcher.group(2);
            if (!matcher.group(4).equals("mekanism.api.chemical." + alias) || (alias.equals("Chemical") ? chemical : builder)) {
                return source;
            }
            if (alias.equals("Chemical")) {
                chemical = true;
            } else {
                builder = true;
            }
            declarations.add(matcher.start(1));
            assignments.add(matcher.start(2));
        }
        if (!chemical || !builder) {
            return source;
        }
        matcher = ASSIGNMENT.matcher(source);
        while (matcher.find()) {
            if (isCodeAt(source, matcher.start()) && !assignments.contains(matcher.start())) {
                return source;
            }
        }
        for (Pattern writes : new Pattern[]{PREFIX_WRITE, DESTRUCTURED_WRITE}) {
            matcher = writes.matcher(source);
            while (matcher.find()) {
                if (isCodeAt(source, matcher.start())) return source;
            }
        }
        // Template interpolation is intentionally conservative: never lower aliases used there.
        matcher = TEMPLATE_ALIAS.matcher(source);
        while (matcher.find()) {
            if (isCodeAt(source, matcher.start())) return source;
        }
        var normalized = new StringBuilder(source);
        for (int start : declarations) {
            // The stock aliases are never reassigned; padding preserves diagnostic line and column positions.
            normalized.replace(start, start + 5, "let  ");
        }
        return normalized.toString();
    }

    private static boolean isCodeAt(String source, int end) {
        char quote = 0;
        boolean lineComment = false;
        boolean blockComment = false;
        for (int i = 0; i < end; i++) {
            char c = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : 0;
            if (lineComment) {
                if (c == '\n' || c == '\r') lineComment = false;
            } else if (blockComment) {
                if (c == '*' && next == '/') { blockComment = false; i++; }
            } else if (quote != 0) {
                if (c == '\\') i++;
                else if (c == quote) quote = 0;
            } else if (c == '/' && next == '/') {
                lineComment = true;
                i++;
            } else if (c == '/' && next == '*') {
                blockComment = true;
                i++;
            } else if (c == '\'' || c == '"' || c == '`') {
                quote = c;
            }
        }
        return quote == 0 && !lineComment && !blockComment;
    }

    private static final class BrokenRhino {
        private static final boolean DETECTED = probe();

        private static boolean probe() {
            try {
                Class<?> factoryType = Class.forName("dev.latvian.mods.rhino.ContextFactory");
                Class<?> contextType = Class.forName("dev.latvian.mods.rhino.Context");
                Class<?> scriptableType = Class.forName("dev.latvian.mods.rhino.Scriptable");
                Object context = factoryType.getMethod("enter").invoke(factoryType.getConstructor().newInstance());
                Object scope = contextType.getMethod("initStandardObjects").invoke(context);
                contextType.getMethod("evaluateString", scriptableType, String.class, String.class, int.class, Object.class)
                      .invoke(context, scope, "if(true){const __mekanism_block_const_probe=1;}", "mekanism-const-probe", 1, null);
            } catch (InvocationTargetException error) {
                Throwable cause = error.getCause();
                return cause.getClass().getName().equals("dev.latvian.mods.rhino.EvaluatorException") &&
                      cause.getMessage().contains("redeclaration of var __mekanism_block_const_probe");
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Fail closed when the optional engine cannot be probed.
            }
            return false;
        }
    }
}
