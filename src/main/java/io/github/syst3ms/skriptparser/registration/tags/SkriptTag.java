package io.github.syst3ms.skriptparser.registration.tags;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A tag is a part of code inside a string that changes certain parts of that string.
 * One could make a tag called {@code <blue>} that colors the substring blue.
 *
 * The substring this tag functions on is defined as such:
 * <ul>
 *     <li>If this is the only tag in the string, this will affect the whole string from the starting index</li>
 *     <li>If there is another tag inside this tag, this will affect the whole string from the starting index,
 *     but the nested tag may also affect parts from the already affected string on its own.</li>
 *     <li>If there comes a {@code reset} tag after this tag, it will affect the string from the starting index to the index of the {@code reset}-tag</li>
 * </ul>
 *
 * @see NormalTag
 * @see SimpleTag
 * @see DynamicTag
 */
public abstract class SkriptTag {

	private String affected = "";
	private int priority = 5;
	private boolean occasional = false;

	/**
	 * Returns the applied string.
	 * @see #getAffected()
	 * @return the function
	 */
	public abstract String getValue();

	/**
	 * The key of this tag.
	 * In the tag {@code <tag=value>}, 'tag' is the key.
	 *
	 * @return the key of this tag
	 */
	public abstract String getKey();

	/**
	 * Higher priority tags will be checked against first.
	 * @return the priority, default 5
	 */
	public int getPriority() {
		return priority;
	}

	public SkriptTag setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * When a tag is occasional, it only gets applied to the string when you call the {@link SkriptTags#occasionally(Supplier)} method.
	 * This is to make sure your tag only gets used when the developer specifically calls that method.
	 * @return whether or not this tag is occasional, default false
	 */
	public boolean isOccasional() {
		return occasional;
	}

	public SkriptTag setOccasional(boolean occasional) {
		this.occasional = occasional;
		return this;
	}

	/**
	 * Gets the affected substring this tag applies to.
	 * @return the affected string
	 */
	@Nullable
	public String getAffected() {
		return affected;
	}

	/**
	 * Sets the affected substring this tags applies to.
	 * @param affected the affected string
	 */
	public void setAffected(String affected) {
		this.affected = affected;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SkriptTag skriptTag = (SkriptTag) o;
		return priority == skriptTag.priority &&
				occasional == skriptTag.occasional;
	}

	@Override
	public int hashCode() {
		return Objects.hash(priority, occasional);
	}
}
