package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import io.github.syst3ms.skriptparser.registration.DefaultRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Main {
    public static final String CONSOLE_FORMAT = "[%tT] %s: %s%n";
    private static SkriptRegistration registration;

    public static void main(String[] args) {
        boolean debug = false;
        String scriptName = "";
        String[] programArgs = new String[0];
        if (args.length == 0) {
            System.err.println("You need to provide a script name !");
            System.exit(1);
        } else if (args.length > 1 && args[0].equals("--debug")) {
            debug = true;
            scriptName = args[1];
            programArgs = Arrays.copyOfRange(args, 2, args.length);
        } else {
            scriptName = args[0];
            programArgs = Arrays.copyOfRange(args, 1, args.length);
        }
        Skript skript = new Skript(programArgs);
        registration = new SkriptRegistration(skript);
        DefaultRegistration.register();
        try {
            FileUtils.loadClasses("io.github.syst3ms.skriptparser", "expressions", "effects", "event", "lang");
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error while loading classes:");
            e.printStackTrace();
        }
        registration.register();
        File script = new File(scriptName);
        List<LogEntry> logs = ScriptLoader.loadScript(script, debug);
        Calendar time = Calendar.getInstance();
        for (LogEntry log : logs) {
            System.out.printf(CONSOLE_FORMAT, time, log.getType().name(), log.getMessage());
        }
    }

    public static SkriptRegistration getMainRegistration() {
        return registration;
    }

}
