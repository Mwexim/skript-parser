package io.github.syst3ms.skriptparser.registration.tags;

import java.util.function.BinaryOperator;
import java.util.regex.Pattern;

/**
 * A dynamic tag is a tag that has no key.
 * The equality sign ('=') needs to be omitted, because the whole tag is matched against a {@link Pattern}.
 * This means that the returned value is semi-static: it is based on the matched string,
 * but also just gets put before the affected substring.
 */
public class DynamicTag extends SkriptTag {

	private final Pattern pattern;
	private final BinaryOperator<String> function;

	private String key;

	public DynamicTag(Pattern pattern, BinaryOperator<String> function) {
		this.pattern = pattern;
		this.function = function;
	}

	@Override
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * When the {@link #getKey()} method is empty,
	 * you can use this method to specify the pattern that needs to match the tag in order to successfully parse.
	 * @return the pattern that needs to match in order to parse
	 */
	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public String getValue() {
		return function.apply(getKey(), getAffected());
	}

	@Override
	public String toString() {
		return "<" + key + ">";
	}

	public final boolean matches(String key) {
		return getPattern().matcher(key).matches();
	}
}
