package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.PatternParser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.MultiMap;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SkriptRegistration {
    private String registerer;
    MultiMap<Class<?>, ExpressionInfo<?, ?>> expressions = new MultiMap<>();
    List<SyntaxInfo<? extends Effect>> effects = new ArrayList<>();
    List<SyntaxInfo<? extends CodeSection>> sections = new ArrayList<>();
    List<Type<?>> types = new ArrayList<>();
    List<Converters.ConverterInfo<?, ?>> converters = new ArrayList<>();
    private PatternParser patternParser;
    public SkriptRegistration(String registerer) {
        this.registerer = registerer;
        this.patternParser = new PatternParser();
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

    public void addSection(Class<? extends CodeSection> c, String... patterns) {
        new SectionRegistrar<>(c, patterns).register();
    }

    public void addSection(Class<? extends CodeSection> c, int priority, String... patterns) {
        new SectionRegistrar<>(c, patterns).setPriority(priority).register();
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
        private Function<String, ? extends C> literalParser;
        private Function<? super C, String> toStringFunction;
        private Changer<? super C> defaultChanger;
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
            ExpressionInfo<C, T> info = new ExpressionInfo<>(super.c, elements, type, isSingle, super.priority);
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
            SyntaxInfo<C> info = new SyntaxInfo<>(super.c, elements, super.priority);
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
            SyntaxInfo<C> info = new SyntaxInfo<C>(super.c, elements, super.priority);
            sections.add(info);
        }
    }
}
