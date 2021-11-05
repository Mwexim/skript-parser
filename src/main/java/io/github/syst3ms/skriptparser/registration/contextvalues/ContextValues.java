package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContextValues {
    // TODO make this pretty
    private static final SkriptAddon methodAddon = new SkriptAddon() {
        @Override
        public void handleTrigger(Trigger trigger) {
        }
    };

    private static final List<ContextValue<?, ?>> contextValues = new ArrayList<>();
    private static final Map<Class<? extends TriggerContext>, List<ContextValue<?, ?>>> cachedContextValues = new HashMap<>();

    public static void register(SkriptRegistration reg) {
        contextValues.addAll(reg.getContextValues());
    }

    /**
     * @return a list of all currently registered context values
     */
    public static List<ContextValue<?, ?>> getContextValues() {
        return contextValues;
    }

    /**
     * Returns a list of all currently registered context values for a given {@link TriggerContext},
     * omitting values that have been excluded specifically.
     * @param ctx the context class
     * @return a list with the context values
     */
    public static List<ContextValue<?, ?>> getContextValues(Class<? extends TriggerContext> ctx) {
        return contextValues.stream()
                .filter(val -> val.getContext().isAssignableFrom(ctx))
                .collect(Collectors.toList());
    }
}
