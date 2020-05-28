package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * A section that keeps executing its contents while a given condition is met.
 */
@SuppressWarnings("unchecked")
public class While extends CodeSection {
    @Nullable
    private Statement actualNext;
    private Expression<Boolean> condition;

    static {
        Main.getMainRegistration().addSection(
                While.class,
                "while %=boolean%"
        );
    }

    @Override
    public void loadSection(FileSection section, SkriptLogger logger) {
        super.loadSection(section, logger);
        super.setNext(this);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        condition = (Expression<Boolean>) expressions[0];
        return true;
    }

    @Override
    protected Statement walk(TriggerContext ctx) {
        Boolean cond = condition.getSingle(ctx);
        if (cond == null || !cond) {
            return actualNext;
        } else {
            return getFirst();
        }
    }

    @Override
    public Statement setNext(@Nullable Statement next) {
        this.actualNext = next;
        return this;
    }

    /**
     * @see Loop#getActualNext()
     */
    @Nullable
    public Statement getActualNext() {
        return actualNext;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "while " + condition.toString(ctx, debug);
    }
}
