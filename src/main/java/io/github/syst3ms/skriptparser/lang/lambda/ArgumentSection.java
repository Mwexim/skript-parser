package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

/**
 * A {@link CodeSection} that can hold information about arguments passed to it by {@link SkriptFunction} or
 * {@link SkriptConsumer}.
 *
 * This overrides {@link CodeSection#loadSection} in such a way that execution inside of the section doesn't continue
 * afterwards, since default Skript behavior is to go out one level when the end of a section is reached.
 * @see SkriptFunction
 * @see SkriptConsumer
 */
public abstract class ArgumentSection extends CodeSection {

    @Override
    public void loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        parserState.setSyntaxRestrictions(getAllowedSyntaxes(), isRestrictingExpressions());
        parserState.addCurrentSection(this);
        this.items = ScriptLoader.loadItems(section, parserState, logger);
        this.first = items.isEmpty() ? null : items.get(0);
        this.last = items.isEmpty() ? null : items.get(items.size() - 1);
        parserState.removeCurrentSection();
        parserState.clearSyntaxRestrictions();
    }

    private Object[] arguments;

    /**
     * @return the arguments passed to this section's code
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments that should be passed to the section code. Typically used by {@link SkriptFunction} and
     * {@link SkriptConsumer}.
     * @param arguments this section's arguments
     */
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }
}
