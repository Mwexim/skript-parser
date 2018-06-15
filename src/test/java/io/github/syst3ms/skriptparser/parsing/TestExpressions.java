package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings("unchecked")
public class TestExpressions {
    public static class ExprSquared implements Expression<Number> {
        private Expression<Number> number;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
            number = (Expression<Number>) expressions[0];
            return true;
        }

        @Override
        public Number[] getValues(TriggerContext e) {
            Number n = number.getSingle(e);
            if (n == null)
                return new Number[0];
            return new Number[]{n.doubleValue() * n.doubleValue()};
        }

        @Override
        public String toString(@Nullable TriggerContext e, boolean debug) {
            return number.toString(e, debug) + " squared";
        }
    }

    public static class ExprRandom implements Expression<Number> {
        private static final Random rnd = new Random();
        private Expression<Number> lowerBound, upperBound;
        private boolean integer, exclusive;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
            lowerBound = (Expression<Number>) expressions[0];
            upperBound = (Expression<Number>) expressions[1];
            integer = (parseResult.getParseMark() & 1) == 1;
            exclusive = (parseResult.getParseMark() & 2) == 2;
            return true;
        }

        @Override
        public Number[] getValues(TriggerContext e) {
            Number lower = lowerBound.getSingle(e);
            Number upper = upperBound.getSingle(e);
            if (lower == null || upper == null)
                return new Number[0];
            if (integer) {
                int l = lower.intValue();
                int u = upper.intValue();
                if (l > u) { // Just swap the variables
                    int temp = l;
                    l = u;
                    u = temp;
                }
                if (exclusive) {
                    l++;
                    u--;
                }
                return new Number[]{l + rnd.nextInt(u - l)};
            } else {
                double l = lower.doubleValue();
                double u = upper.doubleValue();
                if (l > u) { // Just swap the variables
                    double temp = l;
                    l = u;
                    u = temp;
                }
                double r = l + rnd.nextDouble() * (u - l);
                while (r == l || r == u)
                    r = l + rnd.nextDouble() * (u - l);
                return new Number[]{r};
            }
        }

        @Override
        public String toString(@Nullable TriggerContext e, boolean debug) {
            return "random " +
                   (integer ? "integer" : "number") +
                   " between " +
                   lowerBound.toString(e, debug) +
                   " and " +
                   upperBound.toString(e, debug);
        }
    }

    public static class ExprSubstring implements Expression<String> {
        private Expression<String> string;
        private Expression<Number> startIndex, endIndex;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
            string = (Expression<String>) expressions[0];
            startIndex = (Expression<Number>) expressions[1];
            endIndex = (Expression<Number>) expressions[2];
            return true;
        }

        @Override
        public String[] getValues(TriggerContext context) {
            String str = string.getSingle(context);
            Number start = startIndex.getSingle(context);
            Number end = endIndex.getSingle(context);
            if (str == null || start == null || end == null)
                return new String[0];
            int s = start.intValue();
            int e = end.intValue();
            if (s < 0 && e >= str.length())
                return new String[0];
            return new String[]{str.substring(s, e)};
        }

        @Override
        public String toString(@Nullable TriggerContext e, boolean debug) {
            return "substring " +
                   string.toString(e, debug) +
                   " from " +
                   startIndex.toString(e, debug) +
                   " to " +
                   endIndex.toString(e, debug);
        }
    }
}
