package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.util.MultiMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SyntaxManager {
    public static final Comparator<? super SyntaxInfo<?>> INFO_COMPARATOR = (i, i2) -> {
        if (i.getPriority() != i2.getPriority()) {
            return i2.getPriority() - i.getPriority();
        } else {
            return i2.getPatterns().size() - i.getPatterns().size();
        }
    };
    private static MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
    private static List<SyntaxInfo<? extends Effect>> effects = new ArrayList<>();
    private static List<SyntaxInfo<? extends CodeSection>> sections = new ArrayList<>();
    private static List<SkriptEventInfo<?>> triggers = new ArrayList<>();

    public static List<SyntaxInfo<? extends CodeSection>> getSections() {
        return sections;
    }

    public static void register(SkriptRegistration reg) {
        effects.addAll(reg.getEffects());
        effects.sort(INFO_COMPARATOR);
        sections.addAll(reg.getSections());
        sections.sort(INFO_COMPARATOR);
        triggers.addAll(reg.getEvents());
        triggers.sort(INFO_COMPARATOR);
        for (Map.Entry<Class<?>, List<ExpressionInfo<?, ?>>> entry : reg.getExpressions().entrySet()) {
            Class<?> key = entry.getKey();
            List<ExpressionInfo<?, ?>> infos = entry.getValue();
            for (ExpressionInfo<?, ?> info : infos) {
                expressions.putOne(key, info);
            }
        }
    }

    public static List<ExpressionInfo<?, ?>> getAllExpressions() {
        List<ExpressionInfo<?, ?>> expressionInfos = expressions.getAllValues();
        expressionInfos.sort(INFO_COMPARATOR);
        return expressionInfos;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends Expression<T>, T> ExpressionInfo<E, T> getExpressionExact(Expression<T> expr) {
        Class<?> c = expr.getSource().getClass();
        for (ExpressionInfo<?, ?> info : SyntaxManager.getAllExpressions()) {
            if (info.getSyntaxClass() == c) {
                return (ExpressionInfo<E, T>) info;
            }
        }
        return null;
    }

    public static List<SyntaxInfo<? extends Effect>> getEffects() {
        return effects;
    }

    public static List<SkriptEventInfo<?>> getTriggers() {
        return triggers;
    }
}
