package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.FileUtils;

/**
 * The name of the current executed script, without the extension.
 *
 * @name Script Name
 * @pattern [the] script[['s] name]
 * @pattern name of [the] script
 * @since ALPHA
 * @author Mwexim
 */
public class LitScriptName implements Literal<String> {
    static {
        Parser.getMainRegistration().addExpression(
                LitScriptName.class,
                String.class,
                true,
                "[the] script[['s] name]",
                "name of [the] script"
        );
    }

    private SkriptLogger logger;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        logger = parseContext.getLogger();
        return true;
    }

    @Override
    public String[] getValues() {
        return new String[] {FileUtils.removeExtension(logger.getFileName())};
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "script name";
    }
}
