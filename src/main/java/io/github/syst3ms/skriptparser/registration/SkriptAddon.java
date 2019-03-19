package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Trigger;

import java.util.ArrayList;
import java.util.List;

/**
 * The base for all addons, modules that hook into the API to register syntax and handle triggers.
 */
public abstract class SkriptAddon {
    private static List<SkriptAddon> addons = new ArrayList<>();

    private String name;

    {
        addons.add(this);
    }

    public static List<SkriptAddon> getAddons() {
        return addons;
    }

    public abstract void handleTrigger(Trigger trigger);

    public void finishedLoading() {}
}
