package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The entry point for all code in Skript. Once an event triggers, all of the code inside it may be run.
 *
 * Skript-parser's event system is composed of three interacting parts : {@link Trigger}, {@link SkriptEvent} and {@link TriggerContext}.
 * This is directly parallel to Skript's event system, with Bukkit's own Event class replacing TriggerContext.
 *
 * Let's explain how this system works using a simple analogy : skript-parser is like a giant kitchen :
 * <ul>
 *   <li>The goal is to prepare food (write code).</li>
 *   <li>There are many different types of food to prepare ({@link TriggerContext}s).</li>
 *   <li>There are different means of actually preparing the food (different {@link SkriptEvent}s), each one being specific to one or more types of food</li>
 *   <li>Finally, in order to make the recipe come together, one needs the physical, tangible tools to achieve that ({@link Trigger}s)</li>
 * </ul>
 */
public abstract class SkriptEvent implements SyntaxElement {
    /**
     * Whether this event should trigger, given the {@link TriggerContext}
     * @param ctx the TriggerContext to check
     * @return whether the event should trigger
     */
    public abstract boolean check(TriggerContext ctx);

    public List<Statement> loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        return ScriptLoader.loadItems(section, parserState, logger);
    }

    /**
     * For virtually all programming and scripting languages, the need exists to have functions in order to not repeat
     * code too often. Skript is no exception, however, by default, every trigger is loaded in the order it appears in the file,
     * This is undesirable if we don't want the restriction of having to declare functions before using them. This is especially
     * counter-productive if we're dealing with multiple scripts.
     *
     * To solve this problem, {@link Trigger triggers} with a higher loading priority number will be loaded first.
     *
     * @return the loading priority number. 0 by default
     */
    public int getLoadingPriority() {
        return 500;
    }

    /**
     * A list of the classes of every syntax that is allowed to be used inside of this SkriptEvent. The default behavior
     * is to return an empty list, which equates to no restrictions. If overridden, this allows the creation of specialized,
     * DSL-like sections in which only select {@linkplain Statement statements} and other {@linkplain CodeSection sections}
     * (and potentially, but not necessarily, expressions).
     * @return a list of the classes of each syntax allowed inside this SkriptEvent
     * or {@code null} if you don't want to allow any
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
}
