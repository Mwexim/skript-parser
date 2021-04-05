package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.control.Continuable;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Skips the current looped value and continues to the next one in the list, if it exists.
 *
 * @name Continue
 * @pattern continue
 * @since ALPHA
 * @author Mwexim
 */
public class EffContinue extends Effect {
    static {
        Parser.getMainRegistration().addEffect(
            EffContinue.class,
            4,
            "continue"
        );
    }

    private List<? extends Continuable> sections;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        sections = parseContext.getParserState().getCurrentSections().stream()
                .filter(sec -> sec instanceof Continuable)
                .map(sec -> (Continuable) sec)
                .collect(Collectors.toList());
        if (sections.size() == 0) {
            parseContext.getLogger().error("You cannot use the 'continue'-effect here", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
	public Optional<? extends Statement> walk(TriggerContext ctx) {
		return sections.get(0).getContinued(ctx);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "continue";
    }
}