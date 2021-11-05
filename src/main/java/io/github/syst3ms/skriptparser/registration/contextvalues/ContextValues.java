package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ContextValues {
    private static final List<ContextValue<?, ?>> contextValues = new ArrayList<>();

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
    public static List<ContextValue<?, ?>> getContextValues(Class<? extends TriggerContext> ctx) {
        return contextValues.stream()
                .filter(val -> val.getContext().isAssignableFrom(ctx))
                .collect(Collectors.toList());
    }
}
