package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.PatternParser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.MultiMap;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.ArrayList;
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

    public <C extends Expression<T>, T> void addExpression(Class<C> c, Class<T> returnType, boolean isSingle, String... patterns) {
        List<PatternElement> elements = new ArrayList<>();
        for (String s : patterns) {
            elements.add(patternParser.parsePattern(StringUtils.fixEncoding(s)));
        }
        Type<T> t = TypeManager.getByClassExact(returnType);
        if (t == null) {
            //TODO error
            return;
        }
        ExpressionInfo<C, T> info = new ExpressionInfo<>(c, elements, t, isSingle);
        expressions.putOne(returnType, info);
    }

    public <C extends Effect> void addEffect(Class<C> c, String... patterns) {
        List<PatternElement> elements = new ArrayList<>();
        for (String s : patterns) {
            elements.add(patternParser.parsePattern(StringUtils.fixEncoding(s)));
        }
        SyntaxInfo<C> info = new SyntaxInfo<>(c, elements);
        effects.add(info);
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
}
