package io.github.syst3ms.skriptparser.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser instance used for matching a pattern to a syntax, stores a parse mark
 */
public class SkriptParser {
	private String originalPattern;
	private List<Expression<?>> parsedExpressions = new ArrayList<>();
	private List<String> regexMatches = new ArrayList<>();
	private int parseMark = 0;

	public SkriptParser(String originalPattern) {
		this.originalPattern = originalPattern;
	}

	public List<Expression<?>> getParsedExpressions() {
		return parsedExpressions;
	}

	public List<String> getRegexMatches() {
		return regexMatches;
	}

	public int getParseMark() {
		return parseMark;
	}

	public void addMark(int mark) {
		parseMark ^= mark;
	}

	public void addRegexMatch(String match) {
		regexMatches.add(match);
	}

	public String getOriginalPattern() {
		return originalPattern;
	}
}
