package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.registration.SkriptEventInfo;

/**
 * A {@link Trigger trigger}-to-be whose contents haven't been loaded yet. It will be loaded based on its event's
 * {@link SkriptEvent#getLoadingPriority() loading priority}.
 */
public class UnloadedTrigger {
    private final Trigger trigger;
    private final FileSection section;
    private final int line;
    private final SkriptEventInfo<?> eventInfo;
    private final ParserState parserState;

    public UnloadedTrigger(Trigger trigger, FileSection section, int line, SkriptEventInfo<?> eventInfo, ParserState parserState) {
        this.trigger = trigger;
        this.section = section;
        this.line = line;
        this.eventInfo = eventInfo;
        this.parserState = parserState;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public FileSection getSection() {
        return section;
    }

    public int getLine() {
        return line;
    }

    public ParserState getParserState() {
        return parserState;
    }

    public SkriptEventInfo<?> getEventInfo() {
        return eventInfo;
    }
}
