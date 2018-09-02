package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import io.github.syst3ms.skriptparser.registration.DefaultRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    private static SkriptRegistration registration;

    public static void main(String[] args) {
        Skript skript = new Skript(args);
        registration = new SkriptRegistration(skript);
        DefaultRegistration.register();
        try {
            FileUtils.loadClasses("io.github.syst3ms.skriptparser", "expressions", "effects", "event");
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error while loading classes:");
            e.printStackTrace();
        }
        registration.register();
        if (args.length == 0) {
            System.err.println("You need to provide a script name !");
            System.exit(1);
        }
        File script = new File(".", args[0]);
        ScriptLoader.loadScript(script);
    }

    public static SkriptRegistration getMainRegistration() {
        return registration;
    }

}
