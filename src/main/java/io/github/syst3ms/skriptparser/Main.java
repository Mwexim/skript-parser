package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import io.github.syst3ms.skriptparser.registration.DefaultRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.ConsoleColors;
import io.github.syst3ms.skriptparser.util.FileUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        List<String> sub = new ArrayList<>();
        sub.addAll(Arrays.asList(subPackages));
        sub.addAll(Arrays.asList("expressions", "effects", "event", "lang", "sections"));
        subPackages = sub.toArray(new String[0]);
        try {
            for (String mainPackage : mainPackages) {
                FileUtils.loadClasses(FileUtils.getCurrentJarFile(Main.class), mainPackage, subPackages);
            }
            if (standalone) {
                Path parserPath = Paths.get(Main.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                );
                Path addonFolderPath = Paths.get(parserPath.getParent().toString(), "addons");
                if (Files.isDirectory(addonFolderPath)) {
                    Files.walk(addonFolderPath)
                        .filter(Files::isRegularFile)
                        .filter((filePath) -> filePath.toString().endsWith(".jar"))
                        .forEach((Path addonPath) -> {
                            try {
                                URLClassLoader child = new URLClassLoader(
                                    new URL[]{addonPath.toUri().toURL()},
                                    Main.class.getClassLoader()
                                );
                                JarFile jar = new JarFile(addonPath.toString());
                                Manifest manifest = jar.getManifest();
                                String main = manifest.getMainAttributes().getValue("Main-Class");
                                if (main != null) {
                                    Class<?> mainClass = Class.forName(main, true, child);
                                    try {
                                        Method init = mainClass.getDeclaredMethod("initAddon");
                                        init.invoke(null);
                                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                                    } finally {
                                        jar.close();
                                    }
                                }
                            } catch (IOException | ClassNotFoundException e) {
                                System.err.println("Error while loading classes:");
                                e.printStackTrace();
                            }
                        });
                }
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error while loading classes:");
            e.printStackTrace();
        }
        Calendar time = Calendar.getInstance();
        List<LogEntry> logs = registration.register();
        if (!logs.isEmpty()) {
            System.out.print(ConsoleColors.RED.toString());
            System.out.println("Registration log :");
        }
        printLogs(logs, time);
        System.out.print(ConsoleColors.RESET.toString());
        if (!logs.isEmpty()) {
            System.out.println();
        }
        Path scriptPath = Paths.get(scriptName);
        logs = ScriptLoader.loadScript(scriptPath, debug);
        if (!logs.isEmpty()) {
            System.out.print(ConsoleColors.RED.toString());
            System.out.println("Parsing log :");
        }
        printLogs(logs, time);
        System.out.print(ConsoleColors.RESET.toString());
        SkriptAddon.getAddons().forEach(SkriptAddon::finishedLoading);
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
