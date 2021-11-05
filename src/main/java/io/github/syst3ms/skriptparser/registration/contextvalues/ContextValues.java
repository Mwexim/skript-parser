package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.PatternParser;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContextValues {
    private static final List<ContextValue<?, ?>> contextValues = new ArrayList<>();
    private static final Map<Class<? extends TriggerContext>, List<ContextValue<?, ?>>> cachedContextValues = new HashMap<>();

    public static void register(SkriptRegistration reg) {
        contextValues.addAll(reg.getContextValues());
    }

    /**
     * @return a list of all currently registered context values
     */
    public static List<ContextValue<?, ?>> getContextValues() {
        return Collections.unmodifiableList(contextValues);
    }

    /**
     * Returns a list of all currently registered context values for a given {@link TriggerContext},
     * and also all context values of that context, created by using the {@link ContextValueMethod}
     * annotation.
     * Omits values that have been excluded specifically.
     * @param ctx the context class
     * @return a list with the context values
     */
    @SuppressWarnings("unchecked")
    public static List<ContextValue<?, ?>> getContextValues(Class<? extends TriggerContext> ctx) {
        if (cachedContextValues.containsKey(ctx)) {
            return cachedContextValues.get(ctx);
        }

        // Filter all registered context values
        var values = contextValues.stream()
                .filter(val -> val.getContext().isAssignableFrom(ctx))
                .filter(val -> !CollectionUtils.contains(val.getExcluded(), val.getContext()))
                .collect(Collectors.toList());

        // Search context values by the ContextParameter annotation
        for (var method : ctx.getMethods()) {
            if (method.getAnnotation(ContextValueMethod.class) == null || method.getParameterTypes().length != 0)
                continue;

            // Less initialisation
            var annotation = method.getAnnotation(ContextValueMethod.class);
            var returnType = method.getReturnType();
            var type = TypeManager.getByClass((Class<?>) (returnType.isArray() ? returnType.getComponentType() : returnType));
            var pattern = PatternParser.parsePattern(annotation.pattern(), new SkriptLogger());

            if (type.isEmpty() || pattern.isEmpty())
                continue;

            // Now all conditions have been satisfied
            values.add(new ContextValue<>(
                    ctx,
                    (Type<Object>) type.get(),
                    returnType.isArray(),
                    pattern.get(),
                    val -> {
                        try {
                            return returnType.isArray() ? (Object[]) method.invoke(val) : new Object[]{method.invoke(val)};
                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            throw new IllegalStateException("ContextParameter method could not be invoked");
                        }
                    },
                    annotation.state(),
                    annotation.usage()
            ));
        }
        cachedContextValues.put(ctx, values);

        return contextValues.stream()
                .filter(val -> val.getContext().isAssignableFrom(ctx))
                .collect(Collectors.toList());
    }
}
