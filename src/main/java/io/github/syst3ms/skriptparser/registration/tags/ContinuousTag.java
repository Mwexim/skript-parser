package io.github.syst3ms.skriptparser.registration.tags;

/**
 * A continuous tag is a special sort of tag that always has a fixed value.
 * This means that it will not change the affected String, but rather add another String
 * in front of it.
 * You don't want continuous tags to combine with each other, because this behavior will be repeated.
 * That's why continuous tags combine with all tags except with other continuous tags.
 * @author Mwexim
 * @see Tag
 */
public interface ContinuousTag extends Tag {
	@Override
	default String getValue(String affected) {
		return getValue() + affected;
	}

	/**
	 * Returns the String that needs to be put in front of the affected String.
	 * @return the applied string
	 */
	String getValue();

	@Override
	default boolean combinesWith(Class<? extends Tag> tag) {
		return !ContinuousTag.class.isAssignableFrom(tag);
	}
}
