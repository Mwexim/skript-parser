package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An object that stores data about the current parsing, on the scale of the entire trigger.
 */
public class ParserState {
    private Set<Class<? extends TriggerContext>> currentContexts = new HashSet<>();
    private final LinkedList<CodeSection> currentSections = new LinkedList<>();

    // Current statements
    private LinkedList<Statement> currentStatements = new LinkedList<>();
    private final LinkedList<LinkedList<Statement>> statementsReference = new LinkedList<>();

    // Restrictions
    private List<Class<? extends SyntaxElement>> allowedSyntaxes = Collections.emptyList();
    private boolean restrictingExpressions = false;
    private final LinkedList<Pair<List<Class<? extends SyntaxElement>>, Boolean>> restrictionsReference = new LinkedList<>();

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
        return new LinkedList<>(currentStatements);
    }

    /**
     * Adds a new {@link Statement} to the items of the enclosing section.
     * @param statement the enclosing {@link Statement}
     */
    public void addCurrentStatement(Statement statement) {
        currentStatements.addLast(statement);
    }

    public void recurseCurrentStatements() {
        statementsReference.addLast(currentStatements);
        currentStatements.clear();
    }

    /**
     * Clears all stored items of this enclosing section,
     * after all parsing inside it has been completed.
     */
    public void clearCurrentStatements() {
        currentStatements = statementsReference.getLast();
        statementsReference.removeLast();
    }

    /**
     * Define the syntax restrictions enforced by the current section
     * @param allowedSyntaxes all allowed syntaxes
     * @param restrictingExpressions whether expressions are also restricted
     */
    public void setSyntaxRestrictions(List<Class<? extends SyntaxElement>> allowedSyntaxes, boolean restrictingExpressions) {
        restrictionsReference.addLast(new Pair<>(this.allowedSyntaxes, this.restrictingExpressions));
        this.allowedSyntaxes = allowedSyntaxes;
        this.restrictingExpressions = restrictingExpressions;
    }

    /**
     * Clears the previously enforced syntax restrictions
     */
    public void clearSyntaxRestrictions() {
        allowedSyntaxes = restrictionsReference.getLast().getFirst();
        restrictingExpressions = restrictionsReference.getLast().getSecond();
        restrictionsReference.removeLast();
    }

    /**
     * @param c the class of the syntax
     * @return whether the current syntax restrictions forbid a given syntax or not
     */
    public boolean forbidsSyntax(Class<? extends SyntaxElement> c) {
        return !allowedSyntaxes.isEmpty() && !allowedSyntaxes.contains(c);
    }

    /**
     * @return whether the current syntax restrictions also apply to expressions
     */
    public boolean isRestrictingExpressions() {
        return restrictingExpressions;
    }
}
