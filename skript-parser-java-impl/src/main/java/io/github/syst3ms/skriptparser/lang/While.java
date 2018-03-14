package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class While extends CodeSection {
    @Nullable
    private Effect actualNext;
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
    protected Effect walk(Event e) {
        Boolean cond = condition.getSingle(e);
        if (cond == null) {
            return actualNext;
        } else {
            return getFirst();
        }
    }

    @Override
    public Effect setNext(@Nullable Effect next) {
        this.actualNext = next;
        return this;
    }

    @Nullable
    public Effect getActualNext() {
        return actualNext;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "while " + condition.toString(e, debug);
    }
}
