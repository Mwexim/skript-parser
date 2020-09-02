package io.github.syst3ms.skriptparser.registration.tags;

/**
 * Tags are elements one can use inside of strings to change parts of that string easily.
 * Some examples of valid tags and their behaviour:
 * <ul>
 *     <li>{@code <yellow>, <reset>}: singleton tags, no parameters</li>
 *     <li>{@code <case=uppercase>, <color=#ffffff}: parameter tags, one parameter</li>
 *     <li>{@code <link=text,link.to.my/image.png>}: plural tags, multiple parameters</li>
 *     <li>{@code &r}: short tags, no parameters</li>
 * </ul>
 * Note that you can use a backslash ('\') to escape characters inside tags.
 * @see io.github.syst3ms.skriptparser.tags.TagReset TagReset
 * @see io.github.syst3ms.skriptparser.tags.TagCase TagCase
 * @author Mwexim
 */
public interface Tag {

	/**
	 * Initialises this Tag before being used. This method is always called before all the others in
	 * an extending class.
	 * @param key the key that was matched. Note that this can be a single character used in a short tag
	 *            (like {@code &r}).
	 * @param parameters an array of strings representing all the parameters that are being passed
	 *                   to this syntax element. Elements of this array can't be {@code null} or {@link String#isEmpty() empty}.
	 * @return {@code true} if the tag was initialized successfully, {@code false} otherwise.
	 * A good practice is to check for the array length of the parameters to not interfere with other addons.
	 */
	boolean init(String key, String[] parameters);

	/**
	 * Returns a string applied by this tag.
	 * @param affected the string this tags affects.
	 * @return the applied string
	 */
	String getValue(String affected);

	String toString(boolean debug);

	/**
	 * Check if this tag is usable in a certain tag context. The default context is 'default'.
	 * @param tagCtx the tag context
	 * @return whether or not this tag is usable in this context, default true for every context
	 */
	default boolean isUsable(String tagCtx) {
		return true;
	}

	/**
	 * Some tags can stack on top of each other. A perfect example would be Markdown,
	 * where one can make a text both bold and italics at the same time.
	 * If this tag can complement such tag, you should define it here.
	 *
	 * Most of the times, you want a two-way connection,
	 * for example: bold combines with italics and vice-versa.
	 * @param tag the tag
	 * @return whether or not this tag can complement the given tag
	 */
	default boolean combinesWith(Class<? extends Tag> tag) {
		return false;
	}
}
