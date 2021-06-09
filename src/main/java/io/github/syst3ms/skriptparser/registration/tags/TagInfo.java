package io.github.syst3ms.skriptparser.registration.tags;

/**
 * A class containing info about a {@link Tag}.
 * @param <C> the {@link Tag} class
 * @author Mwexim
 */
public class TagInfo<C extends Tag> {
	private final Class<C> c;
	private final int priority;

	public TagInfo(Class<C> c, int priority) {
		this.c = c;
		this.priority = priority;
	}

	public Class<C> getSyntaxClass() {
		return c;
	}

	public int getPriority() {
		return priority;
	}
}
