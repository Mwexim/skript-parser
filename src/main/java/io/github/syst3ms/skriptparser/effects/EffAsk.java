package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.expressions.ExprAnswer;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.TaggedExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Scanner;

/**
 * Ask in the system console a specific message and wait for the answer of the user.
 * The code in the section will be executed once the user answer or enter any value.
 * The code after the section will run even if the user haven't answered yet.
 *
 * @name Ask
 * @type EFFECT
 * @pattern ask for %string%
 * @since ALPHA
 * @author ItsTheSky
 */
public class EffAsk extends Effect {

    static {
        Parser.getMainRegistration().addEffect(
                EffAsk.class,
                "ask for %string%"
        );
    }

    private Expression<String> message;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        message = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    protected void execute(TriggerContext ctx) {
        Scanner scanner = new Scanner(System.in);
        for (String line : TaggedExpression.apply(message, ctx, "console"))
            System.out.println(line);
        ExprAnswer.addAnswer(ctx, scanner.nextLine());
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "ask for " + message.toString(ctx, debug);
    }
}
