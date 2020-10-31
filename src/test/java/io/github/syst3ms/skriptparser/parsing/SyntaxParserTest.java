package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.log.LogType;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import org.junit.Test;

import java.io.File;
import java.net.URL;

public class SyntaxParserTest {
    static {
        TestRegistration.register();
    }

    @Test
    public void syntaxTest() {
        String[] folders = {"effects", "expressions", "literals", "sections", "tags"};
        for (String folder : folders) {
            for (File file : getResourceFolderFiles(folder)) {
                var logs = ScriptLoader.loadScript(file.toPath(), false);
                logs.removeIf(log -> log.getType() != LogType.ERROR);
                if (!logs.isEmpty()) {
                    logs.forEach(log -> {
                        throw new SkriptParserException(log.getMessage());
                    });
                }
                SkriptAddon.getAddons().forEach(SkriptAddon::finishedLoading);
            }
        }
    }

    private static File[] getResourceFolderFiles(String folder) {
        URL url = ClassLoader.getSystemResource(folder);
        var path = url.getPath();
        return new File(path).listFiles();
    }
}
