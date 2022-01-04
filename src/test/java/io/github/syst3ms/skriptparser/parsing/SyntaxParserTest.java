package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.EventExpression;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.LogType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.PatternParser;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.syntax.TestContext;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.junit.Test;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.runners.model.MultipleFailureException;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SyntaxParserTest {
    static {
        TestRegistration.register();
    }

    /**
     * Amount of milliseconds a test can take at most before failing.
     */
    private static final int TEST_TIMEOUT = 10_000;

    private static final List<Throwable> errorsFound = new ArrayList<>();

    @Test
    public void testDefaultExpressions() {
        /*
         * Duration type with SimpleLiteral as default expression
         * Also checks if non-single types accept single default expressions.
         */
        var pattern = PatternParser.parsePattern("look for default expression [%durations?%]", new SkriptLogger()).orElseThrow(AssertionError::new);
        var context = new MatchContext(pattern, new ParserState(), new SkriptLogger());
        assert pattern.match("look for default expression", 0, context) != -1 : "pattern didn't match";

        var expressions = context.getParsedExpressions();
        assert expressions.size() == 1 : "expected exactly one (default) expression, found " + expressions.size();
        assert expressions.get(0) instanceof SimpleLiteral : "didn't find default expression of type SimpleLiteral";
        assert expressions.get(0).getSingle(TriggerContext.DUMMY).orElseThrow(AssertionError::new).equals(Duration.ZERO) : "default expression is not equal to Duration#ZERO constant";

        /*
         * String type with EventExpression as default expression
         */
        pattern = PatternParser.parsePattern("another default expression [%string?%]", new SkriptLogger()).orElseThrow(AssertionError::new);
        context = new MatchContext(pattern, new ParserState(), new SkriptLogger());
        assert pattern.match("another default expression", 0, context) != -1 : "pattern didn't match";

        expressions = context.getParsedExpressions();
        assert expressions.size() == 1 : "expected exactly one (default) expression, found " + expressions.size();
        assert expressions.get(0) instanceof EventExpression : "didn't find default expression of type EventExpression";
        // We use TestContext to eliminate all other context values that purposefully have been declared with SubTestContext
        assert expressions.get(0).getSingle(new TestContext()).orElseThrow(AssertionError::new).equals("This works as well") : "default expression is not equal to 'This works as well'";
    }

    @TestFactory
    public Iterator<DynamicNode> testSyntaxClasses() {
        String[] folders = {"effects", "expressions", "literals", "sections", "tags", "general"};
        ArrayList<DynamicNode> containerList = new ArrayList<>();
        for (String folder : folders) {
            ArrayList<DynamicTest> testsList = new ArrayList<>();
            for (File file : getResourceFolderFiles(folder)) {
                if (file.getName().startsWith("-"))
                    continue;
                testsList.add(
                        DynamicTest.dynamicTest("Testing '" + file.getName().replaceAll("\\..+$", "") + "'", () -> {
                            var executor = Executors.newSingleThreadExecutor();
                            Future<List<LogEntry>> future;

                            future = executor.submit(() -> ScriptLoader.loadScript(file.toPath(), true));
                            List<LogEntry> logs;
                            try {
                                logs = future.get(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
                                logs.removeIf(log -> log.getType() != LogType.ERROR);

                                if (!logs.isEmpty()) {
                                    logs.forEach(log -> errorsFound.add(new SkriptParserException(log.getMessage())));
                                } else {
                                    SkriptAddon.getAddons().forEach(SkriptAddon::finishedLoading);
                                }
                            } catch (TimeoutException ex) {
                                errorsFound.add(new TimeoutException("Parsing of file '" + file.getName() + "' timed out!"));
                            } catch (ExecutionException ex) {
                                errorsFound.add(ex);
                            }

                            // For some weird reason some errors are duplicated
                            var allErrors = new ArrayList<>(errorsFound);
                            Set<String> duplicateErrors = new HashSet<>();
                            allErrors.removeIf(val -> !duplicateErrors.add(val.getMessage()));

                            // Reset variables
                            Variables.clearVariables();
                            errorsFound.clear();

                            MultipleFailureException.assertEmpty(allErrors);
                        })
                );
            }

            containerList.add(DynamicContainer.dynamicContainer(
                    folder,
                    testsList
            ));
        }
        return containerList.iterator();
    }

    public static void addError(Throwable err) {
        errorsFound.add(err);
    }

    public static void removeError(Throwable err) {
        errorsFound.remove(err);
    }

    private static File[] getResourceFolderFiles(String folder) {
        URL url = ClassLoader.getSystemResource(folder);
        if (url != null) {
            var path = url.getPath();
            return new File(path).listFiles();
        }
        return new File[0];
    }
}
