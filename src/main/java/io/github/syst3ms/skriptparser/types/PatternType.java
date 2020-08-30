package io.github.syst3ms.skriptparser.types;

/**
 * A type used in a pattern.
 * Groups a {@link Type} and a number (single or plural) together (in contrast to {@link Type})
 */
public class PatternType<T> {
    private final Type<T> type;
    private final boolean single;

    public PatternType(Type<T> type, boolean single) {
        this.type = type;
        this.single = single;
    }

    public Type<T> getType() {
        return type;
    }

    public boolean isSingle() {
        return single;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof PatternType)) {
            return false;
        } else {
            var o = (PatternType<?>) obj;
            return type.equals(o.type) && single == o.single;
        }
    }

    @Override
    public String toString() {
        var forms = type.getPluralForms();
        return forms[single ? 0 : 1];
    }
}
