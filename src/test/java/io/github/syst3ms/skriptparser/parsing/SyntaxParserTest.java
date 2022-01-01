package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.lang.base.EventExpression;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.LogType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.PatternParser;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.syntax.TestContext;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.junit.Test;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.runners.model.MultipleFailureException;

import java.io.File;
import java.net.URL;
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
    public void testDefaultExpression() {
        assert TypeManager.getByClassExact(String.class).orElseThrow().getDefaultExpression().isPresent();

        var pattern = PatternParser.parsePattern("look for default expression [%strings?%]", new SkriptLogger()).orElseThrow(AssertionError::new);
        var context = new MatchContext(pattern, new ParserState(), new SkriptLogger());
        assert pattern.match("look for default expression", 0, context) != -1 : "pattern didn't match";

        var expr = context.getParsedExpressions().size() >= 1 ? context.getParsedExpressions().get(0) : null;
        assert expr instanceof EventExpression : "didn't find default expression of type EventExpression";
        assert expr.getValues(new TestContext())[0].equals("This works as well");
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
