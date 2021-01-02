package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.sections.SecSwitch;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * This effect is written underneath the {@link SecSwitch switch} section to match the given expression.
 * The statement inside this effect will only be executed if it matches the given expression.
 * One may use 'or'-lists to match multiple expressions at once.
 * The default part can be used to provide actions when no match was found.
 *
 * @name Case
 * @type EFFECT
 * @pattern (case|matche(s|d)) %*objects%[,] (then [do]|do) <.+>
 * @pattern (default|otherwise|no match[es])[,] [(then [do]|do)] <.+>
 * @since ALPHA
 * @author Mwexim
 * @see SecSwitch
 */
public class EffCase extends Effect implements SecSwitch.MatchingElement {
    static {
        Parser.getMainRegistration().addEffect(
                EffCase.class,
                "(case|matche(s|d)) %*objects%[,] (then [do]|do) <.+>",
                "(default|otherwise|no match[es])[,] [(then [do]|do)] <.+>"
        );
    }

    private Expression<Object> matchWith;
    private Effect effect;
    private SecSwitch switchSection;
    private boolean isMatching;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        var logger = parseContext.getLogger();
        var latest = parseContext.getParserState().getCurrentSections().get(0);
        if (!(latest instanceof SecSwitch)) {
            logger.error("You can only use 'case' in a switch!", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        switchSection = (SecSwitch) latest;

        isMatching = matchedPattern == 0;
        if (isMatching) {
            matchWith = (Expression<Object>) expressions[0];
            if (!matchWith.isSingle() && matchWith.isAndList()) {
                logger.error(
                        "Only 'or'-lists may be used, found '" + matchWith.toString(null, logger.isDebug()),
                        ErrorType.SEMANTIC_ERROR
                );
                return false;
            }
        }

        String expr = parseContext.getMatches().get(0).group();
        parseContext.getLogger().recurse();
        effect = SyntaxParser.parseEffect(expr, parseContext.getParserState(), parseContext.getLogger()).orElse(null);
        parseContext.getLogger().callback();
        return effect != null;
    }

    @Override
    protected void execute(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        if (isMatching) {
            switchSection.getMatch().getSingle(ctx)
                    .filter(val -> Expression.check(
                            matchWith.getValues(ctx),
                            val2 -> Comparators.compare(val, val2).is(Relation.EQUAL),
                            false,
                            false
                    ))
                    .ifPresent(__ -> {
                        switchSection.setMatched(true);
                        effect.walk(ctx);
                    });
        } else {
            effect.walk(ctx);
        }
        return Optional.empty();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return isMatching ? ("case " + matchWith.toString(ctx, debug)) : "default";
    }

    @Override
    public boolean isMatching() {
        return isMatching;
    }
}
