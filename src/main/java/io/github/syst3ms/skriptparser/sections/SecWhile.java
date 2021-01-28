package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A section that keeps executing its contents while a given condition is met.
 */
@SuppressWarnings("unchecked")
public class SecWhile extends CodeSection {
    @Nullable
    private Statement actualNext;
    private Expression<Boolean> condition;

    static {
        Parser.getMainRegistration().addSection(
                SecWhile.class,
                "while %=boolean%"
        );
    }

    @Override
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        super.loadSection(section, parserState, logger);
        super.setNext(this);
        return true;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        condition = (Expression<Boolean>) expressions[0];
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Optional<? extends Boolean> cond = condition.getSingle(ctx);
        if (cond.isEmpty() || !cond.get()) {
            return Optional.ofNullable(actualNext);
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
     * This method exists because SecWhile actually sets itself as its next element with {@link #getNext()}.
     * This way it has full control over when to stop iterating.
     * @return the element that is actually after this SecWhile
     */
    @Nullable
    public Optional<Statement> getActualNext() {
        return Optional.ofNullable(actualNext);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "while " + condition.toString(ctx, debug);
    }
}
