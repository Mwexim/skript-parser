package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.ThreadUtils;
import io.github.syst3ms.skriptparser.util.TimeUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Waits a certain duration and then executes all the code after this effect.
 * Note that new events may be triggered during the wait time.
 * When using the {@code wait while %=boolean%} effect, if the condition is never met,
 * the program could go to a recursive state, never escaping from an infinite loop.
 * This is why we advice you to give a limit.
 *
 * @name Wait
 * @pattern (wait|halt) [for] %duration%
 * @pattern (wait|halt) (until|while) %=boolean% [for %*duration%]
 * @since ALPHA
 * @author Mwexim
 */
public class EffWait extends Effect {
    static {
        Parser.getMainRegistration().addEffect(
                EffWait.class,
                "(wait|halt) [for] %duration%",
                "(wait|halt) (0:until|1:while) %=boolean% [for %*duration%]"
        );
    }

    private Expression<Duration> duration;
    private Expression<Boolean> condition;
    private boolean isConditional;
    private boolean negated;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        isConditional = matchedPattern == 1;
        if (isConditional) {
            condition = (Expression<Boolean>) expressions[0];
            if (expressions.length == 2)
                duration = (Literal<Duration>) expressions[1];
            negated = parseContext.getParseMark() == 0;
        } else {
            duration = (Expression<Duration>) expressions[0];
        }
        return true;
    }

    @Override
    protected void execute(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        if (getNext().isEmpty())
            return Optional.empty();

        if (isConditional) {
            var cond = condition.getSingle(ctx);
            // The code we want to run each check.
            Consumer<ExecutorService> code = exec -> {
                if (cond.filter(b -> negated == b.booleanValue()).isPresent()) {
                    Statement.runAll(getNext().get(), ctx);
                    exec.shutdownNow();
                }
            };

            if (duration == null) {
                var thread = ThreadUtils.buildPeriodic();
                thread.scheduleAtFixedRate(
                        () -> code.accept(thread),
                        0,
                        TimeUtils.TICK,
                        TimeUnit.MILLISECONDS
                );
            } else {
                var dur = ((Optional<Duration>) ((Literal<Duration>) duration).getSingle()).orElse(Duration.ZERO);
                long millis = dur.toMillis();
                var thread = ThreadUtils.buildPeriodic();
                thread.scheduleAtFixedRate(
                        () -> code.accept(thread),
                        0,
                        TimeUtils.TICK,
                        TimeUnit.MILLISECONDS
                );
                thread.schedule(
                        () -> {
                            Statement.runAll(getNext().get(), ctx);
                            thread.shutdownNow();
                        },
                        millis,
                        TimeUnit.MILLISECONDS
                );
            }
        } else {
            Optional<? extends Duration> dur = duration.getSingle(ctx);
            if (dur.isEmpty())
                return getNext();

            ThreadUtils.runAfter(() -> Statement.runAll(getNext().get(), ctx), dur.get());
        }
        return Optional.empty();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "wait " + duration.toString(ctx, debug);
    }
}
