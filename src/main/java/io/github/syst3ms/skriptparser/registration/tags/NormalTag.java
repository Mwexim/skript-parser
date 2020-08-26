package io.github.syst3ms.skriptparser.registration.tags;

import org.jetbrains.annotations.Nullable;

import java.util.function.BinaryOperator;

/**
 * A normal tag is a tag that both has a key and value.
 */
public class NormalTag extends SkriptTag {

	private final String key;
	private final BinaryOperator<String> function;

	private String param;

	public NormalTag(String key, BinaryOperator<String> function) {
		this.key = key;
		this.function = function;
	}

	@Override
	public String getValue() {
		return function.apply(getParameter(), getAffected());
	}

	@Override
	public String getKey() {
		return key;
	}

	@Nullable
	public String getParameter() {
		return param;
	}

	public void setParameter(String param) {
		this.param = param;
	}

	/**
	 * Check if a given key matches this tag.
	 * @param key the key to match
	 * @return whether the match was successful or not
	 */
	public final boolean matches(String key) {
		return getKey().equalsIgnoreCase(key);
	}
}
