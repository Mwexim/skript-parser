package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextParameter;

/**
 * The script loading context, which corresponds to running code inside {@code public static void main(String[] args)}
 * in Java.
 */
public class TestContext implements TriggerContext {

    @Override
    public String getName() {
        return "main";
    }

    @ContextParameter(name = "standalone", standalone = true)
    public String standaloneValue() {
        return "It works";
    }

    @ContextParameter(name = "should_not_work_because_of_underscore")
    public String invalidValue() {
        return "It should not work";
    }

    @ContextParameter(name = "primitive")
    public int primitiveValue() {
        return 0;
    }

    // We want to see if inherited methods are able to register values as well.
    public static class RealTestContext extends TestContext { /* Nothing */ }
}
