package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * This is the general control flow section which evaluates conditions and executes code
 * if they succeed. If the condition fails, the section will look for falling clauses to
 * run, like other 'else if' and 'else' sections. If there is no such clause, it will
 * execute the next statement instead.
 *
 * @name Conditional
 * @type SECTION
 * @pattern if %=boolean%
 * @pattern else if %=boolean%
 * @pattern else
 * @since ALPHA
 * @author Mwexim, Syst3ms
 */
public class SecConditional extends CodeSection {
    static {
        Parser.getMainRegistration().addSection(
                SecConditional.class,
                6,
                "if %=boolean%",
                "else if %=boolean%",
                "else"
        );
    }

    private ConditionalMode mode;
    @Nullable
    private Expression<Boolean> condition;
    @Nullable
    private SecConditional fallingClause;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        mode = ConditionalMode.values()[matchedPattern];
        if (mode != ConditionalMode.ELSE)
            condition = (Expression<Boolean>) expressions[0];

        if (mode == ConditionalMode.IF)
            return true;
        var items = parseContext.getParserState().getCurrentStatements();
        var last = items.size() > 0 ? items.get(items.size() - 1) : null;
        if (last instanceof SecConditional && ((SecConditional) last).mode != ConditionalMode.ELSE) {
            ((SecConditional) last).setFallingClause(this);
            return true;
        }

        parseContext.getLogger().error(
                "An '" + mode + "' must be placed after an 'if' or an 'else if'",
                ErrorType.SEMANTIC_ERROR
        );
        return false;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        assert condition != null || mode == ConditionalMode.ELSE;
        if (mode == ConditionalMode.ELSE) {
            return getFirst();
        }
        if (condition.getSingle(ctx).filter(Boolean::booleanValue).isPresent()) {
            return getFirst();
        } else if (fallingClause != null) {
            return Optional.of(fallingClause);
        } else {
            return getNext();
        }
    }

    @Override
    public Statement setNext(@Nullable Statement next) {
        while (next instanceof SecConditional && ((SecConditional) next).mode != ConditionalMode.IF) {
            next = ((SecConditional) next).next;
        }
        return super.setNext(next);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return mode + (condition != null ? " " + condition.toString(ctx, debug) : "");
    }

    @Override
    public boolean checkFinishing(Predicate<? super Statement> finishingTest,
                                  SkriptLogger logger,
                                  int currentSectionLine,
                                  boolean warnUnreachable) {
        if (mode == ConditionalMode.ELSE) {
            return super.checkFinishing(finishingTest, logger, currentSectionLine, warnUnreachable);
        } else if (fallingClause != null) {
            return super.checkFinishing(finishingTest, logger, currentSectionLine, warnUnreachable)
                    && fallingClause.checkFinishing(
                            finishingTest,
                            logger,
                            currentSectionLine + items.size() + 1,
                            warnUnreachable
                    );
        } else {
            return false;
        }
    }

    /**
     * Sets the Conditional object this Conditional falls back to when it's condition verifies to
     * false. Setting this to an "if" Conditional may cause unexpected/confusing behaviour.
     * @param conditional the conditional section
     */
    public void setFallingClause(SecConditional conditional) {
        fallingClause = conditional;
        //fallingClause.setParent(this);
    }

    /**
     * @return the {@link ConditionalMode} describing this Conditional
     * @see ConditionalMode
     */
    public ConditionalMode getMode() {
        return mode;
    }

    public enum ConditionalMode {
        IF, ELSE_IF, ELSE;

        private final String[] modeNames = {"if", "else if", "else"};

        @Override
        public String toString() {
            return modeNames[this.ordinal()];
        }
    }
}
