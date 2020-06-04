package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ParserState {
    private Class<? extends TriggerContext>[] currentContexts;
    private LinkedList<CodeSection> currentSections = new LinkedList<>();
    private List<Class<? extends SyntaxElement>> allowedSyntaxes = Collections.emptyList();
    private boolean restrictingExpressions = false;

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

    public void setSyntaxRestrictions(List<Class<? extends SyntaxElement>> allowedSyntaxes, boolean restrictingExpressions) {
        this.allowedSyntaxes = allowedSyntaxes;
        this.restrictingExpressions = restrictingExpressions;
    }

    public void clearSyntaxRestrictions() {
        allowedSyntaxes = null;
        restrictingExpressions = false;
    }

    public boolean forbidsSyntax(Class<? extends SyntaxElement> c) {
        return !allowedSyntaxes.isEmpty() && !allowedSyntaxes.contains(c);
    }

    public boolean isRestrictingExpressions() {
        return restrictingExpressions;
    }
}
