package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.PatternParser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.MultiMap;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SkriptRegistration {
    private String registerer;
    private MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
    private List<SyntaxInfo<? extends Effect>> effects = new ArrayList<>();
    private List<Type<?>> types = new ArrayList<>();
    private List<Converters.ConverterInfo<?, ?>> converters = new ArrayList<>();
    private PatternParser patternParser;

    public SkriptRegistration(String registerer) {
        this.registerer = registerer;
        this.patternParser = new PatternParser();
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

    public String getRegisterer() {
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

    public <T> void addType(Class<T> c, String name, String pattern) {
        types.add(new Type<>(c, name, pattern));
    }

    public <T> void addType(Class<T> c, String name, String pattern, Function<String, ? extends T> literalParser) {
        types.add(new Type<>(c, name, pattern, literalParser));
    }

    public <T> void addType(Class<T> c, String name, String pattern, Function<String, ? extends T> literalParser, Function<? super T, String> toStringFunction) {
        types.add(new Type<>(c, name, pattern, literalParser, toStringFunction));
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

    public class ExpressionRegistrar<C extends Expression<? extends T>, T> {
        private final Class<C> c;
        private final Class<T> returnType;
        private final boolean isSingle;
        private List<String> patterns = new ArrayList<>();
        private int priority = 5;

        public ExpressionRegistrar(Class<C> c, Class<T> returnType, boolean isSingle) {
            this.c = c;
            this.returnType = returnType;
            this.isSingle = isSingle;
        }

        public ExpressionRegistrar(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
            this.c = c;
            this.returnType = returnType;
            this.isSingle = isSingle;
            Collections.addAll(this.patterns, patterns);
        }

        public ExpressionRegistrar<C, T> addPatterns(String... patterns) {
            Collections.addAll(this.patterns, patterns);
            return this;
        }

        public ExpressionRegistrar<C, T> setPriority(int priority) {
            if (priority < 0)
                throw new IllegalArgumentException("Can't have a negative priority !");
            this.priority = priority;
            return this;
        }

        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : patterns) {
                elements.add(patternParser.parsePattern(StringUtils.fixEncoding(s)));
            }
            Type<T> type = TypeManager.getByClassExact(returnType);
            if (type == null) {
                throw new SkriptParserException("Couldn't figure out the return type corresponding to " + returnType.getName());
            }
            ExpressionInfo<C, T> info = new ExpressionInfo<>(c, elements, type, isSingle, priority);
            expressions.putOne(c, info);
        }
    }

    public class EffectRegistrar<C extends Effect> {
        private final Class<C> c;
        private List<String> patterns = new ArrayList<>();
        private int priority = 5;

        public EffectRegistrar(Class<C> c, String... patterns) {
            this.c = c;
            Collections.addAll(this.patterns, patterns);
        }

        public EffectRegistrar<C> addPatterns(String... patterns) {
            Collections.addAll(this.patterns, patterns);
            return this;
        }

        public EffectRegistrar<C> setPriority(int priority) {
            if (priority < 0)
                throw new IllegalArgumentException("Can't have a negative priority !");
            this.priority = priority;
            return this;
        }

        public void register() {
            List<PatternElement> elements = new ArrayList<>();
            for (String s : patterns) {
                elements.add(patternParser.parsePattern(StringUtils.fixEncoding(s)));
            }
            SyntaxInfo<C> info = new SyntaxInfo<>(c, elements, 5);
            effects.add(info);
        }
    }
}
