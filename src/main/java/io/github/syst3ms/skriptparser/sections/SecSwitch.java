package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.control.Finishing;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Basic switch control statement. Only {@link SecCase case} sections/effects are allowed within this section.
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
 * @pattern (switch|given|match) %object%
 * @since ALPHA
 * @author Mwexim
 * @see SecCase
 */
@SuppressWarnings("unchecked")
public class SecSwitch extends CodeSection implements Finishing {
    static {
        Parser.getMainRegistration().addSection(
                SecSwitch.class,
                "(switch|given|match) %object%"
        );
    }

    private Expression<Object> matched;
    private final List<SecCase> cases = new ArrayList<>();
    @Nullable
    private Iterator<SecCase> iterator;
    @Nullable
    private Statement byDefault;
    private boolean isDone = false;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        matched = (Expression<Object>) expressions[0];
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        if (iterator == null)
            iterator = cases.iterator();

        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        } else if (!isDone && byDefault != null) {
            return Optional.of(byDefault);
        } else {
            finish();
            return getNext();
        }
    }

    @Override
    public void finish() {
        iterator = null;
        isDone = false;
    }

    @Override
    protected Set<Class<? extends SyntaxElement>> getAllowedSyntaxes() {
        return Collections.singleton(SecCase.class);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "switch " + matched.toString(ctx, debug);
    }

    public Expression<Object> getMatch() {
        return matched;
    }

    public List<SecCase> getCases() {
        return cases;
    }

    public Optional<? extends Statement> getDefault() {
        return Optional.ofNullable(byDefault);
    }

    public void setDefault(Statement byDefault) {
        this.byDefault = byDefault;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }
}
