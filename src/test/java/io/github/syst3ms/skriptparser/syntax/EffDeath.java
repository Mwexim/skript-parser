package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Kills a {@link SecBirth} section.
 *
 * @name Death
 * @pattern death
 * @since ALPHA
 * @author Mwexim
 */
public class EffDeath extends Effect {
	static {
		Parser.getMainRegistration().addEffect(
				EffDeath.class,
				"death"
		);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		List<CodeSection> currentSections = parseContext.getParserState().getCurrentSections();
		if (currentSections.stream().noneMatch(s -> s instanceof SecBirth)) {
			parseContext.getLogger().error("'death'-statements cannot be used outside of a 'birth'-section", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	public void execute(TriggerContext ctx) {
		SecBirth.addDeath(this);
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "death";
	}
}
