package io.github.syst3ms.skriptparser.classes;

import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.ArrayList;
import java.util.List;

public class SyntaxInfo<C> {
	private Class<C> c;
	private List<PatternElement> patterns = new ArrayList<>();

	public SyntaxInfo(Class<C> c, List<PatternElement> patterns) {
		this.c = c;
		this.patterns = patterns;
	}

	public List<PatternElement> getPatterns() {
		return patterns;
	}
}
