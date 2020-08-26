package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import org.jetbrains.annotations.Nullable;

/**
 * Runs this effect if a given condition succeeds.
 *
 * @name Do If
 * @type EFFECT
 * @pattern if %=boolean%[,] [do] <.+>
 * @since ALPHA
 * @author Mwexim
 */
public class EffDoIf extends Effect {

    static {
        Main.getMainRegistration().addEffect(
            EffDoIf.class,
            1,
            "if %=boolean%[,] [do] <.+>"
        );
    }

    ConditionalExpression condition;
    private Effect effect;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        condition = (ConditionalExpression) expressions[0];
        String expr = parseContext.getMatches().get(0).group();
        parseContext.getLogger().recurse();
        var eff = SyntaxParser.parseEffect(expr, parseContext.getParserState(), parseContext.getLogger());
        parseContext.getLogger().callback();
        if (eff.isEmpty())
            return false;
        effect = eff.get();
        if (effect instanceof EffDoIf) {
            parseContext.getLogger().error("You can't nest multiple do-if effects!", ErrorType.SEMANTIC_ERROR);
        }
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        if (condition.check(ctx))
            effect.walk(ctx);
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "if " + condition.toString(ctx, debug) + ", " + effect.toString(ctx, debug);
    }
}
