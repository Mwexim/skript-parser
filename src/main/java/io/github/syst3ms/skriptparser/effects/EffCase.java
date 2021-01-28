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
 * Note that you can only use one default statement and that equivalent matches are prohibited.
 *
 * @name Case
 * @type EFFECT
 * @pattern (case|when) %*objects%[,] [do] <.+>
 * @pattern ([by] default|otherwise)[,] [do] <.+>
 * @since ALPHA
 * @author Mwexim
 * @see SecSwitch
 */
public class EffCase extends Effect {
    // TODO try to omit the 'then' in the first pattern?
    static {
        Parser.getMainRegistration().addEffect(
                EffCase.class,
                "(case|when) %*objects%[,] then <.+>",
                "([by] default|otherwise)[,] [do] <.+>"
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
            logger.error("You can only use 'case'-statement inside a 'switch'-section!", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        switchSection = (SecSwitch) latest;

        isMatching = matchedPattern == 0 || matchedPattern == 1;
        if (isMatching) {
            matchWith = (Expression<Object>) expressions[0];
            if (!matchWith.isSingle() && matchWith.isAndList()) {
                logger.error(
                        "Only 'or'-lists may be used, found '" + matchWith.toString(null, logger.isDebug()),
                        ErrorType.SEMANTIC_ERROR
                );
                return false;
            } else if (switchSection.getDefault().isPresent()) {
                logger.error(
                        "A 'case'-section cannot be placed behind a 'default'-statement.",
                        ErrorType.SEMANTIC_ERROR,
                        "Place this statement before the 'default'-statement to provide the same behavior."
                );
                return false;
            }
            switchSection.getCases().add(this);
        } else if (switchSection.getDefault().isPresent()) {
            logger.error(
                    "Only one 'default'-statement may be used inside a 'switch'-section",
                    ErrorType.SEMANTIC_ERROR,
                    "Merge this section with the other 'default'-section to provide the same behavior."
            );
            return false;
        } else {
            switchSection.setDefault(this);
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
                        switchSection.setDone(true);
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
}
