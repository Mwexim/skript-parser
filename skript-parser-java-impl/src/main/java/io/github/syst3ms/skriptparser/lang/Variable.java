package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.ClassUtils;
import io.github.syst3ms.skriptparser.variables.Variables;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class Variable<T> implements Expression<T> {
	private final VariableString name;
	private final boolean local;
	private final boolean list;
	private final Class<?>[] types;
	private final Class<?> supertype;


	public Variable(VariableString name, boolean local, boolean list, Class<?>[] types) {
		this.name = name;
		this.local = local;
		this.list = list;
		this.types = types;
		this.supertype = ClassUtils.getCommonSuperclass(types);
	}

	public Object getRaw(Event e) {
		String n = name.toString(e);
		if (n.endsWith(Variables.LIST_SEPARATOR + "*") != list) // prevents e.g. {%expr%} where "%expr%" ends with "::*" from returning a Map
			return null;
		Object val = Variables.getVariable(n, e, local);
		if (val == null)
			return Variables.getVariable((local ? Variables.LOCAL_VARIABLE_TOKEN : "") + name.defaultVariableName(), e, false);
		return val;
	}

	@Override
	public T[] getValues(Event e) {
		// TODO when converters are done
		/*
		if(list)
			return getConvertedArray(e);
		final T o = getConverted(e);
		if (o == null) {
			final T[] r = (T[]) Array.newInstance(superType, 0);
			assert r != null;
			return r;
		}
		final T[] one = (T[]) Array.newInstance(superType, 1);
		one[0] = o;
		return one;
		*/
		return (T[]) new Object[0];
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSingle() {
		return !list;
	}

	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("var") || s.equalsIgnoreCase("variable") || s.equalsIgnoreCase("value") || s.equalsIgnoreCase("index");
	}

	@Override
	public Iterator<T> iterator(Event event) {
		return Collections.emptyIterator();
	}

	public Iterator<Map.Entry<String, Object>> variableIterator() {
		return Collections.emptyIterator();
	}

	@Override
	public String toString(Event e, boolean debug) {
		if (e != null)
			return TypeManager.toString((Object[]) getValues(e));
		String name = this.name.toString(null, debug);
		return "{" + (local ? Variables.LOCAL_VARIABLE_TOKEN : "") + name.substring(1, name.length() - 1) + "}" + (debug ? "(as " + supertype.getSimpleName() + ")" : "");
	}

	public Class<? extends T> getReturnType() {
		return (Class<? extends T>) supertype;
	}
}