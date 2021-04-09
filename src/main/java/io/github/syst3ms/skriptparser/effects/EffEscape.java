package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.control.Finishing;
import io.github.syst3ms.skriptparser.lang.control.SelfReferencing;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Skips the next given amount of lines and runs the code after.
 * Note that lines that have more indentation than this one are not considered in the amount.
 * Nevertheless, lines that have less indentation will be considered.
 *
 * @name Escape
 * @pattern escape %integer% [(level[s]|line[s])]
 * @since ALPHA
 * @author Mwexim
 */
public class EffEscape extends Effect {
    static {
        Parser.getMainRegistration().addEffect(
            EffEscape.class,
            "escape %integer% [(level|line)[s]]"
        );
    }

    private Expression<BigInteger> amount;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        amount = (Expression<BigInteger>) expressions[0];
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        if (amount.getSingle(ctx).isEmpty())
            return getNext();
        int am = amount.getSingle(ctx).get().intValue();

        Optional<Statement> current = Optional.of(this);
        while (am > 0) {
            if (current.isEmpty()) {
                return Optional.empty();
            } else if (current.get() instanceof Finishing) {
                ((Finishing) current.get()).finish();
            }
            current = current.flatMap(val -> val instanceof SelfReferencing
                    ? ((SelfReferencing) val).getActualNext()
                    : val.getNext());

            // Because otherwise SelfReferencing sections will first reference to themselves
            if (current.filter(val -> val instanceof Finishing).isEmpty())
                am--;
        }
        return current.flatMap(val -> val instanceof SelfReferencing
                ? ((SelfReferencing) val).getActualNext()
                : val.getNext());
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "escape " + amount.toString(ctx, debug) + " lines";
    }
}
