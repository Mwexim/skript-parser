package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.effects.EffReturn;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import io.github.syst3ms.skriptparser.sections.SecConditional;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.sections.SecWhile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Represents a section of runnable code.
 * @see SecConditional
 * @see SecLoop
 * @see SecWhile
 * @see ConditionalExpression
 */
public abstract class CodeSection extends Statement {
    protected List<Statement> items;
    @Nullable
    protected Statement first;
    @Nullable
    protected Statement last;

    /**
     * This methods determines the logic of what is being done to the elements inside of this section.
     * By default, this simply parses all items inside it, but this can be overridden.
     * In case an extending class just needs to do some additional operations on top of what the default implementation
     * already does, then call {@code super.loadSection(section)} before any such operations.
     * @param section the {@link FileSection} representing this {@linkplain CodeSection}
     * @param logger the logger
     * @return {@code true} if the items inside of the section were loaded properly, {@code false} if there was a
     *         problem
     */
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        parserState.setSyntaxRestrictions(getAllowedSyntaxes(), isRestrictingExpressions());
        parserState.addCurrentSection(this);
        setItems(ScriptLoader.loadItems(section, parserState, logger));
        parserState.removeCurrentSection();
        parserState.clearSyntaxRestrictions();
        return true;
    }

    @Override
    @Contract("_ -> fail")
    public boolean run(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract Optional<? extends Statement> walk(TriggerContext ctx);

    /**
     * Sets the items inside this lists, and also modifies other fields, reflected through the outputs of {@link #getFirst()},
     * {@link #getLast()} and {@link Statement#getParent()}.
     * @param items the items to set
     */
    public final void setItems(List<Statement> items) {
        this.items = items;
        for (var item : items) {
            item.setParent(this);
        }
        first = items.isEmpty() ? null : items.get(0);
        last = items.isEmpty() ? null : items.get(items.size() - 1);
    }

    /**
     * The items returned by this method are not representative of the execution of the code, meaning that all items
     * in the list may not be all executed. The list should rather be considered as a flat view of all the lines inside
     * the section. Prefer {@link Statement#runAll(Statement, TriggerContext)} to run the contents of this section
     * @return all items inside this section
     */
    public List<Statement> getItems() {
        return items;
    }

    /**
     * @return the first item of this section, or the item after the section if it's empty, or {@code null} if there is
     * no item after this section, in the latter case
     */
    public final Optional<? extends Statement> getFirst() {
        return Optional.ofNullable(first).or(this::getNext);
    }

    /**
     * @return the last item of this section, or the item after the section if it's empty, or {@code null} if there is
     * no item after this section, in the latter case
     */
    protected final Optional<? extends Statement> getLast() {
        return Optional.ofNullable(last).or(this::getNext);
    }

    /**
     * A list of the classes of every syntax that is allowed to be used inside of this CodeSection. The default behavior
     * is to return an empty list, which equates to no restrictions. If overridden, this allows the creation of specialized,
     * DSL-like sections in which only select {@linkplain Statement statements} and other {@linkplain CodeSection sections}
     * (and potentially, but not necessarily, expressions).
     * @return a list of the classes of each syntax allowed inside this CodeSection
     * @see #isRestrictingExpressions()
     */
    protected Set<Class<? extends SyntaxElement>> getAllowedSyntaxes() {
        return Collections.emptySet();
    }

    /**
     * Whether the syntax restrictions outlined in {@link #getAllowedSyntaxes()} should also apply to expressions.
     * This is usually undesirable, so it is false by default.
     *
     * This should return true <b>if and only if</b> {@link #getAllowedSyntaxes()} contains an {@linkplain Expression} class.
     * @return whether the use of expressions is also restricted by {@link #getAllowedSyntaxes()}. False by default.
     */
    protected boolean isRestrictingExpressions() {
        return false;
    }

    public boolean checkFinishing(Predicate<? super Statement> finishingTest,
                                  SkriptLogger logger,
                                  int currentSectionLine,
                                  boolean warnUnreachable,
                                  String errorMessage) {
        int originalLine = logger.getLine();
        logger.setLine(currentSectionLine);
        boolean finished = false;
        for (Statement statement : items) {
            /*
             * There is an error here if you have some random section (like Loop) not ending properly.
             * Obviously, we shouldn't care about that error, and quickly discard it.
             */
            logger.clearErrors();
            logger.nextLine();
            if (finished) {
                if (warnUnreachable) {
                    // Right now, the section has returned in every case, but yet there are still more items
                    logger.warn("This line is unreachable");
                }
                // Otherwise, we don't wanna do anything
            } else if (statement instanceof CodeSection) {
                finished = ((CodeSection) statement).checkFinishing(
                        finishingTest,
                        logger,
                        currentSectionLine + 1,
                        warnUnreachable,
                        errorMessage
                );
            } else {
                finished = finishingTest.test(statement);
            }
        }
        if (!finished) {
            logger.setLine(currentSectionLine);
            logger.error(errorMessage, ErrorType.SEMANTIC_ERROR);
        }
        logger.setLine(originalLine);
        return finished;
    }

    public boolean checkFinishing(Predicate<? super Statement> finishingTest,
                                  SkriptLogger logger,
                                  int currentSectionLine,
                                  boolean warnUnreachable) {
        return checkFinishing(
                finishingTest,
                logger,
                currentSectionLine,
                warnUnreachable,
                "The code inside of this section should end in a finishing statement, but it doesn't"
        );
    }

    public boolean checkReturns(SkriptLogger logger,
                                int currentSectionLine,
                                boolean warnUnreachable) {
        return checkFinishing(
                s -> s instanceof EffReturn,
                logger,
                currentSectionLine,
                warnUnreachable,
                "The code inside of this section should return a value, but it doesn't"
        );
    }
}
