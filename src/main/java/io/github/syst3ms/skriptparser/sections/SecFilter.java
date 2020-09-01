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

public class SecFilter extends ReturnSection<Boolean> {
    private Variable<?> filtered;
    private SkriptFunction<SecFilter, Boolean> lambda;

    static {
        Main.getMainRegistration().addSection(
                SecFilter.class,
                "filter %^objects%"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        filtered = (Variable<?>) expressions[0];
        lambda = SkriptFunction.create(this);
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(TriggerContext ctx) {
        Object[] filteredValues = Arrays.stream(filtered.getValues(ctx))
                .filter(v -> lambda.apply(ctx, v)
                        .filter(a -> a.length == 1)
                        .map(a -> a[0])
                        .orElse(false)
                )
                .toArray();
        filtered.change(ctx, filteredValues, ChangeMode.SET);
        return getNext();
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "filter " + filtered.toString(ctx, debug);
    }
}
