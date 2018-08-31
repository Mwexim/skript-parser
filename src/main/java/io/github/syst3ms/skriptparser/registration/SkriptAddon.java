package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Trigger;

import java.util.ArrayList;
import java.util.List;

public abstract class SkriptAddon {
    private static List<SkriptAddon> addons = new ArrayList<>();

    {
        addons.add(this);
    }

    public static List<SkriptAddon> getAddons() {
        return addons;
    }

    private String name;

    public abstract void handleTrigger(Trigger trigger);

    public void finishedLoading() {}
}
