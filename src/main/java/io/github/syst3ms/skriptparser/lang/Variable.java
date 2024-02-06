package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.expressions.arithmetic.Arithmetics;
import io.github.syst3ms.skriptparser.expressions.arithmetic.OperationInfo;
import io.github.syst3ms.skriptparser.expressions.arithmetic.Operator;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.Pair;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A reference to a variable, whose value is only known at runtime. It can be local to the event, meaning it isn't
 * defined outside of the event it was first defined in. It can also be a list of multiple values. It can also be both.
 * @param <T> the common supertype of the possibly multiple values of the variable
 */
@SuppressWarnings("unchecked")
public class Variable<T> implements Expression<T> {
    private final VariableString name;
    private final boolean local;
    private final boolean list;
    private Class<?> type;
    private Class<?> supertype;

    public Variable(VariableString name, boolean local, boolean list, Class<?> type) {
        this.name = name;
        this.local = local;
        this.list = list;
        this.type = type;
        this.supertype = ClassUtils.getCommonSuperclass(this.type);
    }

    @Override
    @Contract("_, _, _ -> fail")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T[] getValues(TriggerContext ctx) {
        if (list)
            return getConvertedArray(ctx);
        var o = getConverted(ctx);
        if (o.isEmpty()) {
            return (T[]) Array.newInstance(supertype, 0);
        }
        var one = (T[]) Array.newInstance(supertype, 1);
        one[0] = o.get();
        return one;
    }

    /**
     * @param ctx the event
     * @return the index of this Variable
     */
    public String getIndex(TriggerContext ctx) {
        return name.toString(ctx);
    }

    /**
     * Returns the raw value stored inside the variable map. This can either be the object
     * this Variable is referencing to or some sort of Map containing nested nodes.
     * @param ctx the event
     * @return the raw value
     */
    public Optional<Object> getRaw(TriggerContext ctx) {
        var n = name.toString(ctx);
        if (n.endsWith(Variables.LIST_SEPARATOR + "*") != list) // prevents e.g. {%expr%} where "%expr%" ends with "::*" from returning a Map
            return Optional.empty();
        return Variables.getVariable(n, ctx, local)
                .or(() -> Variables.getVariable(
                        (local ? Variables.LOCAL_VARIABLE_TOKEN : "") + name.defaultVariableName(),
                        ctx,
                        false
                ));
    }

    private Optional<? extends T> getConverted(TriggerContext ctx) {
        return (Optional<? extends T>) Converters.convert(get(ctx).orElse(null), type);
    }

    @SuppressWarnings("ConstantConditions")
    private T[] getConvertedArray(TriggerContext ctx) {
        return Converters.convertArray((Object[]) get(ctx).orElse(null), (Class<T>) type, (Class<T>) supertype);
    }

    private Optional<Object> get(TriggerContext ctx) {
        var val = getRaw(ctx);
        if (!list)
            return val;
        if (val.isEmpty())
            return Optional.of(Array.newInstance(type, 0));
        var list = new ArrayList<>();
        for (Map.Entry<String, ?> v : ((Map<String, ?>) val.get()).entrySet()) {
            if (v.getKey() != null && v.getValue() != null) {
                Object o;
                if (v.getValue() instanceof Map) {
                    o = ((Map<String, ?>) v.getValue()).get(null);
                } else {
                    o = v.getValue();
                }
                list.add(o);
            }
        }
        return Optional.ofNullable(list.toArray());
    }

    @Override
    public boolean isSingle() {
        return !list;
    }

    public Class<? extends T> getReturnType() {
        return (Class<? extends T>) supertype;
    }

    public void setReturnType(Class<?> returnType) {
        type = returnType;
        supertype = ClassUtils.getCommonSuperclass(returnType);
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(ChangeMode mode) {
        return Optional.of(new Class[] {list ? Object[].class : Object.class});
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void change(TriggerContext ctx, ChangeMode mode, Object[] changeWith) throws UnsupportedOperationException {
        switch (mode) {
            case DELETE:
                if (list) {
                    var rem = new ArrayList<String>();
                    var o = (Map<String, Object>) getRaw(ctx).orElse(null);
                    if (o == null)
                        return;
                    for (var i : o.entrySet()) {
                        if (i.getKey() != null){
                            rem.add(i.getKey());
                        }
                    }
                    for (var r : rem) {
                        assert r != null;
                        setIndex(ctx, r, null);
                    }
                }
                set(ctx, null);
                break;
            case SET:
                assert changeWith.length > 0;
                if (list) {
                    set(ctx, null);
                    int i = 1;
                    for (var d : changeWith) {
                        if (d instanceof Object[]) {
                            assert false;
                            for (var j = 0; j < ((Object[]) d).length; j++)
                                setIndex(ctx, i + Variables.LIST_SEPARATOR + j, ((Object[]) d)[j]);
                        } else {
                            setIndex(ctx, String.valueOf(i), d);
                        }
                        i++;
                    }
                } else {
                    set(ctx, changeWith[0]);
                }
                break;
            case RESET:
                Optional<? extends Collection<?>> x = getRaw(ctx).map(r -> r instanceof Map
                        ? ((Map<?, ?>) r).values()
                        : Collections.singletonList(r)
                );
                if (x.isEmpty())
                    return;
                for (Object o : x.get()) {
                    var c = o.getClass();
                    var type = TypeManager.getByClass(c);
                    assert type.isPresent();
                    var changer = type.get().getDefaultChanger();
                    if (changer.map(ch -> ch.acceptsChange(ChangeMode.RESET)).isPresent()) {
                        var one = (Object[]) Array.newInstance(o.getClass(), 1);
                        one[0] = o;
                        changer.ifPresent(ch -> ((Changer<Object>) ch).change(one, new Object[0], ChangeMode.RESET));
                    }
                }
                break;
            case ADD:
            case REMOVE:
            case REMOVE_ALL:
                assert changeWith.length > 0;
                if (list) {
                    Optional<? extends Map<String, Object>> o = getRaw(ctx).map(r -> (Map<String, Object>) r);
                    if (mode == ChangeMode.REMOVE) {
                        if (o.isEmpty())
                            return;
                        var rem = new ArrayList<String>(); // prevents CMEs
                        for (var d : changeWith) {
                            for (var i : o.get().entrySet()) {
                                if (Relation.EQUAL.is(Comparators.compare(i.getValue(), d))) {
                                    rem.add(i.getKey());
                                    break;
                                }
                            }
                        }
                        for (var r : rem) {
                            assert r != null;
                            setIndex(ctx, r, null);
                        }
                    } else if (mode == ChangeMode.REMOVE_ALL) {
                        if (o.isEmpty())
                            return;
                        var rem = new ArrayList<String>(); // prevents CMEs
                        for (var i : o.get().entrySet()) {
                            for (var d : changeWith) {
                                if (Relation.EQUAL.is(Comparators.compare(i.getValue(), d)))
                                    rem.add(i.getKey());
                            }
                        }
                        for (var r : rem) {
                            assert r != null;
                            setIndex(ctx, r, null);
                        }
                    } else {
                        assert mode == ChangeMode.ADD;
                        var i = 1;
                        for (var d : changeWith) {
                            if (o.isPresent())
                                while (o.get().containsKey(String.valueOf(i)))
                                    i++;
                            setIndex(ctx, String.valueOf(i), d);
                            i++;
                        }
                    }
                } else {
                    Object originalValue = get(ctx).orElse(null);
                    Class<?> clazz = originalValue == null ? null : originalValue.getClass();
                    Operator operator = mode == ChangeMode.ADD ? Operator.ADDITION : Operator.SUBTRACTION;
                    Changer<?> changer;
                    Class<?>[] classes;
                    if (clazz == null || !Arithmetics.getOperations(operator, clazz).isEmpty()) {
                        boolean changed = false;
                        for (Object newValue : changeWith) {
                            OperationInfo info = Arithmetics.getOperationInfo(operator, clazz != null ? (Class) clazz : newValue.getClass(), newValue.getClass());
                            if (info == null)
                                continue;

                            Object value = originalValue == null ? Arithmetics.getDefaultValue(info.getLeft()) : originalValue;
                            if (value == null)
                                continue;

                            originalValue = info.getOperation().calculate(value, newValue);
                            changed = true;
                        }
                        if (changed)
                            set(ctx, originalValue);
                    } else if ((changer = TypeManager.getByClass(clazz).flatMap(Type::getDefaultChanger).orElse(null)) != null && (classes = changer.acceptsChange(mode)) != null) {
                        Object[] originalValueArray = (Object[]) Array.newInstance(originalValue.getClass(), 1);
                        originalValueArray[0] = originalValue;

                        Class<?>[] classes2 = new Class<?>[classes.length];
                        for (int i = 0; i < classes.length; i++)
                            classes2[i] = classes[i].isArray() ? classes[i].getComponentType() : classes[i];

                        ArrayList<Object> convertedDelta = new ArrayList<>();
                        for (Object value : changeWith) {
                            Object convertedValue = Converters.convert(value, classes2);
                            if (convertedValue != null)
                                convertedDelta.add(convertedValue);
                        }

                        change(changer, originalValueArray, convertedDelta.toArray(), mode);

                    }
                    break;
                }
        }
    }

    private static <T> void change(final Changer<T> changer, final Object[] what, final @Nullable Object[] delta, final ChangeMode mode) {
        changer.change((T[]) what, delta, mode);
    }

    public Iterator<T> iterator(TriggerContext ctx) {
        if (!list)
            throw new SkriptRuntimeException("");
        var n = this.name.toString(ctx);
        var name = n.substring(0, n.length() - 1);
        var val = Variables.getVariable(name + "*", ctx, local);
        if (val.isEmpty())
            return Collections.emptyIterator();
        assert val.get() instanceof TreeMap;
        // Temporary list to prevent CMEs
        var keys = new ArrayList<>(((Map<String, Object>) val.get()).keySet()).iterator();
        return new Iterator<>() {
            @Nullable
            private T next;

            @Override
            public boolean hasNext() {
                if (next != null)
                    return true;
                while (keys.hasNext()) {
                    @Nullable String key = keys.next();
                    if (key != null) {
                        next = (T) Converters.convert(Variables.getVariable(name + key, ctx, local), type).orElse(null);
                        if (next != null && !(next instanceof TreeMap))
                            return true;
                    }
                }
                next = null;
                return false;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                var n = next;
                assert n != null;
                next = null;
                return n;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @param ctx the event
     * @return an {@link Iterator} that iterates over pairs of indexes and values
     */
    public Iterator<Pair<String, Object>> variablesIterator(TriggerContext ctx) {
        if (!list)
            throw new SkriptRuntimeException("Looping a non-list variable");
        var n = name.toString(ctx);
        var name = n.substring(0, n.length() - 1);
        var val = Variables.getVariable(name + "*", ctx, local);
        if (val.isEmpty())
            return Collections.emptyIterator();
        assert val.get() instanceof Map;
        // Temporary list to prevent CMEs
        @SuppressWarnings("unchecked")
        var keys = new ArrayList<>(((Map<String, Object>) val.get()).keySet()).iterator();
        return new Iterator<>() {
            @Nullable
            private String key;
            @Nullable
            private Object next;

            @Override
            public boolean hasNext() {
                if (next != null)
                    return true;
                while (keys.hasNext()) {
                    key = keys.next();
                    if (key != null) {
                        next = Variables.getVariable(name + key, ctx, local).orElse(null);
                        if (next != null && !(next instanceof TreeMap))
                            return true;
                    }
                }
                next = null;
                return false;
            }

            @Override
            public Pair<String, Object> next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                assert next != null && key != null;
                var n = new Pair<>(key, next);
                next = null;
                return n;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public <C> Optional<? extends Expression<C>> convertExpression(Class<C> to) {
        return Optional.of(new Variable<>(name, local, list, to));
    }

    @Override
    public boolean isLoopOf(String s) {
        return s.equalsIgnoreCase("var") || s.equalsIgnoreCase("variable") || s.equalsIgnoreCase("value") || s.equalsIgnoreCase("index");
    }

    public boolean isIndexLoop(String s) {
        return s.equalsIgnoreCase("index");
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return TypeManager.toString(getValues(ctx));
    }

    private void set(TriggerContext ctx, @Nullable Object value) {
        Variables.setVariable(name.toString(ctx), value, ctx, local);
    }

    private void setIndex(TriggerContext ctx, String index, @Nullable Object value) {
        assert list;
        var s = name.toString(ctx);
        assert s.endsWith("::*") : s + "; " + name;
        Variables.setVariable(s.substring(0, s.length() - 1) + index, value, ctx, local);
    }
}