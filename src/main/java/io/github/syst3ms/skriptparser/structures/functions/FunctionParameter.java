package io.github.syst3ms.skriptparser.structures.functions;

public class FunctionParameter<T> {

	private final String name;

	private final Class<? extends T> type;

	private final boolean single;

	public FunctionParameter(String name, Class<? extends T> type) {
		this(name, type, true);
	}

	public FunctionParameter(String name, Class<? extends T> type, boolean single) {
		this.name = name;
		this.type = type;
		this.single = single;
	}

	public String getName() {
		return name;
	}

	public Class<? extends T> getType() {
		return type;
	}

	public boolean isSingle() {
		return single;
	}

	@Override
	public String toString() {
		return "FunctionParameter{" +
					   "name='" + name + '\'' +
					   ", type=" + type +
					   ", single=" + single +
					   '}';
	}

}
