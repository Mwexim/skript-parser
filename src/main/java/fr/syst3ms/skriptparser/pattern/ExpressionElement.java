package fr.syst3ms.skriptparser.pattern;

public class ExpressionElement implements PatternElement {
    private Class<?>[] classes;
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

    public ExpressionElement(Class<?>[] classes, boolean nullable, int time, Acceptance acceptance) {
        this.classes = classes;
        this.nullable = nullable;
        this.time = time;
        this.acceptance = acceptance;
    }

    @Override
    public int match(String s, int index) {
        // TODO
        return 0;
    }
}
