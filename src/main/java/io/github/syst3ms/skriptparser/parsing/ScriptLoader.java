package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileParser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.VoidElement;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Conditional;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.UnloadedTrigger;
import io.github.syst3ms.skriptparser.log.ErrorContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.util.FileUtils;
import io.github.syst3ms.skriptparser.util.MultiMap;

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
     * @param debug
     */
    public static List<LogEntry> loadScript(Path scriptPath, boolean debug) {
        FileParser parser = new FileParser();
        SkriptLogger logger = new SkriptLogger(debug);
        List<FileElement> elements;
        String scriptName;
        try {
            List<String> lines = FileUtils.readAllLines(scriptPath);
            scriptName = scriptPath.getFileName().toString().replaceAll("(.+)\\..+", "$1");
            elements = parser.parseFileLines(scriptName,
                    lines,
                    0,
                    1,
                    logger
            );
            logger.logOutput();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        logger.setFileInfo(scriptPath.getFileName().toString(), elements);
        List<UnloadedTrigger> unloadedTriggers = new ArrayList<>();
        for (FileElement element : elements) {
            logger.logOutput();
            logger.nextLine();
            if (element instanceof VoidElement)
                continue;
            if (element instanceof FileSection) {
                UnloadedTrigger trig = SyntaxParser.parseTrigger((FileSection) element, logger);
                if (trig == null) {
                    continue;
                }
                logger.setLine(logger.getLine() + ((FileSection) element).length());
                unloadedTriggers.add(trig);
            } else {
                logger.error("Can't have code outside of a trigger", ErrorType.STRUCTURE_ERROR);
            }
        }
        unloadedTriggers.sort((a, b) -> b.getTrigger().getEvent().getLoadingPriority() - a.getTrigger().getEvent().getLoadingPriority());
        for (UnloadedTrigger unloaded : unloadedTriggers) {
            logger.logOutput();
            logger.setLine(unloaded.getLine());
            Trigger loaded = unloaded.getTrigger();
            loaded.loadSection(unloaded.getSection(), unloaded.getParserState(), logger);
            unloaded.getEventInfo().getRegisterer().handleTrigger(loaded);
            triggerMap.putOne(scriptName, loaded);
        }
        logger.logOutput();
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
        List<FileElement> elements = section.getElements();
        logger.recurse();
        for (FileElement element : elements) {
            logger.logOutput();
            logger.nextLine();
            if (element instanceof VoidElement)
                continue;
            if (element instanceof FileSection) {
                FileSection sec = (FileSection) element;
                String content = sec.getLineContent();
                if (content.regionMatches(true, 0, "if ", 0, "if ".length())) {
                    String toParse = content.substring("if ".length());
                    Expression<Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse, SyntaxParser.MAYBE_CONDITIONAL, parserState, logger);
                    if (booleanExpression == null) {
                        continue;
                    } else if (parserState.forbidsSyntax(Conditional.class)) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error("Conditionals are not allowed in this section", ErrorType.SEMANTIC_ERROR);
                        continue;
                    }
                    items.add(new Conditional(sec, booleanExpression, Conditional.ConditionalMode.IF, parserState, logger));
                } else if (content.regionMatches(true, 0, "else if ", 0, "else if ".length())) {
                    if (items.size() == 0 ||
                        !(items.get(items.size() - 1) instanceof Conditional) ||
                        ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        logger.error("An 'else if' must be placed after an 'if'", ErrorType.STRUCTURE_ERROR);
                        continue;
                    }

                    String toParse = content.substring("else if ".length());
                    Expression<Boolean> booleanExpression = SyntaxParser.parseBooleanExpression(toParse, SyntaxParser.MAYBE_CONDITIONAL, parserState, logger);
                    if (booleanExpression == null) {
                        continue;
                    } else if (parserState.forbidsSyntax(Conditional.class)) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error("Conditionals are not allowed in this section", ErrorType.SEMANTIC_ERROR);
                        continue;
                    }
                    Conditional c = new Conditional(sec, booleanExpression, Conditional.ConditionalMode.ELSE_IF, parserState, logger);
                    ((Conditional) items.get(items.size() - 1)).setFallingClause(c);
                } else if (content.equalsIgnoreCase("else")) {
                    if (items.size() == 0 ||
                        !(items.get(items.size() - 1) instanceof Conditional) ||
                        ((Conditional) items.get(items.size() - 1)).getMode() == Conditional.ConditionalMode.ELSE) {
                        logger.error("An 'else' must be placed after an 'if' or an 'else if'", ErrorType.STRUCTURE_ERROR);
                        continue;
                    } else if (parserState.forbidsSyntax(Conditional.class)) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error("Conditionals are not allowed in this section", ErrorType.SEMANTIC_ERROR);
                        continue;
                    }
                    Conditional c = new Conditional(sec, null, Conditional.ConditionalMode.ELSE, parserState, logger);
                    ((Conditional) items.get(items.size() - 1)).setFallingClause(c);
                } else {
                    CodeSection codeSection = SyntaxParser.parseSection(sec, parserState, logger);
                    if (codeSection == null) {
                        continue;
                    } else if (parserState.forbidsSyntax(codeSection.getClass())) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error("The enclosing section does not allow the use of this section : " + codeSection.toString(null, logger.isDebug()), ErrorType.SEMANTIC_ERROR);
                        continue;
                    }
                    items.add(codeSection);
                }
            } else {
                String content = element.getLineContent();
                Statement eff = SyntaxParser.parseStatement(content, parserState, logger);
                if (eff == null) {
                    continue;
                }
                items.add(eff);
            }
        }
        logger.logOutput();
        for (int i = 0; i + 1 < items.size(); i++) {
            items.get(i).setNext(items.get(i + 1));
        }
        logger.callback();
        return items;
    }

    public static MultiMap<String, Trigger> getTriggerMap() {
        return triggerMap;
    }
}
