package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Whenever a birth section is called, one of the methods inside must be {@link EffDeath}.
 * If not, an error will be thrown and the test will fail.
 * Use this to make sure a script reaches a certain line of code.
 * Cannot be used outside of tests.
 *
 * @name Birth
 * @type SECTION
 * @pattern birth
 * @since ALPHA
 * @author Mwexim
 */
public class SecBirth extends CodeSection {
    static {
        Parser.getMainRegistration().addSection(
                SecBirth.class,
                "birth"
        );
    }

    private static final Map<SecBirth, EffDeath> currentBirths = new HashMap<>();

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        var item = getFirst();
        while (!item.equals(getNext())) {
            item = item.flatMap(val -> val.walk(ctx));
        }
        if (currentBirths.containsKey(this)) {
            throw new SkriptRuntimeException("Birth section was not killed afterwards");
        }
        currentBirths.remove(this);
        return getNext();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "birth";
    }

    public static Map<SecBirth, EffDeath> getBirths() {
        return currentBirths;
    }
}
