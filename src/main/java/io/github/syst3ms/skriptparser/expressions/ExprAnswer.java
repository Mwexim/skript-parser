package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.effects.EffAsk;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Retrieve the last input of the user in a {@link EffAsk} effect.
 * This will essentially return the last line of the console.
 *
 * @name Last Answer
 * @type EXPRESSION
 * @pattern [the] [last] (answer|response)
 * @since ALPHA
 * @author ItsTheSky
 */
public class ExprAnswer implements Expression<String> {

    static {
        Parser.getMainRegistration().addExpression(
                ExprAnswer.class,
                String.class,
                true,
                "[the] [last] (answer|response)"
        );
    }

    private static final Map<TriggerContext, String> answers = new HashMap<>();

    public static void addAnswer(TriggerContext ctx, String answer) {
        answers.put(ctx, answer);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext context) {
        return true;
    }

    @Override
    public String[] getValues(TriggerContext ctx) {
        return answers.containsKey(ctx)
                ? new String[] {answers.get(ctx)}
                : new String[0];
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "last answer";
    }
}
