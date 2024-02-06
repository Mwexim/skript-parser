package io.github.syst3ms.skriptparser.structures.functions;

import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.variables.Variables;

import java.util.Arrays;

public final class ScriptFunction<T> extends Function<T> {

	private final String scriptName;

	private final boolean local;
	private Trigger trigger;

	ScriptFunction(String scriptName, boolean local, String name, FunctionParameter<?>[] parameters, Class<? extends T> returnType, boolean returnSingle) {
		super(name, parameters, returnType, returnSingle);
		this.scriptName = scriptName;
		this.local = local;
	}

	@Override
	public T[] execute(Object[][] params, TriggerContext ctx) {
		FunctionContext functionContext = new FunctionContext(this);
		for (int i = 0; i < params.length; i++) {
			FunctionParameter<?> p = parameters[i];
			Object[] val = params[i];
			if (p.isSingle() && val.length > 0) {
				Variables.setVariable(p.getName(), val[0], functionContext, true);
			} else {
				for (int j = 0; j < val.length; j++) {
					Variables.setVariable(p.getName() + "::" + (j + 1), val[j], functionContext, true);
				}
			}
		}
		Statement.runAll(trigger, functionContext);
		if (!(getReturnType().isPresent()/* && getReturnType().get().isAssignableFrom(returnValue.getClass())*/)) {
			return null;
		}
		return (T[]) returnValue;
	}

	public String getScriptName() {
		return scriptName;
	}

	public boolean isLocal() {
		return local;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

}
