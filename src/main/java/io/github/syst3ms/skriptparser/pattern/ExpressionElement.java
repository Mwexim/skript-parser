package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.classes.PatternType;

import java.util.List;

public class ExpressionElement implements PatternElement {
    private List<PatternType<?>> types;
    private boolean nullable;
    private int time;
    private Acceptance acceptance;

    public enum Acceptance {
        BOTH(0),
        EXPRESSIONS_ONLY(1),
        LITERALS_ONLY(2);

        private final int id;

        Acceptance(int id) {
            this.id = id;
        }

        public static Acceptance getAcceptance(int id) {
            for (Acceptance a : values()) {
                if (a.id == id) {
                    return a;
                }
            }
            return null;
        }
    }

    public ExpressionElement(List<PatternType<?>> types, boolean nullable, int time, Acceptance acceptance) {
        this.types = types;
        this.nullable = nullable;
        this.time = time;
        this.acceptance = acceptance;
    }

    @Override
    public int match(String s, int index) {
        // TODO
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ExpressionElement)) {
            return false;
        } else {
            ExpressionElement e = (ExpressionElement) obj;
            return types.equals(e.types) && nullable == e.nullable && time == e.time && acceptance == e.acceptance;
        }
    }
}
