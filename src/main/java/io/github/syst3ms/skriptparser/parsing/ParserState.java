package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * An object that stores data about the current parsing, on the scale of the entire trigger.
 */
public class ParserState {
    private Set<Class<? extends TriggerContext>> currentContexts = new HashSet<>();
    private final LinkedList<CodeSection> currentSections = new LinkedList<>();
    private final LinkedList<LinkedList<Statement>> currentStatements = new LinkedList<>();
    private final LinkedList<Pair<Set<Class<? extends SyntaxElement>>, Boolean>> restrictions = new LinkedList<>();

    {
        currentStatements.add(new LinkedList<>());
        restrictions.add(new Pair<>(Collections.emptySet(), false));
    }

    /**
     * @return the {@link TriggerContext}s handled by the currently parsed event
     */
    public Set<Class<? extends TriggerContext>> getCurrentContexts() {
        return currentContexts;
    }

    /**
     * Sets the {@link TriggerContext}s handled by the currently parsed event
     * @param currentContexts the handled {@link TriggerContext}s
     */
    public void setCurrentContexts(Set<Class<? extends TriggerContext>> currentContexts) {
        this.currentContexts = currentContexts;
    }

    /**
     * @return a list of all enclosing {@linkplain CodeSection}s, with the closest one first
     */
    public LinkedList<CodeSection> getCurrentSections() {
        return new LinkedList<>(currentSections);
    }

    /**
     * Adds a new enclosing {@link CodeSection} to the hierarchy
     * @param section the enclosing {@link CodeSection}
     */
    public void addCurrentSection(CodeSection section) {
        currentSections.addFirst(section);
    }

    /**
     * Removes the current section from the hierarchy, after all parsing inside it has been completed.
     */
    public void removeCurrentSection() {
        currentSections.removeFirst();
    }

    /**
     * Returns a list of all consecutive, successfully parsed {@linkplain Statement}s
     * in the enclosing section.
     * This is essentially a list with all previously parsed items of this section.
     * @return a list of all {@linkplain Statement}s in the enclosing section.
     */
    public LinkedList<Statement> getCurrentStatements() {
        return currentStatements.getLast();
    }

    /**
     * Adds a new {@link Statement} to the items of the enclosing section.
     * @param statement the enclosing {@link Statement}
     */
    public void addCurrentStatement(Statement statement) {
        currentStatements.getLast().add(statement);
    }

    /**
     * Uses recursion to allow items of a new enclosing section to be added, preserving
     * the current items to be used when the {@linkplain #callbackCurrentStatements() callback}
     * has been invoked.
     */
    public void recurseCurrentStatements() {
        currentStatements.addLast(new LinkedList<>());
    }

    /**
     * Clears all stored items of this enclosing section,
     * after all parsing inside it has been completed.
     */
    public void callbackCurrentStatements() {
        currentStatements.removeLast();
    }

    /**
     * Define the syntax restrictions enforced by the current section
     * @param allowedSyntaxes all allowed syntaxes
     * @param restrictingExpressions whether expressions are also restricted
     */
    public void setSyntaxRestrictions(Set<Class<? extends SyntaxElement>> allowedSyntaxes, boolean restrictingExpressions) {
        restrictions.addLast(new Pair<>(allowedSyntaxes, restrictingExpressions));
    }

    /**
     * Clears the previously enforced syntax restrictions
     */
    public void clearSyntaxRestrictions() {
        restrictions.removeLast();
    }

    /**
     * @param c the class of the syntax
     * @return whether the current syntax restrictions forbid a given syntax or not
     */
    public boolean forbidsSyntax(Class<? extends SyntaxElement> c) {
        var allowedSyntaxes = restrictions.getLast().getFirst();
        return !allowedSyntaxes.isEmpty() && !allowedSyntaxes.contains(c);
    }

    /**
     * @return whether the current syntax restrictions also apply to expressions
     */
    public boolean isRestrictingExpressions() {
        return restrictions.getLast().getSecond();
    }
}
