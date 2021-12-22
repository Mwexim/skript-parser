package io.github.syst3ms.skriptparser.syntax.test;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.entries.OptionLoader;
import io.github.syst3ms.skriptparser.lang.entries.SectionConfiguration;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.variables.Variables;

import java.math.BigInteger;
import java.util.Optional;

public class SecCategory extends CodeSection {
    static {
        Parser.getMainRegistration().addSection(
                SecCategory.class,
                "category [test]"
        );
    }

    private final SectionConfiguration config = new SectionConfiguration()
            .addLiteral("number", BigInteger.class)
            .addOptionList("multiple")
            .addOptionList("more multiple values")
            .addOption("unused")
            .addLoader(new OptionLoader(false, "optional", true))
            .addSection("die");

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public boolean loadSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        return config.loadConfiguration(this, section, parserState, logger);
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Variables.setVariable("the_number", config.getValue("number"), null, false);
        Variables.setVariable("multiple", String.join(";", config.getStringList("multiple")), null, false);
        return Optional.of(config.getSection("die"));
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "category";
    }
}
