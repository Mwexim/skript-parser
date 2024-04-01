package io.github.syst3ms.skriptparser.tags;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.registration.tags.ContinuousTag;
import io.github.syst3ms.skriptparser.util.color.ConsoleColors;

import java.util.Locale;
import java.util.Optional;

public class TagColor implements ContinuousTag {

    static {
        Parser.getMainRegistration().addTag(TagColor.class);
    }

    private ConsoleColors color;

    @Override
    public boolean init(String key, String[] parameters) {
        final Optional<ConsoleColors> optional;
        if (key.equalsIgnoreCase("color") && parameters.length != 0) {
            optional = ConsoleColors.search(parameters[0]);
        } else {
            optional = ConsoleColors.search(key);
        }

        if (optional.isEmpty())
            return false;

        color = optional.get();
        return color != ConsoleColors.RESET;
    }

    @Override
    public String getValue() {
        return color.toString();
    }

    @Override
    public String toString(boolean debug) {
        return "<color="+color.name().toLowerCase()+">";
    }
}
