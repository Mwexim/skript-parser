package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValueInfo;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValueInfo.Usage;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValueMethod;

import java.time.Duration;

/**
 * The script loading context, which corresponds to running code inside {@code public static void main(String[] args)}
 * in Java.
 */
public class TestContext implements TriggerContext {
    @Override
    public String getName() {
        return "main";
    }

    @ContextValueMethod(name = "standalone", usage = Usage.STANDALONE_ONLY)
    public String standaloneValue() {
        return "It works";
    }

    @ContextValueMethod(name = "[some] pattern value", state = ContextValueInfo.State.PAST, usage = Usage.STANDALONE_ONLY)
    public String patternValue() {
        return "It works too";
    }

    @ContextValueMethod(name = "primitive")
    public int primitiveValue() {
        return 0;
    }

    public Duration oneDay() {
        return Duration.ofDays(1);
    }

    // We want to see if inherited methods are able to register values as well.
    public static class SubTestContext extends TestContext { /* Nothing */ }
}
