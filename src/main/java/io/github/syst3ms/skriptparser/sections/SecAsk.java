package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.TaggedExpression;
import io.github.syst3ms.skriptparser.lang.lambda.ArgumentSection;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Optional;
import java.util.Scanner;

/**
 * Ask in the system console a specific message and wait for the answer of the user.
 * The code in the section will be executed once the user answer or enter any value.
 * The code after the section will run even if the user haven't answered yet.
 *
 * @name Ask
 * @type SECTION
 * @pattern ask for %string%
 * @since ALPHA
 * @author Mwexim
 */
@SuppressWarnings("unchecked")
public class SecAsk extends ArgumentSection {

    static {
        Parser.getMainRegistration().addSection(
                SecAsk.class,
                "ask for %string%"
        );
    }

    private Expression<String> message;

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        if (getNext().isPresent())
            getNext().get().run(ctx);
        Scanner scanner = new Scanner(System.in);
        System.out.println(TaggedExpression.apply(message, ctx, "console")[0]);
        setArguments(scanner.nextLine());
        setNext(null);
        return getFirst();
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        message = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "ask for " + message.toString(ctx, debug);
    }
}
