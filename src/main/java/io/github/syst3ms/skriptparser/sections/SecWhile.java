package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.control.Continuable;
import io.github.syst3ms.skriptparser.lang.control.SelfReferencing;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * This section will keep executing the statements inside until the given condition
 * does not hold anymore.
 *
 * @name While
 * @type SECTION
 * @pattern while %=boolean%
 * @since ALPHA
 * @author Mwexim
 */
public class SecWhile extends CodeSection implements Continuable, SelfReferencing {
    static {
        Parser.getMainRegistration().addSection(
                SecWhile.class,
                "while %=boolean%"
        );
    }

    @Nullable
    private Statement actualNext;
    private Expression<Boolean> condition;

    @Override
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        super.setNext(this);
        return super.loadSection(section, parserState, logger);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        condition = (Expression<Boolean>) expressions[0];
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Optional<? extends Boolean> cond = condition.getSingle(ctx);
        if (cond.isEmpty() || !cond.get().booleanValue()) {
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

    @Override
    public Optional<? extends Statement> getContinued(TriggerContext ctx) {
        return Optional.of(this);
    }

    @Override
    public Optional<Statement> getActualNext() {
        return Optional.ofNullable(actualNext);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "while " + condition.toString(ctx, debug);
    }
}
