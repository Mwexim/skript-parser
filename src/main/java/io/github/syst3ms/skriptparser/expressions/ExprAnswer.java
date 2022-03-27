package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.effects.EffAsk;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Get back the last entered answer in a {@link EffAsk} effect.
 * This will return the last line of the console.
 *
 * @name Last Answer
 * @type EXPRESSION
 * @pattern [the] [last] (answer|response)
 * @since ALPHA
 * @author Mwexim
 */
public class ExprAnswer implements Expression<String> {

    static {
        Parser.getMainRegistration().addExpression(
                ExprAnswer.class,
                String.class,
                true,
                "[the] [last] (answer|response)"
        );
        ANSWERS = new HashMap<>();
    }

    private static final Map<TriggerContext, String> ANSWERS;

    public static void addAnswer(TriggerContext ctx, String answer) {
        ANSWERS.put(ctx, answer);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext context) {
        return true;
    }

    @Override
    public String[] getValues(TriggerContext ctx) {
        if (ANSWERS.containsKey(ctx)) {
            var answer = ANSWERS.get(ctx);
            ANSWERS.remove(ctx);
            return new String[] {answer};
        } else {
            return new String[0];
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "the answer";
    }
}
