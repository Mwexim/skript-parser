package io.github.syst3ms.skriptparser.classes;

import io.github.syst3ms.skriptparser.PatternParser;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.util.MultiMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SkriptRegistration {
	private String registerer;
	private MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
	private List<SyntaxInfo> effects = new ArrayList<>();
	private List<Type<?>> types = new ArrayList<>();
	private PatternParser patternParser;

	public SkriptRegistration(String registerer) {
		this.registerer = registerer;
		this.patternParser = new PatternParser();
	}

	public List<Type<?>> getTypes() {
		return types;
	}

	public MultiMap<Class<?>, ExpressionInfo<?, ?>> getExpressions() {
		return expressions;
	}

	public List<SyntaxInfo> getEffects() {
		return effects;
	}

	public String getRegisterer() {
		return registerer;
	}

	public <C extends Expression<T>, T> void addExpression(Class<C> c, Class<T> returnType, String... patterns) {
		List<PatternElement> elements = new ArrayList<>();
		for (String s : patterns) {
			elements.add(patternParser.parsePattern(s));
		}
		Type<T> t = TypeManager.getInstance().getByClass(returnType);
		if (t == null) {
			//TODO error
			return;
		}
		ExpressionInfo<C, T> info = new ExpressionInfo<>(c, elements, t);
		effects.add(info);
		expressions.putOne(returnType, info);
	}

	public <C> void addEffect(Class<C> c, String... patterns) {
		List<PatternElement> elements = new ArrayList<>();
		for (String s : patterns) {
			elements.add(patternParser.parsePattern(s));
		}
		SyntaxInfo<C> info = new SyntaxInfo<>(c, elements);
		effects.add(info);
	}

	public <T> void addType(Class<T> c, String name, String pattern) {
		types.add(new Type<>(c, name, pattern));
	}

	public <T> void addType(Class<T> c, String name, String pattern, Function<String, ? extends T> literalParser) {
		types.add(new Type<>(c, name, pattern, literalParser));
	}

	public void register() {
		SyntaxManager.getInstance().register(this);
		TypeManager.getInstance().register(this);
	}
}
