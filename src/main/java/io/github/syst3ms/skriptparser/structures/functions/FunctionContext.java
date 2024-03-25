package io.github.syst3ms.skriptparser.structures.functions;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

public class FunctionContext implements TriggerContext {

	private final Function<?> owningFunction;

	public FunctionContext(Function<?> owningFunction) {
		this.owningFunction = owningFunction;
	}

	public Function<?> getOwningFunction() {
		return owningFunction;
	}

	@Override
	public String getName() {
		return "function";
	}

}
