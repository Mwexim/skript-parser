package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.LogType;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.variables.DatabaseTest;

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

    @TestFactory
    public Iterator<DynamicNode> syntaxTest() {
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

                            DatabaseTest.clearAllVariables();
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
            var files = new File(path).listFiles();
            if (files != null)
            	return files;
        }
        return new File[0];
    }
}
