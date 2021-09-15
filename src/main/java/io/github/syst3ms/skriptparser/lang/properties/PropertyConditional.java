package io.github.syst3ms.skriptparser.lang.properties;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;

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
 *     <li>There is a useful {@link #toString(TriggerContext, boolean, String) toString()}
 *     method and it works well with the plural and negated forms.
 *     It is implemented by default.</li>
 *     <li>Registration is very straightforward.</li>
 *     <li>The performer expression is automatically checked for nullity.</li>
 * </ul>
 * @param <P> the type of the performer in this condition
 * @author Mwexim
 */
public abstract class PropertyConditional<P> extends ConditionalExpression {
    public static final String PROPERTY_IDENTIFIER = "property";
    public static final String CONDITIONAL_TYPE_IDENTIFIER = "conditionalType";

    private Expression<P> performer;

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
        return getPerformer().check(ctx, this::check, isNegated());
    }

    /**
     * Tests this condition for each individual performer. Negated conditions are taken care of
     * automatically, so one must not account for it in here.
     * @param performer the performer
     * @return whether the conditions is true for this performer
     */
    public boolean check(P performer) {
        throw new UnsupportedOperationException("Override #check(P) if you are planning to use the default functionality.");
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        var property = SyntaxManager.getExpressionExact(this)
                .orElseThrow(() -> new SkriptParserException("Unregistered property class: " + getClass().getName()))
                .getData(PROPERTY_IDENTIFIER, String.class);
        return toString(ctx, debug, property);
    }

    protected String toString(TriggerContext ctx, boolean debug, String property) {
        var performer = getPerformer();
        var conditionalType = SyntaxManager.getExpressionExact(this)
                .orElseThrow(() -> new SkriptParserException("Unregistered property class: " + getClass().getName()))
                .getData(CONDITIONAL_TYPE_IDENTIFIER, ConditionalType.class);
        switch (conditionalType) {
            case BE:
                return performer.toString(ctx, debug) + (performer.isSingle() ? " is " : " are ") + (isNegated() ? "not " : "") + property;
            case CAN:
                return performer.toString(ctx, debug) + (isNegated() ? " can't " : " can ") + property;
            case HAVE:
                if (performer.isSingle()) {
                    return performer.toString(ctx, debug) + (isNegated() ? " doesn't have " : " has ") + property;
                } else {
                    return performer.toString(ctx, debug) + (isNegated() ? " don't have " : " have ") + property;
                }
            default:
                throw new IllegalStateException();
        }
    }

    public Expression<P> getPerformer() {
        return performer;
    }

    public void setPerformer(Expression<P> performer) {
        this.performer = performer;
    }

    public static String[] composePatterns(String performer, ConditionalType conditionalType, String property) {
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
