package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

/**
 * A section that keeps executing its contents while a given condition is met.
 */
@SuppressWarnings("unchecked")
public class SecWhile extends CodeSection {

    static {
        Main.getMainRegistration().addSection(
                SecWhile.class,
                "while %=boolean%"
        );
    }

    @Nullable
    private Statement actualNext;
    private Expression<Boolean> condition;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        condition = (Expression<Boolean>) expressions[0];
        return true;
    }

    @Override
    public void loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        super.loadSection(section, parserState, logger);
        super.setNext(this);
    }

    @Override
    public Statement setNext(@Nullable Statement next) {
        this.actualNext = next;
        return this;
    }

    @Override
    public Statement walk(TriggerContext ctx) {
        Boolean cond = condition.getSingle(ctx);
        if (cond == null || !cond) {
            return actualNext;
        } else {
            return getFirst();
        }
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "while " + condition.toString(ctx, debug);
    }

    /**
     * @see SecLoop#getActualNext()
     */
    @Nullable
    public Statement getActualNext() {
        return actualNext;
    }
}
