package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.types.TypeManager;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If the annotation {@code @ContextValueMethod} is present on the declaration of a method,
 * that means that this method can now be used as a bridge between context values and the
 * actual context class.
 * The following rules are expected to be followed:
 * <ul>
 *     <li>The method has no arguments (e.g. the method acts as a supplier).</li>
 *     <li>The method returns a valid registered {@linkplain TypeManager#getByClass(Class) type},
 *     or an array of such type.</li>
 *     <li>The method does not return a primitive (although their Java-class
 *     counterparts are allowed, e.g. {@code int} is not allowed, but {@link Integer} is)</li>
 *     <li>The method is public.</li>
 *     <li>The {@link #name() name} of this ContextValueMethod only contains letters
 *     and is lowercase.</li>
 * </ul>
 * If the returned value of this method is an array, the context value will not be single by convention.
 * In any other occasion that satisfied the rules above, the context value will be single.
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextValueMethod {
	/**
	 * Returns the name of the context value.
	 * If the name, for example, is 'test', the use case will be 'context-test'
	 * @return the name of this context value
	 */
	String name();

	/**
	 * @return whether or not this context value can be used alone
	 */
	boolean standalone() default false;

	/**
	 * @return whether this happens in the present, past or future
	 */
	ContextValueState state() default ContextValueState.PRESENT;
}
