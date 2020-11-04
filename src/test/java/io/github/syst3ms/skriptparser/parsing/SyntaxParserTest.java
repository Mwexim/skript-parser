package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.log.LogType;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.junit.Test;
import org.junit.runners.model.MultipleFailureException;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyntaxParserTest {
    static {
        TestRegistration.register();
    }

    private static final List<Throwable> errorsFound = new ArrayList<>();

    @Test
    public void syntaxTest() throws Exception {
        String[] folders = {"effects", "expressions", "literals", "sections", "tags"};
        for (String folder : folders) {
            for (File file : getResourceFolderFiles(folder)) {
                System.out.println(file.getName());
                if (file.getName().startsWith("-"))
                    continue;
                var logs = ScriptLoader.loadScript(file.toPath(), false);
                logs.removeIf(log -> log.getType() != LogType.ERROR);
                if (!logs.isEmpty()) {
                    logs.forEach(log -> errorsFound.add(new SkriptParserException(log.getMessage())));
                }
                SkriptAddon.getAddons().forEach(SkriptAddon::finishedLoading);
                // Reset variables
                Variables.clearVariables();
            }
        }
        // For some weird reason some errors are duplicated
        Set<String> duplicateErrors = new HashSet<>();
        errorsFound.removeIf(val -> !duplicateErrors.add(val.getMessage()));

        MultipleFailureException.assertEmpty(errorsFound);
    }

    public static void addError(Throwable err) {
        errorsFound.add(err);
    }

    public static void removeError(Throwable err) {
        errorsFound.remove(err);
    }

    private static File[] getResourceFolderFiles(String folder) {
        URL url = ClassLoader.getSystemResource(folder);
        var path = url.getPath();
        return new File(path).listFiles();
    }
}
