package io.github.syst3ms.skriptparser.lang.properties;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SelfRegistrable;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.Nullable;

/**
 * This class can be used for an easier writing of conditions that contain only one type in the pattern
 * and are in one of the following forms:
 * <ul>
 *     <li>{@code something is something}</li>
 *     <li>{@code something can something}</li>
 *     <li>{@code something has something}</li>
 * </ul>
 * The plural and negated forms are also supported.
 *
 * The gains of using this class:
 * <ul>
 *     <li>The {@link SyntaxElement#toString(TriggerContext, boolean) toString(TriggerContext, boolean)}
 *     method is already implemented and it works well with the plural and negated forms</li>
 *     <li>It implements {@link SelfRegistrable}, which means an easy registration is possible</li>
 * </ul>
 * </br>
 * <i>Description partly copied from original Skript project.</i>
 * @param <P> the type of the performer in this condition
 * @author Mwexim
 */
public abstract class PropertyConditional<P> extends ConditionalExpression implements SelfRegistrable {
    private Expression<P> performer;
    private String performerName;
    private ConditionalType conditionalType;
    private String propertyName;
    private String propertyRepresentation;

    public Expression<P> getPerformer() {
        return performer;
    }

    public void setPerformer(Expression<P> performer) {
        this.performer = performer;
    }

    /**
     * This default {@code init()} implementation automatically properly sets the performer in this condition,
     * which can be accessed using {@link #getPerformer()}. If this implementation is overridden for one reason
     * or another, it must call {@link #setPerformer(Expression)} properly.
     * @param expressions an array of expressions representing all the expressions that are being passed
     *                    to this syntax element.
     * @param matchedPattern the index of the pattern that was successfully matched. It corresponds to the order of
     *                       the syntaxes in registration
     * @param parseContext an object containing additional information about the parsing of this syntax element, like
     *                    regex matches and parse marks
     * @return whether the initialization was successful or not.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        setPerformer((Expression<P>) expressions[0]);
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return check(ctx, performer.getValues(ctx));
    }

    public abstract boolean check(TriggerContext ctx, P[] performers);

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return toString(ctx, debug, performer, conditionalType,
                propertyRepresentation != null ? propertyRepresentation : propertyName);
    }

    private String toString(@Nullable TriggerContext ctx, boolean debug,
                            Expression<P> perf,
                            ConditionalType conditionalType,
                            String property) {
        switch (conditionalType) {
            case BE:
                return perf.toString(ctx, debug) + (perf.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") + property;
            case CAN:
                return perf.toString(ctx, debug) + (isNegated() ? " can't " : " can ") + property;
            case HAVE:
                if (perf.isSingle())
                    return perf.toString(ctx, debug) + (isNegated() ? " doesn't have " : " has ") + property;
                else
                    return perf.toString(ctx, debug) + (isNegated() ? " don't have " : " have ") + property;
            default:
                throw new AssertionError();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(SkriptRegistration reg, Object... args) {
        if (args.length < 3 || 4 < args.length)
            throw new IllegalArgumentException("A PropertyCondition needs exactly 3 or 4 arguments");
        performerName = (String) args[0];
        conditionalType = (ConditionalType) args[1];
        propertyName = (String) args[2];
        if (args.length == 4)
            propertyRepresentation = (String) args[3];

        reg.addExpression(getClass(),
                Boolean.class,
                true,
                composePatterns(performerName, conditionalType, propertyName)
        );
    }

    private String[] composePatterns(String performer, ConditionalType conditionalType, String property) {
        var type = performer.startsWith("*") ? performer.substring(1) : "%" + performer + "%";
        switch (conditionalType) {
            case BE:
                return new String[] {
                        type + " (is|are) " + property,
                        type + " (is|are)( not|n't) " + property,
                };
            case CAN:
                return new String[] {
                        type + " can " + property,
                        type + " can([ ]not|'t) " + property,
                };
            case HAVE:
                return new String[] {
                        type + " (has|have) " + property,
                        type + " does( not|n't) have " + property,
                };
            default:
                throw new AssertionError();
        }
    }
}
