package io.github.syst3ms.skriptparser.registration.tags;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A simple tag is a tag that only has a key. The value is omitted and the returned value will always be static.
 */
public class SimpleTag extends Tag {

	private final String key;
	private final Character symbol;
	private final UnaryOperator<String> function;

	public SimpleTag(String key, UnaryOperator<String> function) {
		this(key, null, function);
	}

	public SimpleTag(String key, @Nullable Character symbol, UnaryOperator<String> function) {
		this.key = key;
		this.symbol = symbol;
		this.function = function;
	}

	@Override
	public String getValue() {
		return function.apply(getAffected());
	}

	@Override
	public String getKey() {
		return key;
	}

	/**
	 * The best part of tags is that you can make shortcuts for them in the form of one character.
	 * When specified, you can call this tag by using {@code &x}, where 'x' is the symbol defined here.
	 *
	 * It is advised to developers to not use this unless completely applicable,
	 * since the more abbreviations, the more chance they will clash with each other.
	 * @return the abbreviation of this tag, default {@code null}
	 */
	public Optional<Character> getSymbol() {
		return Optional.ofNullable(symbol);
	}

	@Override
	public String toString() {
		return "<" + key + ">";
	}

	/**
	 * Check if a given key matches this tag.
	 * @param key the key to match
	 * @param isSymbol whether the match needs to be against the key or against the symbol
	 * @return whether the match was successful or not
	 */
	public final boolean matches(String key, boolean isSymbol) {
		if (isSymbol) {
			return getSymbol()
					.filter(sym -> sym.equals(key.charAt(0)))
					.isPresent();
		}
		return getKey().equalsIgnoreCase(key);
	}
}
