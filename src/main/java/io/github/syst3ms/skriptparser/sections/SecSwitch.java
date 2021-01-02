package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.effects.EffCase;
import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Basic switch control statement. Only {@link SecCase case} sections are allowed within this section.
 * The given expression will be matched against each case and all matching ones will be run.
 * Note that unlike in Java, each case is run separately.
 * <br>
 * <pre>
 *     switch (2) {
 *         case 1:
 *             print("Hello");
 *         case 2:
 *             print("World!");
 *             break;
 *         default:
 *             print("Nothing");
 *     }
 * </pre>
 * Would result in the following Skript code:
 * <br>
 * <pre>
 *     switch 2:
 *         case 1:
 *             print "Hello"
 *         case 1 or 2:
 *             print "World!"
 *         default:
 *             print "Nothing"
 * </pre>
 * Note that the default part is only executed if no match was found.
 *
 * @name Switch
 * @type SECTION
 * @pattern (switch|match) %object%
 * @since ALPHA
 * @author Mwexim
 * @see SecCase
 */
@SuppressWarnings("unchecked")
public class SecSwitch extends CodeSection {
    static {
        Parser.getMainRegistration().addSection(
                SecSwitch.class,
                "(switch|match) %object%"
        );
    }

    private Expression<Object> matched;

    private boolean hasMatched = false;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        matched = (Expression<Object>) expressions[0];
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        List<Statement> cases = new ArrayList<>();
        List<Statement> defaults = new ArrayList<>();
        for (Statement val : getItems()) {
            var sec = (MatchingElement) val;
            if (sec.isMatching()) {
                cases.add(val);
            } else {
                defaults.add(val);
            }
        }

        for (Statement element : cases) {
            element.walk(ctx);
        }
        if (!hasMatched) {
            for (Statement element : defaults) {
                element.walk(ctx);
            }
        }
        return getNext();
    }

    @Override
    protected List<Class<? extends SyntaxElement>> getAllowedSyntaxes() {
        return List.of(EffCase.class, SecCase.class);
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "switch " + matched.toString(ctx, debug);
    }

    public Expression<Object> getMatch() {
        return matched;
    }

    public boolean hasMatched() {
        return hasMatched;
    }

    public void setMatched(boolean hasMatched) {
        this.hasMatched = hasMatched;
    }

    public interface MatchingElement {
        /**
         * @return whether or not this statement will match against something or will act as the default part
         */
        boolean isMatching();
    }
}
