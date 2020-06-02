package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;

import java.util.List;

public abstract class RestrictiveSection extends CodeSection {

    @Override
    public void loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        parserState.setSyntaxRestrictions(getAllowedSyntaxes(), isRestrictingExpressions());
        super.loadSection(section, parserState, logger);
        parserState.clearSyntaxRestrictions();
    }

    protected abstract List<Class<? extends SyntaxElement>> getAllowedSyntaxes();

    protected abstract boolean isRestrictingExpressions();
}
