package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.Pair;
import io.github.syst3ms.skriptparser.variables.Variables;
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
    private final Class<?> type;
    private final Class<?> supertype;

    public Variable(VariableString name, boolean local, boolean list, Class<?> type) {
        this.name = name;
        this.local = local;
        this.list = list;
        this.type = type;
        this.supertype = ClassUtils.getCommonSuperclass(this.type);
    }

    private Object getRaw(TriggerContext ctx) {
        String n = name.toString(ctx);
        if (n.endsWith(Variables.LIST_SEPARATOR + "*") != list) // prevents e.g. {%expr%} where "%expr%" ends with "::*" from returning a Map
            return null;
        Object val = Variables.getVariable(n, ctx, local);
        if (val == null)
            return Variables.getVariable((local ? Variables.LOCAL_VARIABLE_TOKEN : "") + name.defaultVariableName(),
                    ctx, false);
        return val;
    }

    private Object get(TriggerContext ctx) {
        Object val = getRaw(ctx);
        if (!list)
            return val;
        if (val == null)
            return Array.newInstance(type, 0);
        List<Object> l = new ArrayList<>();
        for (Map.Entry<String, ?> v : ((Map<String, ?>) val).entrySet()) {
            if (v.getKey() != null && v.getValue() != null) {
                Object o;
                if (v.getValue() instanceof Map)
                    o = ((Map<String, ?>) v.getValue()).get(null);
                else
                    o = v.getValue();
                l.add(o);
            }
        }
        return l.toArray();
    }

    @Override
    public T[] getValues(TriggerContext ctx) {
        if(list)
            return getConvertedArray(ctx);
        T o = getConverted(ctx);
        if (o == null) {
            return (T[]) Array.newInstance(supertype, 0);
        }
        T[] one = (T[]) Array.newInstance(supertype, 1);
        one[0] = o;
        return one;
    }

    private T getConverted(TriggerContext ctx) {
        return (T) Converters.convert(get(ctx), type);
    }

    private T[] getConvertedArray(TriggerContext ctx) {
        return Converters.convertArray((Object[]) get(ctx), (Class<T>) type, (Class<T>) supertype);
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
        String n = this.name.toString(ctx);
        String name = n.substring(0, n.length() - 1);
        Object val = Variables.getVariable(name + "*", ctx, local);
        if (val == null)
            return Collections.emptyIterator();
        assert val instanceof TreeMap;
        // temporary list to prevent CMEs
        Iterator<String> keys = new ArrayList<>(((Map<String, Object>) val).keySet()).iterator();
        return new Iterator<T>() {
            @Nullable
            private String key;
            @Nullable
            private T next;

            @Override
            public boolean hasNext() {
                if (next != null)
                    return true;
                while (keys.hasNext()) {
                    key = keys.next();
                    if (key != null) {
                        next = (T) Converters.convert(Variables.getVariable(name + key, ctx, local), type);
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
                T n = next;
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
        String n = this.name.toString(ctx);
        String name = n.substring(0, n.length() - 1);
        Object val = Variables.getVariable(name + "*", ctx, local);
        if (val == null)
            return Collections.emptyIterator();
        assert val instanceof TreeMap;
        // temporary list to prevent CMEs
        Iterator<String> keys = new ArrayList<>(((Map<String, Object>) val).keySet()).iterator();
        return new Iterator<Pair<String, Object>>() {
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
                        next = Variables.getVariable(name + key, ctx, local) ;
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
                Pair<String, Object> n = new Pair<>(key, next);
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
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        if (ctx != null)
            return TypeManager.toString((Object[]) getValues(ctx));
        String name = this.name.toString(null, debug);
        return "{" + (local ? Variables.LOCAL_VARIABLE_TOKEN : "") + name.substring(1, name.length() - 1) + "}" + (debug ? "(as " + supertype.getSimpleName() + ")" : "");
    }

    public Class<? extends T> getReturnType() {
        return (Class<? extends T>) supertype;
    }

    @Nullable
    @Override
    public <C> Expression<C> convertExpression(Class<C> to) {
        return new Variable<>(name, local, list, to);
    }

    private void set(TriggerContext ctx, @Nullable Object value) {
        Variables.setVariable(name.toString(ctx), value, ctx, local);
    }

    private void setIndex(TriggerContext ctx, String index, @Nullable Object value) {
        assert list;
        String s = name.toString(ctx);
        assert s.endsWith("::*") : s + "; " + name;
        Variables.setVariable(s.substring(0, s.length() - 1) + index, value, ctx, local);
    }

    @Override
    public Class<?>[] acceptsChange(ChangeMode mode) {
        if (!list && mode == ChangeMode.SET)
            return new Class[]{Object[].class};
        return new Class[]{Object[].class};
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void change(TriggerContext ctx, Object[] changeWith, ChangeMode mode) throws UnsupportedOperationException {
        switch (mode) {
            case DELETE:
                if (list) {
                    ArrayList<String> rem = new ArrayList<>();
                    Map<String, Object> o = (Map<String, Object>) getRaw(ctx);
                    if (o == null)
                        return;
                    for (Map.Entry<String, Object> i : o.entrySet()) {
                        if (i.getKey() != null){
                            rem.add(i.getKey());
                        }
                    }
                    for (String r : rem) {
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
                    for (Object d : changeWith) {
                        if (d instanceof Object[]) {
                            for (int j = 0; j < ((Object[]) d).length; j++)
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
                Object x = getRaw(ctx);
                if (x == null)
                    return;
                for (Object o : x instanceof Map ? ((Map<?, ?>) x).values() : Collections.singletonList(x)) {
                    Class<?> c = o.getClass();
                    Type<?> type = TypeManager.getByClass(c);
                    assert type != null;
                    Changer<?> changer = type.getDefaultChanger();
                    if (changer != null && changer.acceptsChange(ChangeMode.RESET) != null) {
                        Object[] one = (Object[]) Array.newInstance(o.getClass(), 1);
                        one[0] = o;
                        ((Changer) changer).change(one, new Object[0], ChangeMode.RESET);
                    }
                }
                break;
            case ADD:
            case REMOVE:
            case REMOVE_ALL:
                assert changeWith.length > 0;
                if (list) {
                    Map<String, Object> o = (Map<String, Object>) getRaw(ctx);
                    if (mode == ChangeMode.REMOVE) {
                        if (o == null)
                            return;
                        ArrayList<String> rem = new ArrayList<>(); // prevents CMEs
                        for (Object d : changeWith) {
                            for (Map.Entry<String, Object> i : o.entrySet()) {
                                if (Relation.EQUAL.is(Comparators.compare(i.getValue(), d))) {
                                    rem.add(i.getKey());
                                    break;
                                }
                            }
                        }
                        for (String r : rem) {
                            assert r != null;
                            setIndex(ctx, r, null);
                        }
                    } else if (mode == ChangeMode.REMOVE_ALL) {
                        if (o == null)
                            return;
                        ArrayList<String> rem = new ArrayList<>(); // prevents CMEs
                        for (Map.Entry<String, Object> i : o.entrySet()) {
                            for (Object d : changeWith) {
                                if (Relation.EQUAL.is(Comparators.compare(i.getValue(), d)))
                                    rem.add(i.getKey());
                            }
                        }
                        for (String r : rem) {
                            assert r != null;
                            setIndex(ctx, r, null);
                        }
                    } else {
                        assert mode == ChangeMode.ADD;
                        int i = 1;
                        for (Object d : changeWith) {
                            if (o != null)
                                while (o.containsKey(String.valueOf(i)))
                                    i++;
                            setIndex(ctx, String.valueOf(i), d);
                            i++;
                        }
                    }
                } else {
                    Object o = get(ctx);
                    Type<?> type;
                    if (o == null) {
                        type = null;
                    } else {
                        type = TypeManager.getByClass(o.getClass());
                    }
                    Arithmetic a = null;
                    Changer<?> changer;
                    Class<?>[] cs;
                    if (o == null || type == null || (a = type.getArithmetic()) != null) {
                        boolean changed = false;
                        for (Object d : changeWith) {
                            if (o == null || type == null) {
                                type = TypeManager.getByClass(d.getClass());
                                //Mirre Start
                                if (type != null && type.getArithmetic() != null || d instanceof Number)
                                    o = d;
                                //Mirre End
                                changed = true;
                                continue;
                            }
                            assert a != null;
                            Class<?> r = a.getRelativeType();
                            Object diff = Converters.convert(d, r);
                            if (diff != null) {
                                if (mode == ChangeMode.ADD)
                                    o = a.add(o, diff);
                                else
                                    o = a.subtract(o, diff);
                                changed = true;
                            }
                        }
                        if (changed)
                            set(ctx, o);
                    } else if ((changer = type.getDefaultChanger()) != null && (cs = changer.acceptsChange(mode)) != null) {
                        Object[] one = (Object[]) Array.newInstance(o.getClass(), 1);
                        one[0] = o;
                        Class<?>[] cs2 = new Class<?>[cs.length];
                        for (int i = 0; i < cs.length; i++)
                            cs2[i] = cs[i].isArray() ? cs[i].getComponentType() : cs[i];
                        ArrayList<Object> l = new ArrayList<>();
                        for (Object d : changeWith) {
                            Object d2 = Converters.convert(d, cs2);
                            if (d2 != null)
                                l.add(d2);
                        }
                        ((Changer<Object>) changer).change(one, l.toArray(), mode);
                }
                break;
            }
        }
    }
}