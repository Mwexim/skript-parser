package fr.syst3ms.skriptparser.classes;

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
}
