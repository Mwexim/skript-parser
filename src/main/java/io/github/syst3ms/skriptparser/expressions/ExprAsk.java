package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.TaggedExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Scanner;

/**
 * Ask for a specific input in the system console and wait for the answer of the user.
 * This will therefore return what the user entered into a string.
 *
 * @name Ask
 * @type EXPRESSION
 * @pattern ask [for] %string%
 * @since ALPHA
 * @author ItsTheSky
 */
public class ExprAsk implements Expression<String> {

    static {
        Parser.getMainRegistration().addExpression(
                ExprAsk.class,
                String.class,
                true,
                "ask [for] %string%"
        );
    }

    private Expression<String> message;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext context) {
        message = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public String[] getValues(TriggerContext ctx) {
        Scanner scanner = new Scanner(System.in);
        for (String line : TaggedExpression.apply(message, ctx, "console"))
            System.out.println(line);
        return new String[]{scanner.nextLine()};
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "ask for " + message.toString(ctx, debug);
    }
}
