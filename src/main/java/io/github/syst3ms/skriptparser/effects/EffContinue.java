package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Skips the current looped value and continues to the next one in the list, if it exists.
 *
 * @name Continue
 * @pattern continue
 * @since ALPHA
 * @author Mwexim
 */
public class EffContinue extends Effect {

    static {
        Main.getMainRegistration().addEffect(
            EffContinue.class,
            "continue"
        );
    }

    private SecLoop loop;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        List<SecLoop> loops = new ArrayList<>();
        for (CodeSection sec : parseContext.getParserState().getCurrentSections()) {
            if (sec instanceof SecLoop) {
                loops.add((SecLoop) sec);
            }
        }
        loop = loops.get(loops.size() - 1);

        if (loop == null) {
            parseContext.getLogger().error("You can only continue in a loop!", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement walk(TriggerContext ctx) {
        return loop.walk(ctx);
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "continue";
    }
}
