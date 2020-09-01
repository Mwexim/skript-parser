package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A top-level section, that is not contained in code.
 * Usually declares an event.
 */
public class Trigger extends CodeSection {
    private final SkriptEvent event;

    public Trigger(SkriptEvent event) {
        this.event = event;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public void loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        parserState.setSyntaxRestrictions(event.getAllowedSyntaxes(), event.isRestrictingExpressions());
        parserState.addCurrentSection(this);
        setItems(event.loadSection(section, parserState, logger));
        parserState.removeCurrentSection();
        parserState.clearSyntaxRestrictions();
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        return getFirst().filter(__ -> event.check(ctx));
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return event.toString(ctx, debug);
    }

    public SkriptEvent getEvent() {
        return event;
    }
}
