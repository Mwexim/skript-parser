package io.github.syst3ms.skriptparser.syntax.test;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.entries.CategorySection;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.variables.Variables;

import java.math.BigInteger;
import java.util.Optional;

public class SecCategory extends CategorySection {
    static {
        Parser.getMainRegistration().addSection(
                SecCategory.class,
                "category [test]"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Variables.setVariable("the_number", new BigInteger(options.get("number")), null, false);
        return Optional.of(sections.get("die"));
    }

    @Override
    protected EntryOption[] getConfiguration() {
        return new EntryOption[] {
                new EntryOption("die", false),
                new EntryOption("number", true),
                new EntryOption("unused", true),
                new EntryOption("optional", true, true)
        };
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "category";
    }
}
