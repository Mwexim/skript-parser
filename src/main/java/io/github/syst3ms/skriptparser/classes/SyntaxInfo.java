package io.github.syst3ms.skriptparser.classes;

import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.ArrayList;
import java.util.List;

public class SyntaxInfo<C> {
	private String[] originalPattern;
	private Class<C> c;
	private List<PatternElement> patterns = new ArrayList<>();

	public SyntaxInfo(Class<C> c, String[] originalPattern, List<PatternElement> patterns) {
		this.originalPattern = originalPattern;
		this.c = c;
		this.patterns = patterns;
	}

	public List<PatternElement> getPatterns() {
		return patterns;
	}

	public String[] getOriginalPattern() {
		return originalPattern;
	}

	public Class<C> getC() {
		return c;
	}
}
