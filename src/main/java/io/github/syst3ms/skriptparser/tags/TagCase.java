package io.github.syst3ms.skriptparser.tags;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.registration.tags.Tag;

/**
 * Applies lowercase or uppercase to the affected string.
 * @name Case Tag
 * @type TAG
 * @pattern case=lower|upper
 * @since ALPHA
 * @author Mwexim
 */
public class TagCase implements Tag {

	static {
		Parser.getMainRegistration().addTag(TagCase.class);
	}

	String param;
	public boolean init(String key, String[] parameters) {
		if (key.equalsIgnoreCase("case")
				&& parameters.length == 1) {
			param = parameters[0];
			return true;
		}
		return false;
	}

	public String getValue(String affected) {
		return param.equalsIgnoreCase("upper") ? affected.toUpperCase()
				: param.equalsIgnoreCase("lower") ? affected.toLowerCase()
				: affected;
	}

	public String toString(boolean debug) {
		return "<case=" + param + ">";
	}
}
