package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.control.Finishing;
import io.github.syst3ms.skriptparser.lang.control.SelfReferencing;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.sections.SecConditional;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.sections.SecWhile;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Exits the entire trigger, preventing all upcoming statements to not get triggered.
 * There's also a possibility to only exit certain sections. If more sections are exited
 * than the total amount of nested sections, the trigger will stop.
 * Note that stopping loops also stops while-loops.
 *
 * @name Exit
 * @pattern (exit|stop) [[the] trigger]
 * @pattern (exit|stop) [a|the [current]|this] (section|loop|condition[al])
 * @pattern (exit|stop) %*integer% (section|loop|condition[al])[s]
 * @pattern (exit|stop) (every|all [[of] the]) (section|loop|condition[al])s
 * @since ALPHA
 * @author Mwexim
 */
public class EffExit extends Effect {
    static {
        Parser.getMainRegistration().addEffect(
                EffExit.class,
                "(exit|stop) [[the] trigger]",
                "(exit|stop) [a|the [current]|this] (0:section|1:loop|2:condition[al])",
                "(exit|stop) %*integer% (0:section|1:loop|2:condition[al])[s]",
                "(exit|stop) (every|all [[of] the]) (0:section|1:loop|2:condition[al])[s]"
        );
    }

    private final static String[] names = {"section", "loop", "conditional"};

    private final List<CodeSection> currentSections = new ArrayList<>();
    private int pattern;
    private int mark;
    private Literal<BigInteger> amount;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        currentSections.addAll(parseContext.getParserState().getCurrentSections());
        pattern = matchedPattern;
        mark = parseContext.getNumericMark();
        if (pattern == 2)
            amount = (Literal<BigInteger>) expressions[0];
        return true;
    }

    @Override
    protected void execute(TriggerContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        switch (pattern) {
            case 0:
                // We do this instead of returning an empty Optional,
                // because we need to call finish() on certain sections.
                return escapeSections(currentSections.size(), this);
            case 1:
                return escapeSections(1, this);
            case 2:
                return amount.getSingle()
                        .flatMap(sec -> escapeSections(sec.intValue(), this));
            case 3:
                // The current trigger is also a part of the current sections!
                return escapeSections(currentSections.size() - 1, this);
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        switch (pattern) {
            case 0:
                return "exit";
            case 1:
                return "exit this " + names[mark];
            case 2:
                return "exit " + amount.toString(ctx, debug) + " " + names[mark] + "s";
            case 3:
                return "exit all " + names[mark] + "s";
            default:
                throw new IllegalStateException();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<Statement> escapeSections(int amount, Statement start) {
        Optional<Statement> temp;
        Optional<Statement> statement = Optional.of(start);
        Statement stm = statement.get();
        while (amount > 0) {
            temp = statement;
            statement = statement.flatMap(Statement::getParent);
            if (statement.isEmpty())
                return Optional.empty();
            stm = statement.get();
            // 0 = all, 1 = only loops, 2 = only conditionals
            if (mark == 0
                    || mark == 1 && (stm instanceof SecLoop || stm instanceof SecWhile)
                    || mark == 2 && stm instanceof SecConditional) {
                if (stm instanceof Finishing)
                    ((Finishing) stm).finish();
                amount--;
                continue;
            }
            stm = temp.get();
            break;
        }

        return stm instanceof SelfReferencing
                ? ((SelfReferencing) stm).getActualNext()
                : (Optional<Statement>) stm.getNext();
    }
}
