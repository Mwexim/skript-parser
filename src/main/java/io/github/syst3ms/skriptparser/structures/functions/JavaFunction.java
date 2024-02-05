package io.github.syst3ms.skriptparser.structures.functions;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

public abstract non-sealed class JavaFunction<T> extends Function<T> {

	protected JavaFunction(String name, FunctionParameter<?>[] parameters, Class<? extends T> returnType, boolean returnSingle) {
		super(name, parameters, returnType, returnSingle);
	}

	@Override
	public final T[] execute(Object[][] params, TriggerContext ctx) {
		for (Object[] param : params) {
			if (param == null || param.length == 0 || param[0] == null)
				return null;
		}
		return executeSimple(params);
	}

	public abstract T[] executeSimple(Object[][] params);

}
