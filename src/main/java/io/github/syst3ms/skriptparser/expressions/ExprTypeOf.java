package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.PropertyExpression;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.Nullable;

/**
 * The type of an object.
 *
 * @name Type Of
 * @type EXPRESSION
 * @pattern [the] type of %objects%
 * @pattern %objects%'[s] type
 * @since ALPHA
 * @author Mwexim
 */
@SuppressWarnings("rawtypes")
public class ExprTypeOf extends PropertyExpression<Type, Object> {

	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprTypeOf.class,
				Type.class,
				true,
				4,
				"objects",
				"type");
	}

	@Override
	public Type[] getValues(TriggerContext ctx) {
		return TypeManager.getByClass(getOwner().getReturnType())
				.map(t -> new Type[] {t})
				.orElse(new Type[] {
						TypeManager.getByClassExact(Object.class).orElseThrow(AssertionError::new)
				});
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "type of " + getOwner().toString(ctx, debug);
	}
}
