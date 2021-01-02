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
 *
 * @name Case
 * @type SECTION
 * @pattern (case|matche(s|d)) %*objects%
 * @pattern (default|otherwise|no match[es])
 * @since ALPHA
 * @author Mwexim
 * @see SecSwitch
 */
@SuppressWarnings("unchecked")
public class SecCase extends CodeSection {
    static {
        Parser.getMainRegistration().addSection(
                SecCase.class,
                "(case|matche(s|d)) %*objects%",
                "(default|otherwise|no match[es])"
        );
    }

    private Expression<Object> matchWith;
    private SecSwitch switchSection;
    private boolean caseNode;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        var logger = parseContext.getLogger();
        var latest = parseContext.getParserState().getCurrentSections().get(0);
        if (!(latest instanceof SecSwitch)) {
            logger.error("You can only use 'case' in a switch!", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        switchSection = (SecSwitch) latest;

        caseNode = matchedPattern == 0;
        if (caseNode) {
            matchWith = (Expression<Object>) expressions[0];
            if (!matchWith.isSingle() && matchWith.isAndList()) {
                logger.error(
                        "Only 'or'-lists may be used, found '" + matchWith.toString(null, logger.isDebug()),
                        ErrorType.SEMANTIC_ERROR
                );
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        if (caseNode) {
            switchSection.getMatch().getSingle(ctx)
                    .filter(val -> Expression.check(
                            matchWith.getValues(ctx),
                            val2 -> Comparators.compare(val, val2).is(Relation.EQUAL),
                            false,
                            false
                    ))
                    .ifPresent(__ -> {
                        switchSection.setMatched(true);
                        var item = getFirst();
                        while (!item.equals(getNext())) // Calling equals() on optionals calls equals() on their values
                            item = item.flatMap(i -> i.walk(ctx));
                    });
        } else {
            var item = getFirst();
            while (!item.equals(getNext())) // Calling equals() on optionals calls equals() on their values
                item = item.flatMap(i -> i.walk(ctx));
        }
        return getNext(); // We need to return the next 'case' section.
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return caseNode ? ("case " + matchWith.toString(ctx, debug)) : "default";
    }

    public boolean isCaseNode() {
        return caseNode;
    }
}
