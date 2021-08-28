package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.properties.ConditionalType;
import io.github.syst3ms.skriptparser.lang.properties.PropertyConditional;

/**
 * Check if a given expression is set (null on the Java side) or not.
 *
 * @name Is Set
 * @type CONDITION
 * @pattern %~objects% (is|are)[ not|n't] set
 * @since ALPHA
 * @author Syst3ms
 */
public class CondExprIsSet extends PropertyConditional<Object> {
    static {
        Parser.getMainRegistration().addPropertyConditional(
                CondExprIsSet.class,
                "~objects",
                ConditionalType.BE,
                "set"
        );
    }

    @Override
    public boolean check(Object performer) {
        return true;
    }
}
