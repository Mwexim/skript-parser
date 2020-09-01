package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

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

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }
}
