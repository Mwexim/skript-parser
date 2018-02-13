package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.interfaces.DynamicNumberExpression;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.util.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SyntaxManager {
    private static SyntaxManager instance = new SyntaxManager();
    private static MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
    private static MultiMap<String, SyntaxInfo<?>> syntaxes = new MultiMap<>();
    private static List<SyntaxInfo<? extends Effect>> effects = new ArrayList<>();

    private SyntaxManager() {
    }

    public static SyntaxManager getInstance() {
        return instance;
    }

    public static void register(SkriptRegistration reg) {
        for (SyntaxInfo<? extends Effect> info : reg.getEffects()) {
            effects.add(info);
            syntaxes.putOne(reg.getRegisterer(), info);
        }
        for (Map.Entry<Class<?>, List<ExpressionInfo<?, ?>>> entry : reg.getExpressions().entrySet()) {
            Class<?> key = entry.getKey();
            List<ExpressionInfo<?, ?>> infos = entry.getValue();
            for (ExpressionInfo<?, ?> info : infos) {
                expressions.putOne(key, info);
                syntaxes.putOne(reg.getRegisterer(), info);
            }
        }
    }

    public static Iterable<SyntaxInfo<?>> getAddonSyntaxes(String name) {
        List<SyntaxInfo<?>> infos = syntaxes.get(name);
        return infos == null ? Collections.emptyList() : infos;
    }

    public static Collection<ExpressionInfo<?, ?>> getAllExpressions() {
        return expressions.getAllValues();
    }

    public static  <T> List<ExpressionInfo<?, ?>> getExpressionsByReturnType(Class<? extends T> c) {
        List<ExpressionInfo<?, ?>> infos = new ArrayList<>();
        for (Class<?> returnType : expressions.keySet()) {
            if (returnType.isAssignableFrom(c)) {
                infos.addAll(expressions.get(returnType));
            }
        }
        return infos;
    }

    public static Collection<SyntaxInfo<? extends Effect>> getEffects() {
        return effects;
    }
}
