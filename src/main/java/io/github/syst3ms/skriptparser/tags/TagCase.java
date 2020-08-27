package io.github.syst3ms.skriptparser.tags;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.registration.tags.Tag;

public class TagCase implements Tag {

	static {
		Main.getMainRegistration().addTag(TagCase.class);
	}

	String param;
	public boolean init(String key, String[] parameters) {
		if (key.equalsIgnoreCase("case")) {
			param = parameters[0];
			return parameters.length == 1;
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
