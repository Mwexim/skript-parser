package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.util.ThreadUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Runs this effect asynchronously.
 * The difference between this and the section {@code async} is due to the fact that this effect will only run the specified action asunc,
 * while the section runs all the effects under the statement async.
 * If you only want to do one operation async, this is the effect you want to go with.
 *
 * @name Async
 * @type EFFECT
 * @pattern async[hronous[ly]] <.+>
 * @since ALPHA
 * @author Mwexim
 */
public class EffAsync extends Effect {

    static {
        Main.getMainRegistration().addEffect(
            EffAsync.class,
            "async[hronous[ly]] [do] <.+>"
        );
    }

    private Effect effect;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        String expr = parseContext.getMatches().get(0).group();
        parseContext.getLogger().recurse();
        effect = SyntaxParser.parseEffect(expr, parseContext.getParserState(), parseContext.getLogger()).orElse(null);
        parseContext.getLogger().callback();
        return effect != null;
    }

    @Override
    public void execute(TriggerContext ctx) {
        ThreadUtils.runAsync(() -> effect.run(ctx));
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "async " + effect.toString(ctx, debug);
    }
}
