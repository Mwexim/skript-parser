package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileParser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.VoidElement;
import io.github.syst3ms.skriptparser.lang.Conditional;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.UnloadedTrigger;
import io.github.syst3ms.skriptparser.log.ErrorContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.util.FileUtils;
import io.github.syst3ms.skriptparser.util.classes.MultiMap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains the logic for loading, parsing and interpreting entire script files
 */
public class ScriptLoader {
    private static final MultiMap<String, Trigger> triggerMap = new MultiMap<>();

    /**
     * Parses and loads the provided script in memory
     * @param scriptPath the script file to load
     * @param debug whether debug is enabled
     */
    public static List<LogEntry> loadScript(Path scriptPath, boolean debug) {
        var logger = new SkriptLogger(debug);
        List<FileElement> elements;
        String scriptName;
        try {
            var lines = FileUtils.readAllLines(scriptPath);
            scriptName = scriptPath.getFileName().toString().replaceAll("(.+)\\..+", "$1");
            elements = FileParser.parseFileLines(scriptName,
                    lines,
                    0,
                    1,
                    logger
            );
            logger.finalizeLogs();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        logger.setFileInfo(scriptPath.getFileName().toString(), elements);
        List<UnloadedTrigger> unloadedTriggers = new ArrayList<>();
        for (var element : elements) {
            logger.finalizeLogs();
            logger.nextLine();
            if (element instanceof VoidElement)
                continue;
            if (element instanceof FileSection) {
                var trig = SyntaxParser.parseTrigger((FileSection) element, logger);
                trig.ifPresent(t -> {
                    logger.setLine(logger.getLine() + ((FileSection) element).length());
                    unloadedTriggers.add(t);
                });
            } else {
                logger.error(
                        "Can't have code outside of a trigger",
                        ErrorType.STRUCTURE_ERROR,
                        "Code always starts with a trigger (or event). Refer to the documentation to see which event you need, or indent this line so it is part of a trigger"
                );
            }
        }
        unloadedTriggers.sort((a, b) -> b.getTrigger().getEvent().getLoadingPriority() - a.getTrigger().getEvent().getLoadingPriority());
        for (var unloaded : unloadedTriggers) {
            logger.finalizeLogs();
            logger.setLine(unloaded.getLine());
            var loaded = unloaded.getTrigger();
            loaded.loadSection(unloaded.getSection(), unloaded.getParserState(), logger);
            unloaded.getEventInfo().getRegisterer().handleTrigger(loaded);
            triggerMap.putOne(scriptName, loaded);
        }
        logger.finalizeLogs();
        return logger.close();
    }

    /**
     * Parses all items inside of a given section.
     * @param section the section
     * @param logger the logger
     * @return a list of {@linkplain Statement effects} inside of the section
     */
    public static List<Statement> loadItems(FileSection section, ParserState parserState, SkriptLogger logger) {
        List<Statement> items = new ArrayList<>();
        var elements = section.getElements();
        logger.recurse();
        for (var element : elements) {
            logger.finalizeLogs();
            logger.nextLine();
            if (element instanceof VoidElement)
                continue;
            if (element instanceof FileSection) {
                var sec = (FileSection) element;
                var content = sec.getLineContent();
                if (content.regionMatches(true, 0, "if ", 0, "if ".length())) {
                    var toParse = content.substring("if ".length());
                    var booleanExpression = SyntaxParser.parseBooleanExpression(toParse, SyntaxParser.MAYBE_CONDITIONAL, parserState, logger);
                    if (booleanExpression.isEmpty())
                        continue;
                    booleanExpression = booleanExpression.filter(__ -> !parserState.forbidsSyntax(Conditional.class));
                    booleanExpression.ifPresent(b -> items.add(new Conditional(sec, b, Conditional.ConditionalMode.IF, parserState, logger)));
                    if (booleanExpression.isEmpty()) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error("Conditionals are not allowed in this section", ErrorType.SEMANTIC_ERROR);
                    }
                } else if (content.regionMatches(true, 0, "else if ", 0, "else if ".length())) {
                    if (items.size() == 0 ||
                            !(items.get(items.size() - 1) instanceof Conditional) ||
                            ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        logger.error(
                                "An 'else if' must be placed after an 'if' or 'else if'",
                                ErrorType.STRUCTURE_ERROR,
                                "Make sure the previous section you used was an 'if'-statement or 'else-if'-statement. If not, convert that section to one of the two or replace this line"
                        );
                        continue;
                    }
                    var toParse = content.substring("else if ".length());
                    var booleanExpression = SyntaxParser.parseBooleanExpression(toParse, SyntaxParser.MAYBE_CONDITIONAL, parserState, logger);
                    if (booleanExpression.isEmpty())
                        continue;
                    booleanExpression = booleanExpression.filter(__ -> !parserState.forbidsSyntax(Conditional.class));
                    booleanExpression.ifPresent(
                            b -> ((Conditional) items.get(items.size() - 1)).setFallingClause(
                                    new Conditional(sec, b, Conditional.ConditionalMode.ELSE_IF, parserState, logger)
                            )
                    );
                    if (booleanExpression.isEmpty()) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error("Conditionals are not allowed in this section", ErrorType.SEMANTIC_ERROR);
                    }
                } else if (content.equalsIgnoreCase("else")) {
                    if (items.size() == 0 ||
                            !(items.get(items.size() - 1) instanceof Conditional) ||
                            ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        logger.error(
                                "An 'else' must be placed after an 'if' or an 'else if'",
                                ErrorType.STRUCTURE_ERROR,
                                "Make sure the previous section you used was an 'if'-statement (or an 'else if'-statement). If not, convert that section to one of those statements or replace this line with one"

                        );
                        continue;
                    } else if (parserState.forbidsSyntax(Conditional.class)) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error("Conditionals are not allowed in this section", ErrorType.SEMANTIC_ERROR);
                        continue;
                    }
                    var c = new Conditional(sec, null, Conditional.ConditionalMode.ELSE, parserState, logger);
                    ((Conditional) items.get(items.size() - 1)).setFallingClause(c);
                } else {
                    var codeSection = SyntaxParser.parseSection(sec, parserState, logger);
                    if (codeSection.isEmpty()) {
                        continue;
                    } else if (parserState.forbidsSyntax(codeSection.get().getClass())) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error(
                                "The enclosing section does not allow the use of this section: "
                                        + codeSection.get().toString(TriggerContext.DUMMY, logger.isDebug()),
                                ErrorType.SEMANTIC_ERROR,
                                "The current section limits the usage of syntax. This means that certain syntax cannot be used here, which was the case. Remove the line entirely and refer to the documentation for the correct usage of this section"
                        );
                        continue;
                    }
                    items.add(codeSection.get());
                }
            } else {
                var content = element.getLineContent();
                var statement = SyntaxParser.parseStatement(content, parserState, logger);
                if (statement.isEmpty()) {
                    continue;
                } else if (parserState.forbidsSyntax(statement.get().getClass())) {
                    logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                    logger.error(
                            "The enclosing section does not allow the use of this effect: "
                                    + statement.get().toString(null, logger.isDebug()),
                            ErrorType.SEMANTIC_ERROR,
                            "The current section limits the usage of syntax. This means that certain syntax cannot be used here, which was the case. Remove the line entirely and refer to the documentation for the correct usage of this section"
                    );
                    continue;
                }
                items.add(statement.get());
            }
        }
        logger.finalizeLogs();
        for (var i = 0; i + 1 < items.size(); i++) {
            items.get(i).setNext(items.get(i + 1));
        }
        logger.callback();
        return items;
    }

    public static MultiMap<String, Trigger> getTriggerMap() {
        return triggerMap;
    }
}
