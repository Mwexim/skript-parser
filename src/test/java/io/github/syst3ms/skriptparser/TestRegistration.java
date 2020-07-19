package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.registration.DefaultRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;

public class TestRegistration {

    public static void register() {
        try {
            Field regField = Main.class.getDeclaredField("registration");
            regField.setAccessible(true);
            regField.set(null, new SkriptRegistration(new Skript(new String[0])));
            Field jarField = FileUtils.class.getDeclaredField("jarFile");
            jarField.setAccessible(true);
            jarField.set(null, new File("./build/libs", "skript-parser.jar"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        DefaultRegistration.register();
        try {
            FileUtils.loadClasses(FileUtils.getCurrentJarFile(TestRegistration.class), "io.github.syst3ms.skriptparser", "effects", "expressions", "lang");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        Main.getMainRegistration().register();
    }
}
