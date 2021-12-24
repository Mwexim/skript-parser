package io.github.syst3ms.skriptparser.registration.context;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.CollectionUtils;

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
     * Returns an unmodifiable list with all the registered context values.
     * @return a list with all context values
     */
    public static List<ContextValue<?, ?>> getContextValues() {
        return Collections.unmodifiableList(contextValues);
    }

    /**
     * Returns a list with all the registered context values for a given {@link TriggerContext},
     * excluding values that explicitly require it.
     * @param ctx the context class
     * @return a list with the applicable context values
     */
    public static List<ContextValue<?, ?>> getContextValues(Class<? extends TriggerContext> ctx) {
        return contextValues.stream()
                .filter(val -> val.getContext().isAssignableFrom(ctx))
                .filter(val -> !CollectionUtils.contains(val.getExcluded(), val.getContext()))
                .collect(Collectors.toList());
    }
}
