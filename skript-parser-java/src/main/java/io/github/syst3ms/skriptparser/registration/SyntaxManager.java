package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.util.MultiMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SyntaxManager {
	private static SyntaxManager instance = new SyntaxManager();
	private MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
	private MultiMap<String, SyntaxInfo<?>> syntaxes = new MultiMap<>();
	private List<SyntaxInfo<?>> effects = new ArrayList<>();

	private SyntaxManager() {
	}

	public static SyntaxManager getInstance() {
		return instance;
	}

	void register(SkriptRegistration reg) {
		for (SyntaxInfo info : reg.getEffects()) {
			effects.add(info);
			syntaxes.putOne(reg.getRegisterer(), info);
		}
		for (Map.Entry<Class<?>, List<ExpressionInfo<?, ?>>> entry : reg.getExpressions().entrySet()) {
			Class<?> key = entry.getKey();
			List<ExpressionInfo<?, ?>> infos = entry.getValue();
			for (ExpressionInfo<?, ?> info : infos) {
				expressions.putOne(key, info);
				syntaxes.putOne(reg.getRegisterer(), info);
			}
		}
	}

	public Iterable<SyntaxInfo<?>> getAddonSyntaxes(String name) {
		List<SyntaxInfo<?>> infos = syntaxes.get(name);
		return infos == null ? Collections.emptyList() : infos;
	}

	public Iterable<ExpressionInfo<?, ?>> getAllExpressions() {
		return expressions.getAllValues();
	}

	public <T> List<ExpressionInfo<?, ?>> getExpressionsByReturnType(Class<? extends T> c) {
		List<ExpressionInfo<?, ?>> infos = new ArrayList<>();
		for (Class<?> returnType : expressions.keySet()) {
			if (returnType.isAssignableFrom(c)) {
				infos.addAll(expressions.get(returnType));
			}
		}
		return infos;
	}

	public Iterable<SyntaxInfo<?>> getEffects() {
		return effects;
	}
}
