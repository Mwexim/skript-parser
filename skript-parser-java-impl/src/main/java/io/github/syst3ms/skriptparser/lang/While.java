package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;

@SuppressWarnings("unchecked")
public class While extends CodeSection {
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

    @Override
    protected Effect walk(Event e) {
        Boolean cond = condition.getSingle(e);
        if (cond == true) { // could be null
            return getFirst();
        } else {
            return actualNext;
        }
    }

    @Override
    public Effect setNext(Effect next) {
        this.actualNext = next;
        return this;
    }

    public Effect getActualNext() {
        return actualNext;
    }

    @Override
    public void execute(Event e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "while " + condition.toString(e, debug);
    }
}
