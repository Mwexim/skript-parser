package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.util.ThreadUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Executes the code in the section asynchronously, meaning in another thread.
 * Note that the next code that isn't part of the section (intended the same amount of times) will be executed in the current thread again.
 * Only the code that is inside the section will be executed from another thread.
 * This may cause some delay. If you don't know what this is, you probably don't need it.
 *
 * @name Async
 * @type SECTION
 * @pattern async[hronous[ly]]
 * @since ALPHA
 * @author Mwexim
 */
public class SecAsync extends CodeSection {

    static {
        Main.getMainRegistration().addSection(
                SecAsync.class,
                "async[hronous[ly]]"
        );
    }

    @Override
    public void loadSection(@NotNull FileSection section, @NotNull ParserState parserState, SkriptLogger logger) {
        super.loadSection(section, parserState, logger);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Optional<? extends Statement>[] item = new Optional[]{getFirst()};
        ThreadUtils.runAsync(() -> {
            while (!item[0].equals(getNext())) // Calling equals() on optionals calls equals() on their values
                item[0] = item[0].flatMap(i -> i.walk(ctx));
        });
        return getNext();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "async";
    }
}
