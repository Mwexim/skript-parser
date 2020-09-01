package io.github.syst3ms.skriptparser.sections;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.lambda.ReturnSection;
import io.github.syst3ms.skriptparser.lang.lambda.SkriptFunction;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class SecFlatMap extends ReturnSection<Object> {
    private Variable<?> flatMapped;
    private SkriptFunction<?, ?> flatMapper;

    static {
        Main.getMainRegistration().addSection(
                SecFlatMap.class,
                "flat map %^objects%|map %^objects% flat"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        flatMapped = (Variable<?>) expressions[0];
        flatMapper = SkriptFunction.create(this);
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Object[] mappedValues = Arrays.stream(flatMapped.getValues(ctx))
                .flatMap(e -> flatMapper.apply(ctx, e)
                        .map(Arrays::stream)
                        .orElse(null)
                )
                .toArray();
        flatMapped.change(ctx, mappedValues, ChangeMode.SET);
        return getNext();
    }

    @Override
    public Class<?> getReturnType() {
        return flatMapped.getReturnType();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "flat map " + flatMapped.toString(ctx, debug);
    }
}
