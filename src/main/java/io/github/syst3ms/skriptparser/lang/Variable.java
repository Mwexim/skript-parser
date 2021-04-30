package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.classes.Pair;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;

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
    private final Class<?> type;
    private final Class<?> supertype;

    public Variable(VariableString name, boolean local, boolean list, Class<?> type) {
        this.name = name;
        this.local = local;
        this.list = list;
        this.type = type;
        this.supertype = ClassUtils.getCommonSuperclass(this.type);
    }

    public String getIndex(TriggerContext ctx) {
        return name.toString(ctx);
    }

    public Optional<Object> getRaw(TriggerContext ctx) {
        var n = name.toString(ctx);
        if (n.endsWith(Variables.LIST_SEPARATOR + "*") != list) // prevents e.g. {%expr%} where "%expr%" ends with "::*" from returning a Map
            return Optional.empty();
        return Variables.getVariable(n, ctx, local)
                .or(() -> Variables.getVariable(
                        (local ? Variables.LOCAL_VARIABLE_TOKEN : "") + name.defaultVariableName(),
                        ctx,
                        false
                    )
                );
    }

    private Optional<Object> get(TriggerContext ctx) {
        var val = getRaw(ctx);
        if (!list)
            return val;
        if (val.isEmpty())
            return Optional.of(Array.newInstance(type, 0));
        List<Object> l = new ArrayList<>();
        for (Map.Entry<String, ?> v : ((Map<String, ?>) val.get()).entrySet()) {
            if (v.getKey() != null && v.getValue() != null) {
                Object o;
                if (v.getValue() instanceof Map)
                    o = ((Map<String, ?>) v.getValue()).get(null);
                else
                    o = v.getValue();
                l.add(o);
            }
        }
        return Optional.ofNullable(l.toArray());
    }

    @Override
    public T[] getValues(TriggerContext ctx) {
        if(list)
            return getConvertedArray(ctx);
        Optional<? extends T> o = getConverted(ctx);
        if (o.isEmpty()) {
            return (T[]) Array.newInstance(supertype, 0);
        }
        var one = (T[]) Array.newInstance(supertype, 1);
        one[0] = o.get();
        return one;
    }

    private Optional<? extends T> getConverted(TriggerContext ctx) {
        return (Optional<? extends T>) Converters.convert(get(ctx).orElse(null), type);
    }

    @SuppressWarnings("ConstantConditions")
    private T[] getConvertedArray(TriggerContext ctx) {
        return Converters.convertArray((Object[]) get(ctx).orElse(null), (Class<T>) type, (Class<T>) supertype);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSingle() {
        return !list;
    }

    @Override
    public boolean isLoopOf(String s) {
        return s.equalsIgnoreCase("var") || s.equalsIgnoreCase("variable") || s.equalsIgnoreCase("value") || s.equalsIgnoreCase("index");
    }

    public boolean isIndexLoop(String s) {
        return s.equalsIgnoreCase("index");
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
    public String toString(TriggerContext ctx, boolean debug) {
        return TypeManager.toString((Object[]) getValues(ctx));
    }

    public Class<? extends T> getReturnType() {
        return (Class<? extends T>) supertype;
    }

    @Override
    public <C> Optional<? extends Expression<C>> convertExpression(Class<C> to) {
        return Optional.of(new Variable<>(name, local, list, to));
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

    @Override
    public Optional<Class<?>[]> acceptsChange(ChangeMode mode) {
        return Optional.of(new Class[]{Object[].class});
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void change(TriggerContext ctx, Object[] changeWith, ChangeMode mode) throws UnsupportedOperationException {
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
                    var i = 1;
                    for (var d : changeWith) {
                        if (d instanceof Object[]) {
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
                        changer.ifPresent(ch -> ((Changer) ch).change(one, new Object[0], ChangeMode.RESET));
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
                    Optional<Object> o = get(ctx);
                    var type = o.flatMap(ob -> (Optional<? extends Type<?>>) TypeManager.getByClass(ob.getClass()));
                    Optional<? extends Arithmetic> a = Optional.empty();
                    Optional<? extends Changer<?>> changer;
                    Class<?>[] cs;
                    if (o.isEmpty() || type.isEmpty() || (a = type.get().getArithmetic()).isPresent()) {
                        var changed = false;
                        for (var d : changeWith) {
                            if (o.isEmpty() || type.isEmpty()) {
                                type = TypeManager.getByClass(d.getClass());
                                if (type.filter(t -> t.getArithmetic().isPresent()).isPresent())
                                    o = Optional.of(d);
                                if (d instanceof Number) {
                                    o = mode == ChangeMode.REMOVE
                                            ? Optional.of(NumberMath.negate((Number) d))
                                            : Optional.of(d);
                                }
                                changed = true;
                                continue;
                            }
                            assert a.isPresent();
                            Class<?> r = a.get().getRelativeType();
                            var diff = Converters.convert(d, r);
                            if (diff.isPresent()) {
                                if (mode == ChangeMode.ADD) {
                                    o = Optional.ofNullable(a.get().add(o.orElse(null), diff.get()));
                                } else {
                                    o = Optional.ofNullable(a.get().subtract(o.orElse(null), diff.get()));
                                }
                                changed = true;
                            }
                        }
                        if (changed)
                            set(ctx, o.orElse(null));
                    } else if ((changer = type.get().getDefaultChanger()).isPresent() && (cs = changer.get().acceptsChange(mode)) != null) {
                        var one = (Object[]) Array.newInstance(o.get().getClass(), 1);
                        one[0] = o.get();
                        var cs2 = new Class<?>[cs.length];
                        for (var i = 0; i < cs.length; i++)
                            cs2[i] = cs[i].isArray() ? cs[i].getComponentType() : cs[i];
                        var l = new ArrayList<>();
                        for (var d : changeWith) {
                            Object d2 = Converters.convert(d, cs2);
                            if (d2 != null)
                                l.add(d2);
                        }
                        ((Changer<Object>) changer.get()).change(one, l.toArray(), mode);
                    }
                    break;
            }
        }
    }
}