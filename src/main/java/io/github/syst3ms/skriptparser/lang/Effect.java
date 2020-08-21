package io.github.syst3ms.skriptparser.lang;

/**
 * A line of code that is executed before moving on to the next one.
 *
 * @see Statement
 */
public abstract class Effect extends Statement {

    protected abstract void execute(TriggerContext ctx);

    @Override
    public boolean run(TriggerContext ctx) {
        try {
            execute(ctx);
        } catch (UnsupportedOperationException ignored) { }
        return true;
    }
}
