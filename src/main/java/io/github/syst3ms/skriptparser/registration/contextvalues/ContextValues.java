package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

import java.util.ArrayList;
import java.util.List;

public class ContextValues {

    private static final List<ContextValue<?>> contextValues = new ArrayList<>();

    public static void register(SkriptRegistration reg) {
        contextValues.addAll(reg.getContextValues());
    }

    /**
     * @return a list of all currently registered context values
     */
    public static List<ContextValue<?>> getContextValues() {
        return contextValues;
    }

}
