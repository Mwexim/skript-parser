package io.github.syst3ms.skriptparser.tags;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.registration.tags.Tag;

public class TagReset implements Tag {

	static {
		Main.getMainRegistration().addTag(TagReset.class);
	}

	public boolean init(String key, String[] parameters) {
		return true;
	}

	public String getValue(String affected) {
		return affected;
	}

	public String toString(boolean debug) {
		return "<reset>";
	}
}
