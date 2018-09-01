package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.PatternParser;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.MultiMap;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A mutable object keeping track of everything registered by any source.
 * Do not forget to call {@link #register()} !
 *
 * @see #getRegisterer()
 */
public class SkriptRegistration {
    private SkriptAddon registerer;
    private MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
    private List<SyntaxInfo<? extends Effect>> effects = new ArrayList<>();
    private List<SyntaxInfo<? extends CodeSection>> sections = new ArrayList<>();
    private List<SkriptEventInfo<?>> events = new ArrayList<>();
    private List<Type<?>> types = new ArrayList<>();
    private List<Converters.ConverterInfo<?, ?>> converters = new ArrayList<>();
    private PatternParser patternParser;

    public SkriptRegistration(SkriptAddon registerer) {
        this.registerer = registerer;
        this.patternParser = new PatternParser();
    }

    public List<SkriptEventInfo<?>> getEvents() {
        return events;
    }

    public List<SyntaxInfo<? extends CodeSection>> getSections() {
        return sections;
    }

    public List<Type<?>> getTypes() {
        return types;
    }

    public MultiMap<Class<?>, ExpressionInfo<?, ?>> getExpressions() {
        return expressions;
    }

    public List<SyntaxInfo<? extends Effect>> getEffects() {
        return effects;
    }

    /**
     * @return the name of what is registering everything
     */
    public SkriptAddon getRegisterer() {
        return registerer;
    }

    public List<Converters.ConverterInfo<?, ?>> getConverters() {
        return converters;
    }

    public <C extends Expression<T>, T> ExpressionRegistrar<C, T> newExpression(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
        return new ExpressionRegistrar<>(c, returnType, isSingle, patterns);
    }

    public <C extends Expression<T>, T> void addExpression(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
        new ExpressionRegistrar<>(c, returnType, isSingle).addPatterns(patterns)
                .register();
    }

    public <C extends Expression<T>, T> void addExpression(Class<C> c, Class<T> returnType, boolean isSingle, int priority, String... patterns) {
        new ExpressionRegistrar<>(c, returnType, isSingle).addPatterns(patterns)
                .setPriority(priority)
                .register();
    }

    public <C extends Effect> EffectRegistrar<C> newEffect(Class<C> c, String... patterns) {
        return new EffectRegistrar<>(c, patterns);
    }

    public <C extends Effect> void addEffect(Class<C> c, String... patterns) {
        new EffectRegistrar<>(c, patterns).register();
    }

    public <C extends Effect> void addEffect(Class<C> c, int priority, String... patterns) {
        new EffectRegistrar<>(c, patterns).setPriority(priority)
                .register();
    }

    public void addSection(Class<? extends CodeSection> c, String... patterns) {
        new SectionRegistrar<>(c, patterns).register();
    }

    public void addSection(Class<? extends CodeSection> c, int priority, String... patterns) {
        new SectionRegistrar<>(c, patterns).setPriority(priority).register();
    }

    public void addEvent(Class<? extends SkriptEvent> c, Class<? extends TriggerContext>[] handledContexts, String... patterns) {
        new EventRegistrar<>(c, patterns).setHandledContexts(handledContexts).register();
    }

    public void addEvent(Class<? extends SkriptEvent> c, Class<? extends TriggerContext>[] handledContexts, int priority, String... patterns) {
        new EventRegistrar<>(c, patterns).setHandledContexts(handledContexts).setPriority(priority).register();
    }

    public <E extends SkriptEvent> EventRegistrar<E> newEvent(Class<E> c, String... patterns) {
        return new EventRegistrar<>(c, patterns);
    }

    public <T> void addType(Class<T> c, String name, String pattern) {
        new TypeRegistrar<>(c, name, pattern).register();
    }

    public <T> TypeRegistrar<T> newType(Class<T> c, String name, String pattern) {
        return new TypeRegistrar<>(c, name, pattern);
    }

    public <F, T> void addConverter(Class<F> from, Class<T> to, Function<? super F, ? extends T> converter) {
        converters.add(new Converters.ConverterInfo<>(from, to, converter));
    }

    public <F, T> void addConverter(Class<F> from, Class<T> to, Function<? super F, ? extends T> converter, int options) {
        converters.add(new Converters.ConverterInfo<>(from, to, converter, options));
    }

    public void register() {
        SyntaxManager.register(this);
        TypeManager.register(this);
        Converters.registerConverters(this);
        Converters.createMissingConverters();
    }

    public interface Registrar {
        void register();
    }

    public class TypeRegistrar<C> implements Registrar {
        private final Class<C> c;
        private final String baseName;
        private final String pattern;
        private Function<? super C, String> toStringFunction = o -> Objects.toString(o, TypeManager.NULL_REPRESENTATION);
        @Nullable
        private Function<String, ? extends C> literalParser;
        @Nullable
        private Changer<? super C> defaultChanger;
        @Nullable
        private Arithmetic<C, ?> arithmetic;

        public TypeRegistrar(Class<C> c, String baseName, String pattern) {
            this.c = c;
            this.baseName = baseName;
            this.pattern = pattern;
        }

        public TypeRegistrar<C> literalParser(Function<String, ? extends C> literalParser) {
            this.literalParser = literalParser;
            return this;
        }

        public TypeRegistrar<C> toStringFunction(Function<? super C, String> toStringFunction) {
            this.toStringFunction = toStringFunction;
            return this;
        }

        public TypeRegistrar<C> defaultChanger(Changer<? super C> defaultChanger) {
            this.defaultChanger = defaultChanger;
            return this;
        }

        public <R> TypeRegistrar<C> arithmetic(Arithmetic<C, R> arithmetic) {
            this.arithmetic = arithmetic;
            return this;
        }

        @Override
        public void register() {
            types.add(new Type<>(c, baseName, pattern, literalParser, toStringFunction, defaultChanger, arithmetic));
        }
    }

    public abstract class SyntaxRegistrar<C extends SyntaxElement> implements Registrar {
        private final Class<C> c;
        private List<String> patterns = new ArrayList<>();
        private int priority = 5;

        SyntaxRegistrar(Class<C> c, String... patterns) {
            this(c, 5, patterns);
        }

        SyntaxRegistrar(Class<C> c, int priority, String... patterns) {
            this.c = c;
            this.priority = priority;
            Collections.addAll(this.patterns, patterns);
        }

        public SyntaxRegistrar<C> addPatterns(String... patterns) {
            Collections.addAll(this.patterns, patterns);
            return this;
        }

        public SyntaxRegistrar<C> setPriority(int priority) {
            if (priority < 0)
                throw new SkriptParserException("Can't have a negative priority !");
            this.priority = priority;
            return this;
        }
    }

    public class ExpressionRegistrar<C extends Expression<? extends T>, T> extends SyntaxRegistrar<C> {
        private final Class<T> returnType;
        private final boolean isSingle;

        ExpressionRegistrar(Class<C> c, Class<T> returnType, boolean isSingle) {
            this(c, returnType, isSingle, new String[0]);
        }

        ExpressionRegistrar(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
            super(c, patterns);
            this.returnType = returnType;
            this.isSingle = isSingle;
        }

        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : super.patterns) {
                elements.add(patternParser.parsePattern(StringUtils.fixEncoding(s)));
            }
            Type<T> type = TypeManager.getByClassExact(returnType);
            if (type == null) {
                throw new SkriptParserException("Couldn't figure out the return type corresponding to " + returnType.getName());
            }
            ExpressionInfo<C, T> info = new ExpressionInfo<>(super.c, elements, registerer, type, isSingle, super.priority);
            expressions.putOne(super.c, info);
        }
    }

    public class EffectRegistrar<C extends Effect> extends SyntaxRegistrar<C> {

        EffectRegistrar(Class<C> c, String... patterns) {
            super(c, patterns);
        }

        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : super.patterns) {
                elements.add(patternParser.parsePattern(StringUtils.fixEncoding(s)));
            }
            SyntaxInfo<C> info = new SyntaxInfo<>(super.c, elements, super.priority, registerer);
            effects.add(info);
        }
    }

    public class SectionRegistrar<C extends CodeSection> extends SyntaxRegistrar<C> {

        SectionRegistrar(Class<C> c, String... patterns) {
            super(c, patterns);
        }

        @Override
        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : super.patterns) {
                elements.add(patternParser.parsePattern(StringUtils.fixEncoding(s)));
            }
            SyntaxInfo<C> info = new SyntaxInfo<>(super.c, elements, super.priority, registerer);
            sections.add(info);
        }
    }

    public class EventRegistrar<T extends SkriptEvent> extends SyntaxRegistrar<T> {
        private Class<? extends TriggerContext>[] handledContexts;

        EventRegistrar(Class<T> c, String... patterns) {
            super(c, patterns);
        }

        @Override
        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : super.patterns) {
                if (s.startsWith("*")) {
                    s = s.substring(1,s.length());
                } else {
                    s = "[on] " + s;
                }
                elements.add(patternParser.parsePattern(StringUtils.fixEncoding(s)));
            }
            SkriptEventInfo<T> info = new SkriptEventInfo<>(super.c, handledContexts, elements, super.priority, registerer);
            events.add(info);
        }

        @SafeVarargs
        public final EventRegistrar<T> setHandledContexts(Class<? extends TriggerContext>... contexts) {
            this.handledContexts = contexts;
            return this;
        }
    }
}
