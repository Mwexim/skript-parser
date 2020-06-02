package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.pattern.PatternParser;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.conversions.ConverterInfo;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.MultiMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A mutable object keeping track of all syntax and types registered by an {@link SkriptAddon addon}
 * Do not forget to call {@link #register()} !
 *
 * @see #getRegisterer()
 */
public class SkriptRegistration {
    private final SkriptAddon registerer;
    private final PatternParser patternParser;
    private final SkriptLogger logger = new SkriptLogger();
    private final MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
    private final List<SyntaxInfo<? extends Effect>> effects = new ArrayList<>();
    private final List<SyntaxInfo<? extends CodeSection>> sections = new ArrayList<>();
    private final List<SkriptEventInfo<?>> events = new ArrayList<>();
    private final List<Type<?>> types = new ArrayList<>();
    private final List<ConverterInfo<?, ?>> converters = new ArrayList<>();
    private boolean newTypes = false;

    public SkriptRegistration(SkriptAddon registerer) {
        this.registerer = registerer;
        this.patternParser = new PatternParser();
    }

    /**
     * @return all currently registered events
     */
    public List<SkriptEventInfo<?>> getEvents() {
        return events;
    }

    /**
     * @return all currently registered sections
     */
    public List<SyntaxInfo<? extends CodeSection>> getSections() {
        return sections;
    }

    /**
     * @return all currently registered types
     */
    public List<Type<?>> getTypes() {
        return types;
    }

    /**
     * @return all currently registered expressions
     */
    public MultiMap<Class<?>, ExpressionInfo<?, ?>> getExpressions() {
        return expressions;
    }

    /**
     * @return all currently registered effects
     */
    public List<SyntaxInfo<? extends Effect>> getEffects() {
        return effects;
    }

    /**
     * @return the addon handling this registration (may be Skript itself)
     */
    public SkriptAddon getRegisterer() {
        return registerer;
    }

    /**
     * @return all currently registered converters
     */
    public List<ConverterInfo<?, ?>> getConverters() {
        return converters;
    }

    /**
     * Starts a registration process for an {@link Expression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param isSingle whether the Expression is a single value
     * @param patterns the Expression's patterns
     * @param <C> the Expression
     * @param <T> the Expression's return type
     * @return an {@link ExpressionRegistrar} to continue the registration process
     */
    public <C extends Expression<T>, T> ExpressionRegistrar<C, T> newExpression(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
        return new ExpressionRegistrar<>(c, returnType, isSingle, patterns);
    }

    /**
     * Registers an {@link Expression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param isSingle whether the Expression is a single value
     * @param patterns the Expression's patterns
     * @param <C> the Expression
     * @param <T> the Expression's return type
     */
    public <C extends Expression<T>, T> void addExpression(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
        new ExpressionRegistrar<>(c, returnType, isSingle).addPatterns(patterns)
                .register();
    }

    /**
     * Registers an {@link Expression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param isSingle whether the Expression is a single value
     * @param priority the parsing priority this Expression has. 5 by default, a lower number means lower priority
     * @param patterns the Expression's patterns
     * @param <C> the Expression
     * @param <T> the Expression's return type
     */
    public <C extends Expression<T>, T> void addExpression(Class<C> c, Class<T> returnType, boolean isSingle, int priority, String... patterns) {
        new ExpressionRegistrar<>(c, returnType, isSingle).addPatterns(patterns)
                .setPriority(priority)
                .register();
    }

    /**
     * Starts a registration process for an {@link Effect}
     * @param c the Effect's class
     * @param patterns the Effect's patterns
     * @param <C> the Effect
     * @return an {@link EffectRegistrar}
     */
    public <C extends Effect> EffectRegistrar<C> newEffect(Class<C> c, String... patterns) {
        return new EffectRegistrar<>(c, patterns);
    }

    /**
     * Registers an {@link Effect}
     * @param c the Effect's class
     * @param patterns the Effect's patterns
     * @param <C> the Effect
     */
    public <C extends Effect> void addEffect(Class<C> c, String... patterns) {
        new EffectRegistrar<>(c, patterns).register();
    }

    /**
     * Registers an {@link Effect}
     * @param c the Effect's class
     * @param priority the parsing priority this Effect has. 5 by default, a lower number means lower priority
     * @param patterns the Effect's patterns
     * @param <C> the Effect
     */
    public <C extends Effect> void addEffect(Class<C> c, int priority, String... patterns) {
        new EffectRegistrar<>(c, patterns).setPriority(priority)
                .register();
    }

    /**
     * Registers a {@link CodeSection}
     * @param c the CodeSection's class
     * @param patterns the CodeSection's patterns
     */
    public void addSection(Class<? extends CodeSection> c, String... patterns) {
        new SectionRegistrar<>(c, patterns).register();
    }

    /**
     * Registers a {@link CodeSection}
     * @param c the CodeSection's class
     * @param priority the parsing priority this CodeSection has. 5 by default, a lower number means lower priority
     * @param patterns the CodeSection's patterns
     */
    public void addSection(Class<? extends CodeSection> c, int priority, String... patterns) {
        new SectionRegistrar<>(c, patterns).setPriority(priority).register();
    }

    public <E extends SkriptEvent> EventRegistrar<E> newEvent(Class<E> c, String... patterns) {
        return new EventRegistrar<>(c, patterns);
    }

    /**
     * Registers a {@link SkriptEvent}
     * @param c the SkriptEvent's class
     * @param handledContexts the {@link TriggerContext}s this SkriptEvent can handle
     * @param patterns the SkriptEvent's patterns
     */
    public void addEvent(Class<? extends SkriptEvent> c, Class<? extends TriggerContext>[] handledContexts, String... patterns) {
        new EventRegistrar<>(c, patterns).setHandledContexts(handledContexts).register();
    }

    /**
     * Registers a {@link SkriptEvent}
     * @param c the SkriptEvent's class
     * @param handledContexts the {@link TriggerContext}s this SkriptEvent can handle
     * @param priority the parsing priority this SkriptEvent has. 5 by default, a lower number means lower priority
     * @param patterns the SkriptEvent's patterns
     */
    public void addEvent(Class<? extends SkriptEvent> c, Class<? extends TriggerContext>[] handledContexts, int priority, String... patterns) {
        new EventRegistrar<>(c, patterns).setHandledContexts(handledContexts).setPriority(priority).register();
    }

    /**
     * Registers a {@link Type}
     * @param c the class the Type represents
     * @param pattern the Type's pattern
     * @param <T> the represented class
     */
    public <T> void addType(Class<T> c, String name, String pattern) {
        new TypeRegistrar<>(c, name, pattern).register();
    }

    /**
     * Starts a registration process for a {@link Type}
     * @param c the class the Type represents
     * @param pattern the Type's pattern
     * @param <T> the represented class
     * @return an {@link TypeRegistrar}
     */
    public <T> TypeRegistrar<T> newType(Class<T> c, String name, String pattern) {
        return new TypeRegistrar<>(c, name, pattern);
    }

    /**
     * Registers a converter
     * @param from the class it converts from
     * @param to the class it converts to
     * @param converter the converter
     * @param <F> from
     * @param <T> to
     */
    public <F, T> void addConverter(Class<F> from, Class<T> to, Function<? super F, ? extends T> converter) {
        converters.add(new ConverterInfo<>(from, to, converter));
    }

    /**
     * Registers a converter
     * @param from the class it converts from
     * @param to the class it converts to
     * @param converter the converter
     * @param options see {@link Converters}
     * @param <F> from
     * @param <T> to
     */
    public <F, T> void addConverter(Class<F> from, Class<T> to, Function<? super F, ? extends T> converter, int options) {
        converters.add(new ConverterInfo<>(from, to, converter, options));
    }

    /**
     * Adds all currently registered syntaxes to Skript's usable database.
     */
    public List<LogEntry> register() {
        SyntaxManager.register(this);
        TypeManager.register(this);
        Converters.registerConverters(this);
        Converters.createMissingConverters();
        return logger.close();
    }

    public interface Registrar {
        void register();
    }

    /**
     * A class for registering types.
     * @param <C> the represented class
     */
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

        /**
         * @param literalParser a function interpreting a string as an instance of the type
         * @return the registrar
         */
        public TypeRegistrar<C> literalParser(Function<String, ? extends C> literalParser) {
            this.literalParser = literalParser;
            return this;
        }

        /**
         * @param toStringFunction a function converting an instance of the type to a String
         * @return the registrar
         */
        public TypeRegistrar<C> toStringFunction(Function<? super C, String> toStringFunction) {
            this.toStringFunction = c -> c == null ? TypeManager.NULL_REPRESENTATION : toStringFunction.apply(c);
            return this;
        }

        /**
         * @param defaultChanger a default {@link Changer} for this type
         * @return the registrar
         */
        public TypeRegistrar<C> defaultChanger(Changer<? super C> defaultChanger) {
            this.defaultChanger = defaultChanger;
            return this;
        }

        /**
         * @param arithmetic a default {@link Arithmetic} for this type
         * @return the registrar
         */
        public <R> TypeRegistrar<C> arithmetic(Arithmetic<C, R> arithmetic) {
            this.arithmetic = arithmetic;
            return this;
        }

        /**
         * Adds this type to the list of currently registered syntaxes
         */
        @Override
        public void register() {
            newTypes = true;
            types.add(new Type<>(c, baseName, pattern, literalParser, toStringFunction, defaultChanger, arithmetic));
        }
    }

    public abstract class SyntaxRegistrar<C extends SyntaxElement> implements Registrar {
        protected final Class<C> c;
        private final List<String> patterns = new ArrayList<>();
        private int priority = 5;

        SyntaxRegistrar(Class<C> c, String... patterns) {
            this(c, 5, patterns);
            typeCheck();
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
            typeCheck();
        }

        ExpressionRegistrar(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
            super(c, patterns);
            this.returnType = returnType;
            this.isSingle = isSingle;
        }

        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : super.patterns) {
                PatternElement e = patternParser.parsePattern(s, logger);
                if (e != null) {
                    elements.add(e);
                }
            }
            Type<T> type = TypeManager.getByClassExact(returnType);
            if (type == null) {
                logger.error("Couldn't find a type corresponding to the class '" + returnType.getName() + "'", ErrorType.NO_MATCH);
                return;
            }
            ExpressionInfo<C, T> info = new ExpressionInfo<>(super.c, elements, registerer, type, isSingle, super.priority);
            expressions.putOne(super.c, info);
        }
    }

    public class EffectRegistrar<C extends Effect> extends SyntaxRegistrar<C> {

        EffectRegistrar(Class<C> c, String... patterns) {
            super(c, patterns);
            typeCheck();
        }

        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : super.patterns) {
                elements.add(patternParser.parsePattern(s, logger));
            }
            SyntaxInfo<C> info = new SyntaxInfo<>(super.c, elements, super.priority, registerer);
            effects.add(info);
        }
    }

    public class SectionRegistrar<C extends CodeSection> extends SyntaxRegistrar<C> {

        SectionRegistrar(Class<C> c, String... patterns) {
            super(c, patterns);
            typeCheck();
        }

        @Override
        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : super.patterns) {
                PatternElement e = patternParser.parsePattern(s, logger);
                if (e != null) {
                    elements.add(e);
                }
            }
            SyntaxInfo<C> info = new SyntaxInfo<>(super.c, elements, super.priority, registerer);
            sections.add(info);
        }
    }

    public class EventRegistrar<T extends SkriptEvent> extends SyntaxRegistrar<T> {
        private Class<? extends TriggerContext>[] handledContexts;

        EventRegistrar(Class<T> c, String... patterns) {
            super(c, patterns);
            typeCheck();
        }

        @Override
        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : super.patterns) {
                if (s.startsWith("*")) {
                    s = s.substring(1);
                } else {
                    s = "[on] " + s;
                }
                PatternElement e = patternParser.parsePattern(s, logger);
                if (e != null) {
                    elements.add(e);
                }
            }
            SkriptEventInfo<T> info = new SkriptEventInfo<>(super.c, handledContexts, elements, super.priority, registerer);
            events.add(info);
            registerer.addHandledEvent(this.c);
        }

        @SafeVarargs
        public final EventRegistrar<T> setHandledContexts(Class<? extends TriggerContext>... contexts) {
            this.handledContexts = contexts;
            return this;
        }
    }

    private void typeCheck() {
        if (newTypes) {
            TypeManager.register(this);
            newTypes = false;
        }
    }
}
