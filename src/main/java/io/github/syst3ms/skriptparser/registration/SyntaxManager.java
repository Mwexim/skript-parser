package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.util.classes.MultiMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SyntaxManager {
    /**
     * The ordering describing the order in which syntaxes should be tested during parsing
     */
    public static final Comparator<? super SyntaxInfo<?>> INFO_COMPARATOR = (i, i2) -> {
        if (i.getPriority() != i2.getPriority()) {
            return i2.getPriority() - i.getPriority();
        } else {
            return i2.getPatterns().size() - i.getPatterns().size();
        }
    };
    private static final MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
    private static final List<SyntaxInfo<? extends Effect>> effects = new ArrayList<>();
    private static final List<SyntaxInfo<? extends CodeSection>> sections = new ArrayList<>();
    private static final List<SkriptEventInfo<?>> triggers = new ArrayList<>();

    static void register(SkriptRegistration reg) {
        effects.addAll(reg.getEffects());
        effects.sort(INFO_COMPARATOR);
        sections.addAll(reg.getSections());
        sections.sort(INFO_COMPARATOR);
        triggers.addAll(reg.getEvents());
        triggers.sort(INFO_COMPARATOR);
        for (var entry : reg.getExpressions().entrySet()) {
            var key = entry.getKey();
            var infos = entry.getValue();
            for (var info : infos) {
                expressions.putOne(key, info);
            }
        }
    }

    /**
     * @return a list of all currently registered expressions
     */
    public static List<ExpressionInfo<?, ?>> getAllExpressions() {
        var expressionInfos = expressions.getAllValues();
        expressionInfos.sort(INFO_COMPARATOR);
        return expressionInfos;
    }

    /**
     * @param expr the expression instance
     * @param <E> the expression class
     * @param <T> the expression return type
     * @return the {@link ExpressionInfo} corresponding to the given {@link Expression} instance
     */
    @SuppressWarnings("unchecked")
    public static <E extends Expression<T>, T> Optional<? extends ExpressionInfo<E, T>> getExpressionExact(Expression<T> expr) {
        Class<?> c = expr.getSource().getClass();
        for (var info : SyntaxManager.getAllExpressions()) {
            if (info.getSyntaxClass() == c) {
                return Optional.of((ExpressionInfo<E, T>) info);
            }
        }
        return Optional.empty();
    }

    /**
     * @return a list of all currently registered sections
     */
    public static List<SyntaxInfo<? extends CodeSection>> getSections() {
        return sections;
    }

    /**
     * @return a list of all currently registered effects
     */
    public static List<SyntaxInfo<? extends Effect>> getEffects() {
        return effects;
    }

    /**
     * @return a list of all currently registered events
     */
    public static List<SkriptEventInfo<?>> getEvents() {
        return triggers;
    }
}
