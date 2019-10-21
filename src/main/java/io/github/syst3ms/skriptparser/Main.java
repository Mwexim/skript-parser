package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import io.github.syst3ms.skriptparser.registration.DefaultRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
            File addonFolder = new File(".", "addons");
            if (addonFolder.exists() && addonFolder.isDirectory()) {
                File[] addons = addonFolder.listFiles();
                if (addons != null) {
                    for (File addon : addons) {
                        if (addon.isFile() && addon.getName().endsWith(".jar")) {
                            URLClassLoader child = new URLClassLoader(
                                    new URL[] {addon.toURI().toURL()},
                                    Main.class.getClassLoader()
                            );
                            JarFile jar = new JarFile(addon);
                            Manifest manifest = jar.getManifest();
                            String main = manifest.getMainAttributes().getValue("Main-Class");
                            Class<?> mainClass = Class.forName(main, true, child);
                            try {
                                Method init = mainClass.getDeclaredMethod("initAddon");
                                init.invoke(null);
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException e) {
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
