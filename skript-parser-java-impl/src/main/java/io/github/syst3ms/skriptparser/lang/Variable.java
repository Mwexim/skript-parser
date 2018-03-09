package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.variables.Variables;
import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

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

    public Object getRaw(Event e) {
        String n = name.toString(e);
        if (n.endsWith(Variables.LIST_SEPARATOR + "*") != list) // prevents e.g. {%expr%} where "%expr%" ends with "::*" from returning a Map
            return null;
        Object val = Variables.getVariable(n, e, local);
        if (val == null)
            return Variables.getVariable((local ? Variables.LOCAL_VARIABLE_TOKEN : "") + name.defaultVariableName(), e, false);
        return val;
    }

    private Object get(Event e) {
        Object val = getRaw(e);
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
    public T[] getValues(Event e) {
		if(list)
			return getConvertedArray(e);
		T o = getConverted(e);
		if (o == null) {
			T[] r = (T[]) Array.newInstance(supertype, 0);
			assert r != null;
			return r;
		}
		T[] one = (T[]) Array.newInstance(supertype, 1);
		one[0] = o;
		return one;
    }

    private T getConverted(Event e) {
        assert !list;
        return (T) Converters.convert(get(e), type);
    }

    private T[] getConvertedArray(Event e) {
        assert list;
        return Converters.convertArray((Object[]) get(e), (Class<T>) type, (Class<T>) supertype);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
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

    public Iterator<T> iterator(Event e) {
        if (!list)
            throw new SkriptRuntimeException("");
        String n = this.name.toString(e);
        String name = n.substring(0, n.length() - 1);
        Object val = Variables.getVariable(name + "*", e, local);
        if (val == null)
            return Collections.emptyIterator();
        assert val instanceof TreeMap;
        // temporary list to prevent CMEs
        @SuppressWarnings("unchecked")
        Iterator<String> keys = new ArrayList<>(((Map<String, Object>) val).keySet()).iterator();
        return new Iterator<T>() {
            private String key;
            private T next;

            @SuppressWarnings({"unchecked"})
            @Override
            public boolean hasNext() {
                if (next != null)
                    return true;
                while (keys.hasNext()) {
                    key = keys.next();
                    if (key != null) {
                        next = (T) Converters.convert(Variables.getVariable(name + key, e, local), type);
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

    public Iterator<Pair<String, Object>> variablesIterator(Event e) {
        if (!list)
            throw new SkriptRuntimeException("Looping a non-list variable");
        String n = this.name.toString(e);
        String name = n.substring(0, n.length() - 1);
        Object val = Variables.getVariable(name + "*", e, local);
        if (val == null)
            return Collections.emptyIterator();
        assert val instanceof TreeMap;
        // temporary list to prevent CMEs
        @SuppressWarnings("unchecked")
        Iterator<String> keys = new ArrayList<>(((Map<String, Object>) val).keySet()).iterator();
        return new Iterator<Pair<String, Object>>() {
            private String key;
            private Object next;

            @Override
            public boolean hasNext() {
                if (next != null)
                    return true;
                while (keys.hasNext()) {
                    key = keys.next();
                    if (key != null) {
                        next = Variables.getVariable(name + key, e, local) ;
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
    public String toString(Event e, boolean debug) {
        if (e != null)
            return TypeManager.toString((Object[]) getValues(e));
        String name = this.name.toString(null, debug);
        return "{" + (local ? Variables.LOCAL_VARIABLE_TOKEN : "") + name.substring(1, name.length() - 1) + "}" + (debug ? "(as " + supertype.getSimpleName() + ")" : "");
    }

    public Class<? extends T> getReturnType() {
        return (Class<? extends T>) supertype;
    }

    private void set(Event e, Object value) {
        Variables.setVariable("" + name.toString(e), value, e, local);
    }

    private void setIndex(Event e, String index, Object value) {
        assert list;
        String s = name.toString(e);
        assert s.endsWith("::*") : s + "; " + name;
        Variables.setVariable(s.substring(0, s.length() - 1) + index, value, e, local);
    }

    @Override
    public Class<?>[] acceptsChange(ChangeMode mode) {
        if (!list && mode == ChangeMode.SET)
            return new Class[]{Object[].class};
        return new Class[]{Object[].class};
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void change(Event e, Object[] changeWith, ChangeMode mode) throws UnsupportedOperationException {
        switch (mode) {
            case DELETE:
                if (list) {
                    ArrayList<String> rem = new ArrayList<>();
                    Map<String, Object> o = (Map<String, Object>) getRaw(e);
                    if (o == null)
                        return;
                    for (Map.Entry<String, Object> i : o.entrySet()) {
                        if (i.getKey() != null){
                            rem.add(i.getKey());
                        }
                    }
                    for (String r : rem) {
                        assert r != null;
                        setIndex(e, r, null);
                    }
                }

                set(e, null);
                break;
            case SET:
                assert changeWith != null;
                if (list) {
                    set(e, null);
                    int i = 1;
                    for (Object d : changeWith) {
                        if (d instanceof Object[]) {
                            for (int j = 0; j < ((Object[]) d).length; j++)
                                setIndex(e, "" + i + Variables.LIST_SEPARATOR + j, ((Object[]) d)[j]);
                        } else {
                            setIndex(e, "" + i, d);
                        }
                        i++;
                    }
                } else {
                    set(e, changeWith[0]);
                }
                break;
            case RESET:
                Object x = getRaw(e);
                if (x == null)
                    return;
                for (Object o : x instanceof Map ? ((Map<?, ?>) x).values() : Collections.singletonList(x)) {
                    Class<?> c = o.getClass();
                    Type<?> type = TypeManager.getByClass(c);
                    Changer<?> changer = type.getDefaultChanger();
                    if (changer != null && changer.acceptsChange(ChangeMode.RESET) != null) {
                        Object[] one = (Object[]) Array.newInstance(o.getClass(), 1);
                        one[0] = o;
                        ((Changer) changer).change(one, null, ChangeMode.RESET);
                    }
                }
                break;
            case ADD:
            case REMOVE:
            case REMOVE_ALL:
                assert changeWith != null;
                if (list) {
                    Map<String, Object> o = (Map<String, Object>) getRaw(e);
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
                            setIndex(e, r, null);
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
                            setIndex(e, r, null);
                        }
                    } else {
                        assert mode == ChangeMode.ADD;
                        int i = 1;
                        for (Object d : changeWith) {
                            if (o != null)
                                while (o.containsKey("" + i))
                                    i++;
                            setIndex(e, "" + i, d);
                            i++;
                        }
                    }
                } else {
                    Object o = get(e);
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
                                if (type.getArithmetic() != null || d instanceof Number)
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
                            set(e, o);
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
    // TODO uncomment
}