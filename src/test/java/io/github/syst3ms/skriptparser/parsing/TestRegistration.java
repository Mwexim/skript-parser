package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.Skript;
import io.github.syst3ms.skriptparser.registration.DefaultRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.FileUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;

public class TestRegistration {

    public static void register() {
        try {
            Field regField = Main.class.getDeclaredField("registration");
            regField.setAccessible(true);
            regField.set(null, new SkriptRegistration(new Skript(new String[0])));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        DefaultRegistration.register();
        try {
            FileUtils.loadClasses("io.github.syst3ms.skriptparser", "effects", "expressions", "lang");
        } catch (IOException | URISyntaxException e) {
            System.err.println("Something is fugged :");
            e.printStackTrace();
        }
        Main.getMainRegistration().register();
    }
}
