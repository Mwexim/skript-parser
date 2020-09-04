package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import org.jetbrains.annotations.Nullable;

/**
 * Chains multiple effects so they can be executed on the same line.
 *
 * @name Compose
 * @type EFFECT
 * @pattern [do] <.+>[,] then [do] <.+>
 * @since ALPHA
 * @author Mwexim
 */
public class EffCompose extends Effect {

    static {
        Parser.getMainRegistration().addEffect(
            EffCompose.class,
            1,
            "[do] <.+>[,] then [do] <.+>"
        );
    }

    private Effect effect1, effect2;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        String expr1 = parseContext.getMatches().get(0).group();
        String expr2 = parseContext.getMatches().get(1).group();
        parseContext.getLogger().recurse();
        var eff1 = SyntaxParser.parseEffect(expr1, parseContext.getParserState(), parseContext.getLogger());
        var eff2 = SyntaxParser.parseEffect(expr2, parseContext.getParserState(), parseContext.getLogger());
        parseContext.getLogger().callback();
        if (eff1.isEmpty() || eff2.isEmpty())
            return false;
        effect1 = eff1.get();
        effect2 = eff2.get();
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        effect1.walk(ctx);
        effect2.walk(ctx);
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return effect1.toString(ctx, debug) + " then " + effect2.toString(ctx, debug);
    }
}
