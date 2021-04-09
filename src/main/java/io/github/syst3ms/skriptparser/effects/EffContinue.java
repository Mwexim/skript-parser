package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.control.Continuable;
import io.github.syst3ms.skriptparser.lang.control.Finishing;
import io.github.syst3ms.skriptparser.lang.lambda.ArgumentSection;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Skips the current looped value and continues to the next one in the list, if it exists.
 *
 * @name Continue
 * @pattern continue [%*integer% loop[s]]
 * @since ALPHA
 * @author Mwexim
 */
public class EffContinue extends Effect {
    static {
        Parser.getMainRegistration().addEffect(
            EffContinue.class,
            4,
            "continue [%*integer% loop[s]]"
        );
    }

    private Expression<BigInteger> position;
    private List<? extends Continuable> sections;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        if (expressions.length == 1)
            position = (Expression<BigInteger>) expressions[0];

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
        // Indices start at 1
        int pos = position != null ? position.getSingle(ctx)
                .filter(val -> val.compareTo(BigInteger.ZERO) > 0 && val.compareTo(BigInteger.valueOf(sections.size())) <= 0)
                .map(val -> val.intValue() - 1)
                .orElse(-1) : 0;
        if (pos == -1)
            return Optional.empty();

        sections.subList(0, pos).forEach(sec -> {
            if (sec instanceof Finishing)
                ((Finishing) sec).finish();
        });

        if (sections.get(pos) instanceof ArgumentSection) {
            ((ArgumentSection) sections.get(0)).step(this);
        }
		return sections.get(pos).getContinued(ctx);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "continue";
    }
}