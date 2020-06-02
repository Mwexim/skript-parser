package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.CodeSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ParserState {
    private Class<? extends TriggerContext>[] currentContexts;
    private LinkedList<CodeSection> currentSections = new LinkedList<>();

    public Class<? extends TriggerContext>[] getCurrentContexts() {
        return currentContexts;
    }

    public List<CodeSection> getCurrentSections() {
        return Collections.unmodifiableList(currentSections);
    }

    public void setCurrentContexts(Class<? extends TriggerContext>[] currentContexts) {
        this.currentContexts = currentContexts;
    }

    public void addCurrentSection(CodeSection section) {
        currentSections.addFirst(section);
    }

    public void removeCurrentSection() {
        currentSections.removeFirst();
    }
}
