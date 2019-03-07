package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
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
    public void loadSection(FileSection section) {
        super.loadSection(section);
        super.setNext(this);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        condition = (Expression<Boolean>) expressions[0];
        return true;
    }

    @SuppressWarnings("PointlessBooleanExpression")
    @Override
    protected Statement walk(TriggerContext e) {
        Boolean cond = condition.getSingle(e);
        if (cond == null) {
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
