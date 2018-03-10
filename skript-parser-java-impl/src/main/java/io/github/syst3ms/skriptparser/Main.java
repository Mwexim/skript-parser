package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    private static final SkriptRegistration registration = new SkriptRegistration("main");

    public static void main(String[] args) {
        try {
            FileUtils.loadClasses("io.github.syst3ms.skriptparser", "expressions");
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error while loading classes :");
            e.printStackTrace();
        }
    }

    public static SkriptRegistration getMainRegistration() {
        return registration;
    }

}
