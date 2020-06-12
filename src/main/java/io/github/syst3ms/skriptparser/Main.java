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
import java.util.ArrayList;
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
        init(scriptName, new String[0], new String[0], programArgs, debug, true);
    }

    /**
     * Starts the parser.
     * @param scriptName the name of the script to load
     * @param mainPackages packages inside which all subpackages containing classes to load may be present. Doesn't need
     *                     to contain Skript's own main packages.
     * @param subPackages the subpackages inside which classes to load may be present. Doesn't need to contain Skript's
     *                    own subpackages.
     * @param programArgs any other program arguments (typically from the command line)
     * @param debug whether to active debug mode or not
     * @param standalone whether the parser tries to load addons (standalone) or not (library)
     */
    public static void init(String scriptName, String[] mainPackages, String[] subPackages, String[] programArgs, boolean debug, boolean standalone) {
        Skript skript = new Skript(programArgs);
        registration = new SkriptRegistration(skript);
        DefaultRegistration.register();
        // Make sure Skript loads properly no matter what
        mainPackages = Arrays.copyOf(mainPackages, mainPackages.length + 1);
        mainPackages[mainPackages.length - 1] = "io.github.syst3ms.skriptparser";
        List<String> sub = new ArrayList<String>();
        sub.addAll(Arrays.asList(subPackages));
        sub.addAll(Arrays.asList("expressions", "effects", "event", "lang"));
        subPackages = sub.toArray(new String[0]);
        try {
            for (String mainPackage : mainPackages) {
                FileUtils.loadClasses(mainPackage, subPackages);
            }
            if (standalone) {
                File addonFolder = new File(".", "addons");
                if (addonFolder.exists() && addonFolder.isDirectory()) {
                    File[] addons = addonFolder.listFiles();
                    if (addons != null) {
                        for (File addon : addons) {
                            if (addon.isFile() && addon.getName().endsWith(".jar")) {
                                URLClassLoader child = new URLClassLoader(
                                        new URL[]{addon.toURI().toURL()},
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
                                } finally {
                                	jar.close();
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException e) {
            System.err.println("Error while loading classes:");
            e.printStackTrace();
        }
        Calendar time = Calendar.getInstance();
        List<LogEntry> logs = registration.register();
        if (!logs.isEmpty()) {
            System.out.println("Registration log :");
            System.out.println("---");
        }
        printLogs(logs, time);
        if (!logs.isEmpty()) {
            System.out.println();
        }
        File script = new File(scriptName);
        logs = ScriptLoader.loadScript(script, debug);
        if (!logs.isEmpty()) {
            System.out.println("Parsing log :");
            System.out.println("---");
        }
        printLogs(logs, time);
    }

    private static void printLogs(List<LogEntry> logs, Calendar time) {
        for (LogEntry log : logs) {
            System.out.printf(CONSOLE_FORMAT, time, log.getType().name(), log.getMessage());
        }
    }

    public static SkriptRegistration getMainRegistration() {
        return registration;
    }

}
