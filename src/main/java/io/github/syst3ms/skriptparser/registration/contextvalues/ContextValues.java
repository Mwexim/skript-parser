package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContextValues {
    private static final List<ContextValue<?>> contextValues = new ArrayList<>();
    private static final Map<Class<? extends TriggerContext>, List<ContextValue<?>>> cachedContextValues = new HashMap<>();

    public static void register(SkriptRegistration reg) {
        contextValues.addAll(reg.getContextValues());
    }

    /**
     * @return a list of all currently registered context values
     */
    public static List<ContextValue<?>> getContextValues() {
        return contextValues;
    }

    /**
     * Returns a list of all currently registered context values for a given {@link TriggerContext},
     * and also all context values of that context, created by using the {@link ContextValueMethod}
     * annotation.
     * @param ctx the context class
     * @return a list with the context values
     */
    @SuppressWarnings("unchecked")
    public static List<ContextValue<?>> getContextValues(Class<? extends TriggerContext> ctx) {
        if (cachedContextValues.containsKey(ctx)) {
            return cachedContextValues.get(ctx);
        }

        // Filter all registered context values
        List<ContextValue<?>> values = contextValues.stream()
                .filter(val -> val.getContext().isAssignableFrom(ctx))
                .collect(Collectors.toList());

        // Search context values by the ContextParameter annotation
        for (var method : ctx.getMethods()) {
            if (method.getAnnotation(ContextValueMethod.class) != null
                    && method.getParameterTypes().length == 0) {
                // Less initialisation
                var annotation = method.getAnnotation(ContextValueMethod.class);
                var returnType = method.getReturnType();
                var type = TypeManager.getByClass((Class<?>) (returnType.isArray() ? returnType.getComponentType() : returnType));

                if (type.isPresent() && Pattern.matches("[a-z]+", annotation.name())) {
                    // Now all conditions have been satisfied
                    values.add(new ContextValue<>(
                            ctx,
                            (Type<Object>) type.get(),
                            returnType.isArray(),
                            annotation.name(),
                            val -> {
                                try {
                                    return returnType.isArray() ? (Object[]) method.invoke(val) : new Object[] {method.invoke(val)};
                                } catch (IllegalAccessException | InvocationTargetException ex) {
                                    throw new IllegalStateException("ContextParameter method could not be invoked");
                                }
                            },
                            annotation.state(),
                            annotation.standalone()
                    ));
                }
            }
        }
        cachedContextValues.put(ctx, values);
        return values;
    }
}
