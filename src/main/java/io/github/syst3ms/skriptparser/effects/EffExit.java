package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.sections.SecWhile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Exits the entire trigger, preventing all upcoming statements to not get triggered.
 * There's also a possibility to only exit certain sections.
 * Note that stopping loops also stops while-loops.
 *
 * @name Exit
 * @pattern (exit|stop) [[the] [current] trigger]
 * @pattern (exit|stop) [(a|the [current]|this)] (section|loop|condition[al])
 * @pattern (exit|stop) %*integer% (section|loop|condition[al])[s]
 * @pattern (exit|stop) (every|all) [the] (section|loop|condition[al])s
 * @since ALPHA
 * @author Mwexim
 */
public class EffExit extends Effect {

    static {
        Main.getMainRegistration().addEffect(
            EffExit.class,
            "(exit|stop) [the] [trigger]",
                "(exit|stop) [(a|the [current]|this)] (0:section|1:loop|2:condition[al])",
                "(exit|stop) %*integer% (0:section|1:loop|2:condition[al])[s]",
                "(exit|stop) (every|all) [the] (0:section|1:loop|2:condition[al])s"
        );
    }

    private final static String[] names = {"section", "loop", "conditional"};
    private final List<CodeSection> currentSections = new ArrayList<>();

    private int pattern;
    private int parseMark;
    private Expression<Long> amount;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        currentSections.addAll(parseContext.getParserState().getCurrentSections());
        pattern = matchedPattern;
        parseMark = parseContext.getParseMark();
        if (pattern == 2)
                amount = (Expression<Long>) expressions[0];
        return true;
    }

    @Override
    protected void execute(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement walk(TriggerContext ctx) {
        switch (pattern) {
            case 0:
                return null;
            case 1:
                return escapeSections(1, this);
            case 2:
                Long l = amount.getSingle(ctx);
                if (l == null)
                    return null;

                return escapeSections(l, this);
            case 3:
                // The current Trigger itself is also a part of the current sections!
                return escapeSections(currentSections.size() - 1, this);
        }
        return null;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        switch (pattern) {
            case 1:
                return "exit this " + names[parseMark];
            case 2:
                return "exit " + amount.toString(ctx, debug) + " " + names[parseMark] + "s";
            case 3:
                return "exit all " + names[parseMark] + "s";
            case 0:
            default:
                return "exit";
        }
    }

    @Nullable
    private Statement escapeSections(long amount, Statement start) {
        Statement temp;
        while (amount > 0) {
            temp = start;
            start = start.getParent();
            if (start == null)
                return null;
            // 0 = all, 1 = only loops, 2 = only conditionals
            if (parseMark == 0
                    || parseMark == 1 && (start instanceof SecLoop || start instanceof SecWhile)
                    || parseMark == 2 && start instanceof Conditional) {
                amount--;
                continue;
            }
            return temp.getNext();
        }
        System.out.println(start.getNext());
        return start instanceof SecLoop ? ((SecLoop) start).getActualNext()
                : start instanceof SecWhile ? ((SecWhile) start).getActualNext()
                : start.getNext();
    }
}
