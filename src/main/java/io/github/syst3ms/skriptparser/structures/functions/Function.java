package io.github.syst3ms.skriptparser.structures.functions;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

import java.util.Arrays;
import java.util.Optional;

public abstract sealed class Function<T> permits ScriptFunction, JavaFunction {

	private final String name;

	protected final FunctionParameter<?>[] parameters;

	private final Class<? extends T> returnType;

	private final boolean returnSingle;

	protected volatile Object[] returnValue;

	protected Function(String name, FunctionParameter<?>[] parameters,
					Class<? extends T> returnType, boolean returnSingle) {
		if (!Functions.isValidFunctionName(name)) throw new IllegalArgumentException("'" + name + "' is not a valid function name.");
		this.name = name;
		if (parameters != null) this.parameters = parameters;
		else this.parameters = new FunctionParameter<?>[0];
		this.returnType = returnType;
		this.returnSingle = returnSingle;
	}

	public abstract T[] execute(Object[][] params, TriggerContext ctx);

	public String getName() {
		return name;
	}

	public FunctionParameter<?>[] getParameters() {
		return parameters;
	}

	public Optional<Class<? extends T>> getReturnType() {
		return Optional.ofNullable(returnType);
	}

	public boolean isReturnSingle() {
		return returnSingle;
	}

	public void setReturnValue(Object[] returnValue) {
		this.returnValue = returnValue;
	}

	@Override
	public String toString() {
		return "Function{" +
					   "name='" + name + '\'' +
					   ", parameters=" + Arrays.toString(parameters) +
					   ", returnType=" + returnType +
					   ", returnSingle=" + returnSingle +
					   '}';
	}

}
