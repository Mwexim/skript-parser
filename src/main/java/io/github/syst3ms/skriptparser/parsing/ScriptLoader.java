package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileParser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.file.VoidElement;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.UnloadedTrigger;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
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
     * Parses and loads the provided script in memory.
     * 
     * @param scriptPath the script file to load.
     * @param debug whether debug is enabled.
     */
    public static List<LogEntry> loadScript(Path scriptPath, boolean debug) {
        return loadScript(scriptPath, new SkriptLogger(debug), debug);
    }

    /**
     * Parses and loads the provided script in memory.
     * The provided SkriptLogger can be used within syntaxes to input erroring into the logs during parse time.
     * 
     * @param scriptPath the script file to load.
     * @param logger The {@link SkriptLogger} to use for the logged entries. Useful for custom logging.
     * @param debug whether debug is enabled.
     */
    public static List<LogEntry> loadScript(Path scriptPath, SkriptLogger logger, boolean debug) {
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
        logger.recurse();
        parserState.recurseCurrentStatements();
        List<Statement> items = new ArrayList<>();
        var elements = section.getElements();
        for (var element : elements) {
            logger.finalizeLogs();
            logger.nextLine();
            if (element instanceof VoidElement)
                continue;
            if (element instanceof FileSection) {
                var codeSection = SyntaxParser.parseSection((FileSection) element, parserState, logger);
                if (codeSection.isEmpty()) {
                    continue;
                }

                parserState.addCurrentStatement(codeSection.get());
                items.add(codeSection.get());
            } else {
                var statement = SyntaxParser.parseEffect(element.getLineContent(), parserState, logger);
                if (statement.isEmpty())
                    continue;

                parserState.addCurrentStatement(statement.get());
                items.add(statement.get());
            }
        }
        logger.finalizeLogs();
        for (var i = items.size() - 1; i > 0; i--) {
            items.get(i - 1).setNext(items.get(i));
        }
        logger.callback();
        parserState.callbackCurrentStatements();
        return items;
    }

    public static MultiMap<String, Trigger> getTriggerMap() {
        return triggerMap;
    }
}