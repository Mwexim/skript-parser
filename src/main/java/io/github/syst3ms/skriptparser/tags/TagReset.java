package io.github.syst3ms.skriptparser.tags;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.registration.tags.Tag;

/**
 * A tag that resets all currently ongoing tags.
 * @name Reset Tag
 * @type TAG
 * @pattern reset, r
 * @since ALPHA
 * @author Mwexim
 */
public class TagReset implements Tag {

	static {
		Parser.getMainRegistration().addTag(TagReset.class, 3);
	}

	public boolean init(String key, String[] parameters) {
		return key.equalsIgnoreCase("reset")
				|| key.equalsIgnoreCase("r")
				&& parameters.length == 0;
	}

	public String getValue(String affected) {
		return affected;
	}

	public String toString(boolean debug) {
		return "<reset>";
	}
}
