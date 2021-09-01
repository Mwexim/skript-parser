package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

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

	private SecBirth birth;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		var sections = parseContext.getParserState().getCurrentSections();
		birth = sections.stream()
				.filter(SecBirth.class::isInstance)
				.map(SecBirth.class::cast)
				.findFirst()
				.orElse(null);
		if (birth == null) {
			parseContext.getLogger().error("'death'-statements cannot be used outside of a 'birth'-section", ErrorType.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}

	@Override
	public void execute(TriggerContext ctx) {
		birth.setDead(true);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return "death";
	}
}
