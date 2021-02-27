package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;

/**
 * Runs this effect if a given condition succeeds.
 *
 * @name Do If
 * @type EFFECT
 * @pattern [do|execute] <.+?>[,] [only] if %=boolean%
 * @since ALPHA
 * @author Mwexim
 */
public class EffDoIf extends Effect {
    static {
        Parser.getMainRegistration().addEffect(
            EffDoIf.class,
            1,
            "[do|execute] <.+?>[,] [only] if %=boolean%"
        );
    }

    private Expression<Boolean> condition;
    private Effect effect;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        condition = (Expression<Boolean>) expressions[0];
        String expr = parseContext.getMatches().get(0).group();
        parseContext.getLogger().recurse();
        var eff = SyntaxParser.parseEffect(expr, parseContext.getParserState(), parseContext.getLogger());
        parseContext.getLogger().callback();
        if (eff.isEmpty())
            return false;
        effect = eff.get();
        if (effect instanceof EffDoIf) {
            parseContext.getLogger().error("You can't nest multiple do if-effects!", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void execute(TriggerContext ctx) {
        if (condition.getSingle(ctx).filter(b -> b).isPresent())
            effect.walk(ctx);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "if " + condition.toString(ctx, debug) + ", " + effect.toString(ctx, debug);
    }
}
