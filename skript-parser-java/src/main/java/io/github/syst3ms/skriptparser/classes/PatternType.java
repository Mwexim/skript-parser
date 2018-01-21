package io.github.syst3ms.skriptparser.classes;

/**
 * A type used in a pattern.
 * Groups a {@link Type} and a number together (in contrast to {@link Type} itself)
 */
public class PatternType<T> {
	private Type<T> type;
	private boolean single;

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
		if (obj == null || !(obj instanceof PatternType)) {
			return false;
		} else {
			PatternType<?> o = (PatternType<?>) obj;
			return type.equals(o.type) && single == o.single;
		}
	}

    @Override
    public String toString() { // Not perfect, but good enough for toString()
        String baseName = type.getBaseName();
        if (baseName.endsWith("child")) { // This exception seems likely enough
            return baseName.replace("child", "children");
        } else if (baseName.matches(".*(s|ch|sh|x|z)")) {
            return baseName + "es";
        } else {
            return baseName + "s";
        }
    }
}
