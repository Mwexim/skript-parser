package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * This section is written underneath the {@link SecSwitch switch} section to match the given expression.
 * The content of this section will only be executed if it matches the given expression.
 * One may use 'or'-lists to match multiple expressions at once.
 * The default part can be used to provide actions when no match was found.
 * Note that you can only use one default statement.
 *
 * @name Case
 * @type SECTION
 * @pattern (case|when) %*objects%
 * @pattern ([by] default|otherwise)
 * @since ALPHA
 * @author Mwexim
 * @see SecSwitch
 */
@SuppressWarnings("unchecked")
public class SecCase extends CodeSection {
    static {
        Parser.getMainRegistration().addSection(
                SecCase.class,
                "(case|when) %*objects%",
                "([by] default|otherwise)"
        );
    }

    private Expression<Object> matchWith;
    private SecSwitch switchSection;
    private boolean isMatching;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        var logger = parseContext.getLogger();
        var latest = parseContext.getParserState().getCurrentSections().get(0);
        if (!(latest instanceof SecSwitch)) {
            logger.error("You can only use a 'case'-statement inside a 'switch'-section!", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        switchSection = (SecSwitch) latest;

        isMatching = matchedPattern == 0;
        if (isMatching) {
            matchWith = (Expression<Object>) expressions[0];
            if (!matchWith.isSingle() && matchWith.isAndList()) {
                logger.error(
                        "Only 'or'-lists may be used, found '" + matchWith.toString(TriggerContext.DUMMY, logger.isDebug()),
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
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        if (isMatching) {
            return switchSection.getMatch().getSingle(ctx)
                    .filter(toMatch -> matchWith.check(
                            ctx,
                            with -> Comparators.compare(toMatch, with).is(Relation.EQUAL)
                    ))
                    .flatMap(__ -> {
                        switchSection.setDone(true);
                        return getFirst();
                    })
                    .map(val -> (Statement) val)
                    .or(() -> Optional.of(switchSection));
        } else {
            return getFirst();
        }
    }

    @Override
    public Statement setNext(@Nullable Statement next) {
        this.next = switchSection;
        return this;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return isMatching ? ("case " + matchWith.toString(ctx, debug)) : "default";
    }
}
