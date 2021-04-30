package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SelfRegistrable;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.pattern.ChoiceElement;
import io.github.syst3ms.skriptparser.pattern.ChoiceGroup;
import io.github.syst3ms.skriptparser.pattern.CompoundElement;
import io.github.syst3ms.skriptparser.pattern.ExpressionElement;
import io.github.syst3ms.skriptparser.pattern.OptionalGroup;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.pattern.PatternParser;
import io.github.syst3ms.skriptparser.pattern.RegexGroup;
import io.github.syst3ms.skriptparser.pattern.TextElement;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValue;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValueState;
import io.github.syst3ms.skriptparser.registration.contextvalues.ContextValues;
import io.github.syst3ms.skriptparser.registration.tags.Tag;
import io.github.syst3ms.skriptparser.registration.tags.TagInfo;
import io.github.syst3ms.skriptparser.registration.tags.TagManager;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.conversions.ConverterInfo;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.classes.MultiMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A mutable object keeping track of all syntax and types registered by an {@link SkriptAddon addon}
 * Do not forget to call {@link #register()} !
 *
 * @see #getRegisterer()
 */
public class SkriptRegistration {
    private final SkriptAddon registerer;
    private final SkriptLogger logger = new SkriptLogger();
    private final MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
    private final List<SyntaxInfo<? extends Effect>> effects = new ArrayList<>();
    private final List<SyntaxInfo<? extends CodeSection>> sections = new ArrayList<>();
    private final List<SkriptEventInfo<?>> events = new ArrayList<>();
    private final List<Type<?>> types = new ArrayList<>();
    private final List<ConverterInfo<?, ?>> converters = new ArrayList<>();
    private final List<ContextValue<?>> contextValues = new ArrayList<>();
    private final List<TagInfo<? extends Tag>> tags = new ArrayList<>();
    private boolean newTypes = false;

    public SkriptRegistration(SkriptAddon registerer) {
        this.registerer = registerer;
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
     * @return all currently registered context values
     */
    public List<ContextValue<?>> getContextValues() {
        return contextValues;
    }

    /**
     * @return all currently registered tags
     */
    public List<TagInfo<?>> getTags() {
        return tags;
    }

    /**
     * Registers a syntax class that can register itself.
     * @param c the syntax' class
     * @param args the arguments
     */
    public void addSelfRegisteringElement(Class<? extends SelfRegistrable> c, Object... args) {
        try {
            c.getDeclaredConstructor().newInstance().register(this, args);
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Couldn't instantiate class '" + c.getName() + "'", ErrorType.EXCEPTION);
        }
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
     * Starts a registration process for a {@link PropertyExpression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param isSingle whether the Expression is a single value
     * @param ownerType the type of the owner
     * @param property the property that is used
     * @param <C> the Expression
     * @param <T> the Expression's return type
     * @return an {@link ExpressionRegistrar} to continue the registration process
     */
    public <C extends Expression<T>, T> ExpressionRegistrar<C, T> newPropertyExpression(Class<C> c, Class<T> returnType, boolean isSingle, String ownerType, String property) {
        return new ExpressionRegistrar<>(c, returnType, isSingle,
                adaptPropertyPrefix(ownerType) + "'[s] " + property,
                (property.startsWith("[the]") ? property : "[the] " + property) + " of " + adaptPropertyPrefix(ownerType));
    }

    /**
     * Registers a {@link PropertyExpression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param isSingle whether the Expression is a single value
     * @param ownerType the type of the owner
     * @param property the property that is used
     * @param <C> the Expression
     * @param <T> the Expression's return type
     */
    public <C extends Expression<T>, T> void addPropertyExpression(Class<C> c, Class<T> returnType, boolean isSingle, String ownerType, String property) {
        new ExpressionRegistrar<>(c, returnType, isSingle,
                adaptPropertyPrefix(ownerType) + "'[s] " + property,
                (property.startsWith("[the]") ? property : "[the] " + property) + " of " + adaptPropertyPrefix(ownerType))
                .register();
    }

    /**
     * Registers a {@link PropertyExpression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param isSingle whether the Expression is a single value
     * @param priority the parsing priority this Expression has. 5 by default, a lower number means lower priority
     * @param ownerType the type of the owner
     * @param property the property that is used
     * @param <C> the Expression
     * @param <T> the Expression's return type
     */
    public <C extends Expression<T>, T> void addPropertyExpression(Class<C> c, Class<T> returnType, boolean isSingle, int priority, String ownerType, String property) {
        new ExpressionRegistrar<>(c, returnType, isSingle,
                adaptPropertyPrefix(ownerType) + "'[s] " + property,
                (property.startsWith("[the]") ? property : "[the] " + property) + " of " + adaptPropertyPrefix(ownerType))
                .setPriority(priority)
                .register();
    }

    /**
     * Starts a registration process for an {@link Effect}
     * @param c the Effect's class
     * @param patterns the Effect's patterns
     * @param <C> the Effect
     * @return an {@link EffectRegistrar} to continue the registration process
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

    /**
     * Starts a registration process for a {@link SkriptEvent}
     * @param c the SkriptEvent's class
     * @param patterns the SkriptEvent's patterns
     * @param <E> the SkriptEvent
     * @return an {@link EventRegistrar} to continue the registration process
     */
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
     * Registers a {@link Type}
     * @param c the class the Type represents
     * @param pattern the Type's pattern
     * @param <T> the represented class
     */
    public <T> void addType(Class<T> c, String name, String pattern) {
        new TypeRegistrar<>(c, name, pattern).register();
    }

    /**
     * Starts the registration process for an {@link Enum} as a {@link Type}.
     * <br>
     * The {@link TypeRegistrar#literalParser(Function) literalParser(Function)}
     * and {@link TypeRegistrar#toStringFunction(Function) toStringFunction(Function)} methods
     * are implemented at default.
     * Note that the represented Enum's values need to be in the {@code SCREAMING_SNAKE_CASE} convention.
     * @param c the class the Type represents
     * @param pattern the Type's pattern
     * @param <T> the represented class
     * @return an {@link TypeRegistrar}
     */
    public <T extends Enum<T>> TypeRegistrar<T> newEnumType(Class<T> c, String name, String pattern) {
        return new TypeRegistrar<>(c, name, pattern)
                .literalParser(s -> {
                    // We won't allow ugly syntax.
                    // Otherwise, a field like 'MY_ENUM_CONSTANT' would allow 'my enum_constant'.
                    if (s.contains("_"))
                        return null;
                    s = s.replaceAll(" ", "_").toUpperCase();
                    try {
                        return Enum.valueOf(c, s);
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                })
                .toStringFunction(o -> o.toString().replaceAll("_", " ").toLowerCase());
    }

    /**
     * Registers an {@link Enum} as a {@link Type}.
     * <br>
     * The {@link TypeRegistrar#literalParser(Function) literalParser(Function)}
     * and {@link TypeRegistrar#toStringFunction(Function) toStringFunction(Function)} methods
     * are implemented at default.
     * Note that the represented Enum's values need to be in the {@code SCREAMING_SNAKE_CASE} convention.
     * @param c the class the Type represents
     * @param pattern the Type's pattern
     * @param <T> the represented class
     */
    public <T extends Enum<T>> void addEnumType(Class<T> c, String name, String pattern) {
        new TypeRegistrar<>(c, name, pattern)
                .literalParser(s -> {
                    // We won't allow ugly syntax.
                    // Otherwise, a field like 'MY_ENUM_CONSTANT' would allow 'my enum_constant'.
                    if (s.contains("_"))
                        return null;
                    s = s.replaceAll(" ", "_").toUpperCase();
                    try {
                        return Enum.valueOf(c, s);
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                })
                .toStringFunction(o -> o.toString().replaceAll("_", " ").toLowerCase())
                .register();
    }

    /**
     * Registers a converter
     * @param from the class it converts from
     * @param to the class it converts to
     * @param converter the converter
     * @param <F> from
     * @param <T> to
     */
    public <F, T> void addConverter(Class<F> from, Class<T> to, Function<? super F, Optional<? extends T>> converter) {
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
    public <F, T> void addConverter(Class<F> from, Class<T> to, Function<? super F, Optional<? extends T>> converter, int options) {
        converters.add(new ConverterInfo<>(from, to, converter, options));
    }

    /**
     * Registers a {@link Tag}.
     * @param c the Tag's class
     */
    public void addTag(Class<? extends Tag> c) {
        tags.add(new TagInfo<>(c, 5, registerer));
    }

    /**
     * Registers a {@link Tag}.
     * @param c the Tag's class
     * @param priority the parsing priority this Tag has. 5 by default, a lower number means lower priority
     */
    public void addTag(Class<? extends Tag> c, int priority) {
        tags.add(new TagInfo<>(c, priority, registerer));
    }

    /**
     * Adds all currently registered syntaxes to Skript's usable database.
     */
    public List<LogEntry> register() {
        SyntaxManager.register(this);
        ContextValues.register(this);
        TypeManager.register(this);
        TagManager.register(this);
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
        protected final List<String> patterns = new ArrayList<>();
        protected int priority;

        SyntaxRegistrar(Class<C> c, String... patterns) {
            this(c, -1, patterns);
            typeCheck();
        }

        SyntaxRegistrar(Class<C> c, int priority, String... patterns) {
            this.c = c;
            this.priority = priority;
            Collections.addAll(this.patterns, patterns);
        }

        /**
         * Adds patterns to the current syntax
         * @param patterns the patterns to add
         * @return the registrar
         */
        public SyntaxRegistrar<C> addPatterns(String... patterns) {
            Collections.addAll(this.patterns, patterns);
            return this;
        }

        /**
         * Sets the priority of the current syntax. Default is 5.
         * @param priority the priority
         * @return the registrar
         */
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

        /**
         * Adds this expression to the list of currently registered syntaxes
         */
        @Override
        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            boolean computePriority = priority == -1;
            priority = computePriority ? 5 : priority;
            super.patterns.forEach(s -> PatternParser.parsePattern(s, logger).ifPresent(e -> {
                if (computePriority)
                    setPriority(Math.min(priority, findAppropriatePriority(e)));
                elements.add(e);
            }));
            var type = TypeManager.getByClassExact(returnType);
            if (type.isEmpty()) {
                logger.error("Couldn't find a type corresponding to the class '" + returnType.getName() + "'", ErrorType.NO_MATCH);
                return;
            }
            var info = new ExpressionInfo<>(super.c, elements, registerer, type.get(), isSingle, priority);
            expressions.putOne(super.c, info);
        }
    }

    public class EffectRegistrar<C extends Effect> extends SyntaxRegistrar<C> {

        EffectRegistrar(Class<C> c, String... patterns) {
            super(c, patterns);
            typeCheck();
        }

        /**
         * Adds this effect to the list of currently registered syntaxes
         */
        @Override
        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            boolean computePriority = priority == -1;
            priority = computePriority ? 5 : priority;
            super.patterns.forEach(s -> PatternParser.parsePattern(s, logger).ifPresent(e -> {
                if (computePriority)
                    setPriority(Math.min(priority, findAppropriatePriority(e)));
                elements.add(e);
            }));
            var info = new SyntaxInfo<>(super.c, elements, priority, registerer);
            effects.add(info);
        }
    }

    public class SectionRegistrar<C extends CodeSection> extends SyntaxRegistrar<C> {

        SectionRegistrar(Class<C> c, String... patterns) {
            super(c, patterns);
            typeCheck();
        }

        /**
         * Adds this section to the list of currently registered syntaxes
         */
        @Override
        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            boolean computePriority = priority == -1;
            priority = computePriority ? 5 : priority;
            super.patterns.forEach(s -> PatternParser.parsePattern(s, logger).ifPresent(e -> {
                if (computePriority)
                    setPriority(Math.min(priority, findAppropriatePriority(e)));
                elements.add(e);
            }));
            var info = new SyntaxInfo<>(super.c, elements, priority, registerer);
            sections.add(info);
        }
    }

    @SuppressWarnings("unchecked")
    public class EventRegistrar<T extends SkriptEvent> extends SyntaxRegistrar<T> {
        private Set<Class<? extends TriggerContext>> handledContexts = new HashSet<>();

        EventRegistrar(Class<T> c, String... patterns) {
            super(c, patterns);
            typeCheck();
        }

        /**
         * Adds this event to the list of currently registered syntaxes
         */
        @Override
        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            boolean computePriority = priority == -1;
            priority = computePriority ? 5 : priority;
            for (var s : super.patterns) {
                if (s.startsWith("*")) {
                    s = s.substring(1);
                } else {
                    s = "[on] " + s;
                }
                PatternParser.parsePattern(s, logger).ifPresent(e -> {
                    if (computePriority)
                        setPriority(Math.min(priority, findAppropriatePriority(e)));
                    elements.add(e);
                });
            }
            var info = new SkriptEventInfo<>(super.c, handledContexts, elements, priority, registerer);
            events.add(info);
            registerer.addHandledEvent(this.c);
        }

        /**
         * Set the context this event can handle
         * @param contexts the contexts
         * @return the registrar
         */
        @SafeVarargs
        public final EventRegistrar<T> setHandledContexts(Class<? extends TriggerContext>... contexts) {
            this.handledContexts = Set.of(contexts);
            return this;
        }

        /**
         * Registers a {@link ContextValue}
         * @param context the context this value appears in
         * @param returnType the returned type of this context value
         * @param isSingle whether or not this value is single
         * @param name the name of this context value (used in the suffix)
         * @param contextFunction the function that needs to be applied in order to get the context value
         * @param <C> the context class
         * @param <R> the type class
         * @return the registrar
         */
        public final <C extends TriggerContext, R> EventRegistrar<T> addContextValue(Class<C> context, Class<R> returnType, boolean isSingle, String name, Function<C, R[]> contextFunction) {
            var type = (Type<R>) TypeManager.getByClass(returnType).orElseThrow();
            var pattern = removePrefix(name);
            contextValues.add(new ContextValue<>(context, type, isSingle, pattern, (Function<TriggerContext, R[]>) contextFunction, name.startsWith("*")));
            return this;
        }

        /**
         * Registers a {@link ContextValue}
         * @param context the context this value appears in
         * @param returnType the returned type of this context value
         * @param isSingle whether or not this value is single
         * @param name the name of this context value (used in the suffix)
         * @param contextFunction the function that needs to be applied in order to get the context value
         * @param state whether this happens in the present, past or future
         * @param <C> the context class
         * @param <R> the type class
         * @return the registrar
         */
        public final <C extends TriggerContext, R> EventRegistrar<T> addContextValue(Class<C> context, Class<R> returnType, boolean isSingle, String name, Function<C, R[]> contextFunction, ContextValueState state) {
            var type = (Type<R>) TypeManager.getByClass(returnType).orElseThrow();
            var pattern = removePrefix(name);
            contextValues.add(new ContextValue<>(context, type, isSingle, pattern, (Function<TriggerContext, R[]>) contextFunction, state, name.startsWith("*")));
            return this;
        }
    }

    private void typeCheck() {
        if (newTypes) {
            TypeManager.register(this);
            newTypes = false;
        }
    }

    private String adaptPropertyPrefix(String str) {
        return str.startsWith("*") ? str.substring(1) : "%" + str + "%";
    }

    private String removePrefix(String str) {
        return str.startsWith("*") ? str.substring(1) : str;
    }

    private int findAppropriatePriority(PatternElement el) {
        if (el instanceof TextElement) {
            return 5;
        } else if (el instanceof RegexGroup) {
            return 1;
        } else if (el instanceof ChoiceGroup) {
            var priority = 5;
            for (ChoiceElement choice : ((ChoiceGroup) el).getChoices()) {
                priority = Math.min(priority, findAppropriatePriority(choice.getElement()));
            }
            return priority;
        } else if (el instanceof ExpressionElement) {
            return 2;
        } else {
            assert el instanceof CompoundElement : "a single Optional group as a pattern";
            var compound = (CompoundElement) el;
            var elements = compound.getElements();
            var priority = 5;
            for (PatternElement element : elements) {
                var e = element instanceof OptionalGroup ? ((OptionalGroup) element).getElement() : element;
                priority = Math.min(priority, findAppropriatePriority(e));
                if (!(element instanceof OptionalGroup || e instanceof TextElement && ((TextElement) e).getText().isBlank()))
                    break;
            }
            var containsRegex = elements.stream().anyMatch(p -> p instanceof RegexGroup);
            return containsRegex ? Math.min(priority, 3) : priority;
        }
    }
}
