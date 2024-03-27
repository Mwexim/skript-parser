package io.github.syst3ms.skriptparser.variables;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileParser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DatabaseTest {

    private static final List<String> LINES = Arrays.asList(
            "databases:",
                "\tram:",
                    "\t\ttest node: true",
                    "\t\tpattern: .*"
            );

    static {
        Variables.registerStorage(RamStorage.class, "ram");
    }

    @BeforeEach
    public void clearVariables() {
        Variables.clearVariables();
    }

    public static void clearAllVariables() {
        Variables.clearVariables();
    }

    @Before
    public void setupVariables() {
        Variables.registerStorage(RamStorage.class, "ram");
        SkriptLogger logger = new SkriptLogger(true);
        List<FileElement> elements = FileParser.parseFileLines("database-test", LINES, 0, 1, logger);
        assert elements.size() > 0;
        FileElement element = elements.get(0);
        assert element instanceof FileSection;
        Variables.load(logger, (FileSection) element);
        logger.finalizeLogs();
        logger.close();
        assert Variables.AVAILABLE_STORAGES.size() > 0;
        assert Variables.STORAGES.size() > 0;
        Variables.setVariable("test", "Hello World!", null, false);
    }

    @Test
    public void testVariables() throws InterruptedException {
        Thread.sleep(1);
        assert RamStorage.VARIABLES.containsKey("test");
        Optional<Object> object = Variables.getVariable("test", null, false);
        assert object.isPresent();
        assert object.get().equals("Hello World!");
        Variables.setVariable("test", "Hello New World!", null, false);
        Thread.sleep(1);
        Optional<Object> newObject = Variables.getVariable("test", null, false);
        assert newObject.isPresent();
        assert newObject.get().equals("Hello New World!");
        assert RamStorage.VARIABLES.containsKey("test");
        SerializedVariable variable = RamStorage.VARIABLES.get("test");
        Optional<?> value = RamStorage.SELF.deserialize(variable.value.type, variable.value.data);
        assert value.isPresent();
        assert value.get().equals("Hello New World!") : value.get();
        Variables.setVariable("test", null, null, false);
        Thread.sleep(1);
        assert !Variables.getVariable("test", null, false).isPresent();
        assert !RamStorage.VARIABLES.containsKey("test");
    }

}
