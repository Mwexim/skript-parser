package io.github.syst3ms.skriptparser.tags;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.registration.tags.ContinuousTag;

/**
 * Adds special characters to the string.
 * @name Case Tag
 * @type TAG
 * @pattern br[eak]|tab
 * @since ALPHA
 * @author Mwexim
 */
public class TagSpecialCharacters implements ContinuousTag {
	static {
		Parser.getMainRegistration().addTag(TagSpecialCharacters.class);
	}

	private int type;

	public boolean init(String key, String[] parameters) {
		if (parameters.length != 0)
			return false;

		switch (key) {
			case "break":
			case "br":
				type = 0;
				return true;
			case "tab":
				type = 1;
				return true;
			default:
				return false;
		}
	}

	@Override
	public String getValue() {
		switch (type) {
			case 0:
				return System.lineSeparator();
			case 1:
				return "\t";
			default:
				throw new IllegalStateException();
		}
	}

	public String toString(boolean debug) {
		switch (type) {
			case 0:
				return "<break>";
			case 1:
				return "<tab>";
			default:
				throw new IllegalStateException();
		}
	}
}
