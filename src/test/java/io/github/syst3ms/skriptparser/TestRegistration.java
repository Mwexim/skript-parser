package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.registration.DefaultRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.FileUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;

public class TestRegistration {

    public static void register() {
        try {
            Field regField = Parser.class.getDeclaredField("registration");
            regField.setAccessible(true);
            regField.set(null, new SkriptRegistration(new Skript(new String[0])));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        DefaultRegistration.register();
        try {
            FileUtils.loadClasses(
                    Path.of("build/classes/java/main"),
                    "io.github.syst3ms.skriptparser",
                    "effects",
                    "expressions",
                    "lang"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        Parser.getMainRegistration().register();
    }
}
