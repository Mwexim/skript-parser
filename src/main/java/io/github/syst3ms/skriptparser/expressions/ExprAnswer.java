package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.lambda.SectionValue;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.sections.SecAsk;

/**
 * Get back the last entered answer in a {@link SecAsk} section.
 * This will return the last line of the console.
 *
 * @name Last Answer
 * @type EXPRESSION
 * @pattern [the] [last] (answer|response)
 * @since ALPHA
 * @author Mwexim
 */
public class ExprAnswer extends SectionValue<SecAsk, String> {

    static {
        Parser.getMainRegistration().addExpression(
                ExprAnswer.class,
                String.class,
                true,
                "[the] [last] (answer|response)"
        );
    }

    @Override
    public boolean preInitialize(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public String[] getSectionValues(SecAsk section, TriggerContext ctx) {
        return new String[]{section.getArguments()[0].toString()};
    }

    @Override
    public Class<? extends SecAsk> getSectionClass() {
        return SecAsk.class;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "the answer";
    }
}
