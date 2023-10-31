package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ContextExpression;
import io.github.syst3ms.skriptparser.lang.base.ExecutableExpression;
import io.github.syst3ms.skriptparser.lang.properties.ConditionalType;
import io.github.syst3ms.skriptparser.lang.properties.PropertyConditional;
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
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.State;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.Usage;
import io.github.syst3ms.skriptparser.registration.context.ContextValues;
import io.github.syst3ms.skriptparser.registration.tags.Tag;
import io.github.syst3ms.skriptparser.registration.tags.TagInfo;
import io.github.syst3ms.skriptparser.registration.tags.TagManager;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.conversions.ConverterInfo;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import io.github.syst3ms.skriptparser.util.MultiMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final List<ContextValue<?, ?>> contextValues = new ArrayList<>();
    private final List<TagInfo<? extends Tag>> tags = new ArrayList<>();
    private boolean newTypes = false;

    public SkriptRegistration(SkriptAddon registerer) {
        this.registerer = registerer;
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
     * @return all currently registered sections
     */
    public List<SyntaxInfo<? extends CodeSection>> getSections() {
        return sections;
    }
    /**
     * @return all currently registered events
     */
    public List<SkriptEventInfo<?>> getEvents() {
        return events;
    }

    /**
     * @return all currently registered types
     */
    public List<Type<?>> getTypes() {
        return types;
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
    public List<ContextValue<?, ?>> getContextValues() {
        return contextValues;
    }

    /**
     * @return all currently registered tags
     */
    public List<TagInfo<?>> getTags() {
        return tags;
    }

    /**
     * @return the addon handling this registration (may be Skript itself)
     */
    public SkriptAddon getRegisterer() {
        return registerer;
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
        newExpression(c, returnType, isSingle, patterns).register();
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
        newExpression(c, returnType, isSingle, patterns).setPriority(priority).register();
    }

    /**
     * Starts a registration process for a {@link PropertyExpression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param owner the owner in the pattern
     * @param property the property
     * @param <C> the Expression
     * @param <T> the Expression's return type
     * @return an {@link ExpressionRegistrar} to continue the registration process
     */
    public <C extends PropertyExpression<?, T>, T> ExpressionRegistrar<C, T> newPropertyExpression(Class<C> c, Class<T> returnType, String property, String owner) {
        return (ExpressionRegistrar<C, T>) newExpression(c, returnType, false, PropertyExpression.composePatterns(property, owner))
                .addData(PropertyExpression.PROPERTY_IDENTIFIER, property);
    }

    /**
     * Registers a {@link PropertyExpression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param owner the owner in the pattern
     * @param property the property
     * @param <C> the Expression
     * @param <T> the Expression's return type
     */
    public <C extends PropertyExpression<?, T>, T> void addPropertyExpression(Class<C> c, Class<T> returnType, String property, String owner) {
        newPropertyExpression(c, returnType, property, owner).register();
    }

    /**
     * Registers a {@link PropertyExpression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param priority the priority
     * @param owner the owner in the pattern
     * @param property the property
     * @param <C> the Expression
     * @param <T> the Expression's return type
     */
    public <C extends PropertyExpression<?, T>, T> void addPropertyExpression(Class<C> c, Class<T> returnType, int priority, String property, String owner) {
        newPropertyExpression(c, returnType, property, owner).setPriority(priority).register();
    }

    /**
     * Starts a registration process for a {@link PropertyConditional}
     * @param c the Expression's class
     * @param performer the type of the performer
     * @param conditionalType the verb used in this conditional property
     * @param property the property
     * @param <C> the Expression
     * @return an {@link ExpressionRegistrar} to continue the registration process
     */
    public <C extends PropertyConditional<?>> ExpressionRegistrar<C, Boolean> newPropertyConditional(Class<C> c, String performer, ConditionalType conditionalType, String property) {
        return (ExpressionRegistrar<C, Boolean>) newExpression(c, Boolean.class, true, PropertyConditional.composePatterns(performer, conditionalType, property))
                .addData(PropertyConditional.CONDITIONAL_TYPE_IDENTIFIER, conditionalType)
                .addData(PropertyConditional.PROPERTY_IDENTIFIER, property);
    }

    /**
     * Registers a {@link PropertyConditional}
     * @param c the Expression's class
     * @param performer the type of the performer
     * @param conditionalType the verb used in this conditional property
     * @param property the property
     * @param <C> the Expression
     */
    public <C extends PropertyConditional<?>> void addPropertyConditional(Class<C> c, String performer, ConditionalType conditionalType, String property) {
        newPropertyConditional(c, performer, conditionalType, property).register();
    }

    /**
     * Registers a {@link PropertyConditional}
     * @param c the Expression's class
     * @param priority the parsing priority this Expression has. 5 by default, a lower number means lower priority
     * @param performer the type of the performer
     * @param conditionalType the verb used in this conditional property
     * @param property the property
     * @param <C> the Expression
     */
    public <C extends PropertyConditional<?>> void addPropertyConditional(Class<C> c, int priority, String performer, ConditionalType conditionalType, String property) {
        newPropertyConditional(c, performer, conditionalType, property).setPriority(priority).register();
    }

    /**
     * Registers an {@link ExecutableExpression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param isSingle whether the Expression is a single value
     * @param patterns the Expression's patterns
     * @param <C> the Expression
     * @param <T> the Expression's return type
     */
    public <C extends ExecutableExpression<T>, T> void addExecutableExpression(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
        addExpression(c, returnType, isSingle, patterns);
        addEffect(c, patterns);
    }

    /**
     * Registers an {@link ExecutableExpression}
     * @param c the Expression's class
     * @param returnType the Expression's return type
     * @param isSingle whether the Expression is a single value
     * @param patterns the Expression's patterns
     * @param <C> the Expression
     * @param <T> the Expression's return type
     */
    public <C extends ExecutableExpression<T>, T> void addExecutableExpression(Class<C> c, Class<T> returnType, boolean isSingle, int priority, String... patterns) {
        addExpression(c, returnType, isSingle, priority, patterns);
        addEffect(c, priority, patterns);
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
        newEffect(c, patterns).register();
    }

    /**
     * Registers an {@link Effect}
     * @param c the Effect's class
     * @param priority the parsing priority this Effect has. 5 by default, a lower number means lower priority
     * @param patterns the Effect's patterns
     * @param <C> the Effect
     */
    public <C extends Effect> void addEffect(Class<C> c, int priority, String... patterns) {
        newEffect(c,patterns).setPriority(priority).register();
    }

    /**
     * Starts a registration process for a {@link CodeSection}
     * @param c the CodeSection's class
     * @param patterns the CodeSection's patterns
     * @param <C> the CodeSection
     * @return a {@link SectionRegistrar} to continue the registration process
     */
    public <C extends CodeSection> SectionRegistrar<C> newSection(Class<C> c, String... patterns) {
        return new SectionRegistrar<>(c, patterns);
    }


    /**
     * Registers a {@link CodeSection}
     * @param c the CodeSection's class
     * @param patterns the CodeSection's patterns
     */
    public void addSection(Class<? extends CodeSection> c, String... patterns) {
        newSection(c, patterns).register();
    }

    /**
     * Registers a {@link CodeSection}
     * @param c the CodeSection's class
     * @param priority the parsing priority this CodeSection has. 5 by default, a lower number means lower priority
     * @param patterns the CodeSection's patterns
     */
    public void addSection(Class<? extends CodeSection> c, int priority, String... patterns) {
        newSection(c, patterns).setPriority(priority).register();
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
        newEvent(c, patterns).setHandledContexts(handledContexts).register();
    }

    /**
     * Registers a {@link SkriptEvent}
     * @param c the SkriptEvent's class
     * @param handledContexts the {@link TriggerContext}s this SkriptEvent can handle
     * @param priority the parsing priority this SkriptEvent has. 5 by default, a lower number means lower priority
     * @param patterns the SkriptEvent's patterns
     */
    public void addEvent(Class<? extends SkriptEvent> c, Class<? extends TriggerContext>[] handledContexts, int priority, String... patterns) {
        newEvent(c, patterns).setHandledContexts(handledContexts).setPriority(priority).register();
    }

    /**
     * Starts a registration process for a {@link ContextExpression}
     * @param context the TriggerContext class
     * @param returnType the returned type of this context value
     * @param isSingle whether or not the return value is single
     * @param pattern the pattern
     * @param function the function that needs to be applied in order to get the context value
     * @param <C> the TriggerContext class
     * @param <T> the ContextValue's return type
     * @return a {@link ContextValueRegistrar} to continue the registration process
     */
    public <C extends TriggerContext, T> ContextValueRegistrar<C, T> newContextValue(Class<C> context, Class<T> returnType, boolean isSingle, String pattern, Function<C, T[]> function) {
        return new ContextValueRegistrar<>(context, returnType, isSingle, pattern, function);
    }

    /**
     * Registers a {@link ContextValue}
     * @param context the TriggerContext class
     * @param returnType the returned type of this context value
     * @param isSingle whether or not the return value is single
     * @param pattern the pattern
     * @param function the function that needs to be applied in order to get the context value
     * @param <C> the TriggerContext class
     * @param <T> the ContextValue's return type
     */
    public <C extends TriggerContext, T> void addContextValue(Class<C> context, Class<T> returnType, boolean isSingle, String pattern, Function<C, T[]> function) {
        newContextValue(context, returnType, isSingle, pattern, function).register();
    }

    /**
     * Registers a {@link ContextValue} that returns a single value.
     * The {@linkplain Type#getBaseName() base name} of the return type will be used as pattern.
     * There will be a leading '{@code [the] }' in the pattern if the context value can be used alone.
     * @param context the TriggerContext class
     * @param returnType the returned type of this context value
     * @param function the function that needs to be applied in order to get the context value
     * @param <C> the TriggerContext class
     * @param <T> the ContextValue's return type
     */
    public <C extends TriggerContext, T> void addContextType(Class<C> context, Class<T> returnType, Function<C, T> function) {
        addContextType(context, returnType, function, State.PRESENT, Usage.EXPRESSION_ONLY);
    }

    /**
     * Registers a {@link ContextValue} that returns a single value.
     * The {@linkplain Type#getBaseName() base name} of the return type will be used as pattern.
     * There will be a leading '{@code [the] }' in the pattern if the context value can be used alone.
     * @param context the TriggerContext class
     * @param returnType the returned type of this context value
     * @param function the function that needs to be applied in order to get the context value
     * @param state the time state
     * @param <C> the TriggerContext class
     * @param <T> the ContextValue's return type
     * @see State#PRESENT
     * @see Usage#EXPRESSION_ONLY
     */
    public <C extends TriggerContext, T> void addContextType(Class<C> context, Class<T> returnType, Function<C, T> function, State state) {
        addContextType(context, returnType, function, state, Usage.EXPRESSION_ONLY);
    }

    /**
     * Registers a {@link ContextValue} that returns a single value.
     * The {@linkplain Type#getBaseName() base name} of the return type will be used as pattern.
     * There will be a leading '{@code [the] }' in the pattern if the context value can be used alone.
     * @param context the TriggerContext class
     * @param returnType the returned type of this context value
     * @param function the function that needs to be applied in order to get the context value
     * @param state the time state
     * @param usage the usage
     * @param <C> the TriggerContext class
     * @param <T> the ContextValue's return type
     * @return this {@link EventRegistrar}
     * @see Usage#EXPRESSION_ONLY
     */
    public <C extends TriggerContext, T> void addContextType(Class<C> context, Class<T> returnType, Function<C, T> function, State state, Usage usage) {
        var typeName = TypeManager.getByClassExact(returnType).map(Type::getBaseName);
        if (typeName.isEmpty()) {
            logger.error("Couldn't find a type corresponding to the class '" + returnType.getName() + "'", ErrorType.NO_MATCH);
            return;
        }
        newContextValue(context, returnType, true, typeName.get(), value -> CollectionUtils.arrayOf(function.apply(value)))
                .setState(state)
                .setUsage(usage)
                .register();
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
        newType(c, name, pattern).register();
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
        tags.add(new TagInfo<>(c, 5));
    }

    /**
     * Registers a {@link Tag}.
     * @param c the Tag's class
     * @param priority the parsing priority this Tag has. 5 by default, a lower number means lower priority
     */
    public void addTag(Class<? extends Tag> c, int priority) {
        tags.add(new TagInfo<>(c, priority));
    }

    /**
     * Adds all currently registered syntaxes to Skript's usable database.
     * @return all possible errors, warnings and other logs that occurred while parsing the patterns
     */
    public List<LogEntry> register() {
        return register(false);
    }

    /**
     * Adds all currently registered syntaxes to Skript's usable database.
     * @param ignoreLogs whether to return the logs and close the logger,
     *                   or just ignore and clear them while keeping the logger open
     * @return all possible errors, warnings and other logs that occurred while parsing the patterns
     */
    public List<LogEntry> register(boolean ignoreLogs) {
        SyntaxManager.register(this);
        ContextValues.register(this);
        TypeManager.register(this);
        TagManager.register(this);
        Converters.registerConverters(this);
        Converters.createMissingConverters();
        if (ignoreLogs) {
            logger.clearLogs();
            return new ArrayList<>();
        } else {
            return logger.close();
        }
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
        protected final Map<String, Object> data = new HashMap<>();

        SyntaxRegistrar(Class<C> c, String... patterns) {
            this.c = c;
            Collections.addAll(this.patterns, patterns);
            typeCheck();
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
                throw new SkriptParserException("Can't have a negative priority!");
            this.priority = priority;
            return this;
        }

        public SyntaxRegistrar<C> addData(String identifier, Object data) {
            this.data.put(identifier, data);
            return this;
        }

        protected List<PatternElement> parsePatterns() {
            boolean computePriority = priority == -1;
            priority = computePriority ? 5 : priority;
            return patterns.stream()
                    .map(s -> {
                        var result = PatternParser.parsePattern(s, logger).orElse(null);
                        logger.finalizeLogs();
                        return result;
                    })
                    .filter(Objects::nonNull)
                    .peek(e -> {
                        if (computePriority)
                            setPriority(Math.min(priority, findAppropriatePriority(e)));
                    })
                    .collect(Collectors.toList());
        }
    }

    public class ExpressionRegistrar<C extends Expression<? extends T>, T> extends SyntaxRegistrar<C> {
        private final Class<T> returnType;
        private final boolean isSingle;

        ExpressionRegistrar(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
            super(c, patterns);
            this.returnType = returnType;
            this.isSingle = isSingle;
            typeCheck();
        }

        /**
         * Adds this expression to the list of currently registered syntaxes
         */
        @Override
        public void register() {
            var type = TypeManager.getByClassExact(returnType);
            if (type.isEmpty()) {
                logger.error("Couldn't find a type corresponding to the class '" + returnType.getName() + "'", ErrorType.NO_MATCH);
                return;
            }
            expressions.putOne(super.c, new ExpressionInfo<>(registerer, super.c, type.get(), isSingle, priority, parsePatterns(), super.data));
        }
    }

    public class EffectRegistrar<C extends Effect> extends SyntaxRegistrar<C> {
        EffectRegistrar(Class<C> c, String... patterns) {
            super(c, patterns);
        }

        /**
         * Adds this effect to the list of currently registered syntaxes
         */
        @Override
        public void register() {
            effects.add(new SyntaxInfo<>(registerer, super.c, priority, parsePatterns(), super.data));
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
            sections.add(new SyntaxInfo<>(registerer, super.c, priority, parsePatterns(), super.data));
        }
    }

    public class EventRegistrar<T extends SkriptEvent> extends SyntaxRegistrar<T> {
        private Set<Class<? extends TriggerContext>> handledContexts = new HashSet<>();

        EventRegistrar(Class<T> c, String... patterns) {
            super(c, patterns);
            typeCheck();
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
         * Adds this event to the list of currently registered syntaxes
         */
        @Override
        public void register() {
            for (int i = 0; i < super.patterns.size(); i++) {
                var pattern = super.patterns.get(i);
                if (pattern.startsWith("*")) {
                    super.patterns.set(i, pattern.substring(1));
                } else {
                    super.patterns.set(i, "[on] " + pattern);
                }
            }
            events.add(new SkriptEventInfo<>(registerer, super.c, handledContexts, priority, parsePatterns(), data));
            registerer.addHandledEvent(this.c);
        }
    }

    public class ContextValueRegistrar<C extends TriggerContext, T> implements Registrar {
        private final Class<C> context;
        private final Class<T> returnType;
        private final boolean isSingle;
        private final String pattern;

        private final Function<C, T[]> function;
        private State state = State.PRESENT;
        private Usage usage = Usage.EXPRESSION_ONLY;

        @SuppressWarnings("unchecked")
        private Class<? extends C>[] excluded = new Class[0];

        public ContextValueRegistrar(Class<C> context, Class<T> returnType, boolean isSingle, String pattern, Function<C, T[]> function) {
            this.context = context;
            this.returnType = returnType;
            this.isSingle = isSingle;
            this.pattern = pattern;
            this.function = function;
        }

        public ContextValueRegistrar<C, T> setState(State state) {
            this.state = state;
            return this;
        }

        public ContextValueRegistrar<C, T> setUsage(Usage usage) {
            this.usage = usage;
            return this;
        }

        @SafeVarargs
        public final ContextValueRegistrar<C, T> setExcluded(Class<? extends C>... excluded) {
            this.excluded = excluded;
            return this;
        }

        @Override
        public void register() {
            var pattern = PatternParser.parsePattern(this.pattern, logger);
            if (pattern.isEmpty())
                return;

            var type = TypeManager.getByClassExact(returnType);
            if (type.isEmpty()) {
                logger.error("Couldn't find a type corresponding to the class '" + returnType.getName() + "'", ErrorType.NO_MATCH);
                return;
            }

            // Register the context value
            contextValues.add(new ContextValue<>(context, type.get(), isSingle, pattern.get(), function, state, usage, excluded));
        }
    }

    private void typeCheck() {
        if (newTypes) {
            TypeManager.register(this);
            newTypes = false;
        }
    }

    private static String removePrefix(String str) {
        return str.startsWith("*") ? str.substring(1) : str;
    }

    private static int findAppropriatePriority(PatternElement el) {
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
