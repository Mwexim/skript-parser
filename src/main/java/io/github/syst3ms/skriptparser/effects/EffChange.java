package io.github.syst3ms.skriptparser.effects;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.Nullable;

/**
 *  A very general effect that can change many expressions. Many expressions can only be set and/or deleted, while some can have things added to or removed from them.
 *
 * @name Change: Set/Add/Remove/Delete/Reset
 * @pattern set %~objects% to %objects%
 * @pattern %~objects% = %objects%
 * @pattern add %objects% to %~objects%
 * @pattern %~objects% += %objects%
 * @pattern remove %objects% from %~objects%
 * @pattern %~objects% -= %~objects%
 * @pattern remove (all|every) %objects% from %~objects%
 * @pattern (delete|clear) %~objects%
 * @pattern reset %~objects%
 * @since ALPHA
 * @author Syst3ms
 */
public class EffChange extends Effect {
    public static final PatternInfos<ChangeMode> PATTERNS = new PatternInfos<>(new Object[][]{
            {"set %~objects% to %objects%", ChangeMode.SET},
            {"%~objects% = %objects%", ChangeMode.SET},
            {"add %objects% to %~objects%", ChangeMode.ADD},
            {"%~objects% += %objects%", ChangeMode.ADD},
            {"remove %objects% from %~objects%", ChangeMode.REMOVE},
            {"%~objects% -= %objects%", ChangeMode.REMOVE},
            {"remove (all|every) %objects% from %~objects%", ChangeMode.REMOVE_ALL},
            {"(delete|clear) %~objects%", ChangeMode.DELETE},
            {"reset %~objects%", ChangeMode.RESET}
    });

    static {
        Parser.getMainRegistration().addEffect(EffChange.class, PATTERNS.getPatterns());
    }

    private Expression<?> changed;
    @Nullable
    private Expression<?> changeWith;
    private ChangeMode mode;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        ChangeMode mode = PATTERNS.getInfo(matchedPattern);
        if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
            changed = expressions[0];
        } else if ((matchedPattern & 1) == 1 || mode == ChangeMode.SET) {
            // Notice the difference in the order of expressions
            changed = expressions[0];
            changeWith = expressions[1];
        } else {
            changed = expressions[1];
            changeWith = expressions[0];
        }
        this.mode = mode;

        var logger = parseContext.getLogger();
        String changedString = changed.toString(TriggerContext.DUMMY, logger.isDebug());

        if (changeWith == null) {
            assert mode == ChangeMode.DELETE || mode == ChangeMode.RESET;
            return changed.acceptsChange(mode).isPresent();
        } else {
            if (changed.acceptsChange(mode).isEmpty()) {
                switch (mode) {
                    case SET:
                        logger.error(changedString + " cannot be set to anything", ErrorType.SEMANTIC_ERROR);
                        break;
                    case ADD:
                        logger.error("Nothing can be added to " + changedString, ErrorType.SEMANTIC_ERROR);
                        break;
                    case REMOVE_ALL:
                    case REMOVE:
                        logger.error("Nothing can be removed from " + changedString, ErrorType.SEMANTIC_ERROR);
                        break;
                    default:
                    	throw new IllegalStateException();
                }
                return false;
            } else if (!changed.acceptsChange(mode, changeWith)) {
                var type = TypeManager.getByClassExact(changeWith.getReturnType());
                assert type.isPresent();
                String changeTypeName = type.get().withIndefiniteArticle(!changeWith.isSingle());
                switch (mode) {
                    case SET:
                        logger.error(changedString + " cannot be set to " + changeTypeName, ErrorType.SEMANTIC_ERROR);
                        break;
                    case ADD:
                        logger.error(changeTypeName + " cannot be added to " + changedString, ErrorType.SEMANTIC_ERROR);
                        break;
                    case REMOVE_ALL:
                    case REMOVE:
                        logger.error(changeTypeName + " cannot be removed from " + changedString, ErrorType.SEMANTIC_ERROR);
                        break;
                    case DELETE:
                    case RESET:
                    	throw new IllegalStateException();
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void execute(TriggerContext ctx) {
        if (changeWith == null) {
            changed.change(ctx, mode, new Object[0]);
        } else {
            var values = changeWith.getValues(ctx);
            if (values.length == 0)
                return;
            changed.change(ctx, mode, values);
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        String changedString = changed.toString(ctx, debug);
        String changedWithString = changeWith != null ? changeWith.toString(ctx, debug) : "";
        switch (mode) {
            case SET:
                return String.format("set %s to %s", changedString, changedWithString);
            case ADD:
                return String.format("add %s to %s", changedWithString, changedString);
            case REMOVE:
                return String.format("remove %s from %s", changedWithString, changedString);
            case DELETE:
            case RESET:
                return String.format("%s %s", mode.name().toLowerCase(), changedString);
            case REMOVE_ALL:
                return String.format("remove all %s from %s", changedWithString, changedString);
            default:
                throw new IllegalStateException();
        }
    }
}
