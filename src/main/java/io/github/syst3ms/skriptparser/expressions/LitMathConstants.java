package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;

import java.math.BigDecimal;

/**
 * Math constants pi, e and phi (golden ratio)
 *
 * @name Math Constants
 * @pattern (pi|e|phi)
 * @since ALPHA
 * @author Syst3ms
 */
public class LitMathConstants implements Literal<Number> {
    static {
        Parser.getMainRegistration().addExpression(
                LitMathConstants.class,
                Number.class,
                true,
                "(0:pi|1:e|2:phi)"
        );
    }

    private static final BigDecimal PHI = new BigDecimal(
            "1.61803398874989484820458683436563811772030917980576286213544862270526046281890" +
                    "244970720720418939113748475408807538689175212663386222353693179318006076672635" +
                    "443338908659593958290563832266131992829026788067520876689250171169620703222104" +
                    "321626954862629631361443814975870122034080588795445474924618569536486444924104" +
                    "432077134494704956584678850987433944221254487706647809158846074998871240076521" +
                    "705751797883416625624940758906970400028121042762177111777805315317141011704666" +
                    "599146697987317613560067087480710131795236894275219484353056783002287856997829" +
                    "778347845878228911097625003026961561700250464338243776486102838312683303724292" +
                    "675263116533924731671112115881863851331620384005222165791286675294654906811317" +
                    "159934323597349498509040947621322298101726107059611645629909816290555208524790" +
                    "352406020172799747175342777592778625619432082750513121815628551222480939471234" +
                    "145170223735805772786160086883829523045926478780178899219902707769038953219681" +
                    "986151437803149974110692608867429622675756052317277752035361393621076738937645" +
                    "560606059216589466759551900400555908950229530942312482355212212415444006470340" +
                    "565734797663972394949946584578873039623090375033993856210242369025138680414577" +
                    "995698122445747178034173126453220416397232134044449487302315417676893752103068" +
                    "737880344170093954409627955898678723209512426893557309704509595684401755519881" +
                    "921802064052905518934947592600734852282101088194644544222318891319294689622002"
    );

    private int pattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        pattern = parseContext.getNumericMark();
        return true;
    }

    @Override
    public Number[] getValues() {
        switch (pattern) {
            case 0:
                return new Number[]{BigDecimalMath.pi(BigDecimalMath.DEFAULT_CONTEXT)};
            case 1:
                return new Number[]{BigDecimalMath.e(BigDecimalMath.DEFAULT_CONTEXT)};
            case 2:
                return new Number[]{PHI};
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        switch (pattern) {
            case 0:
                return "pi";
            case 1:
                return "e";
            case 2:
                return "phi";
            default:
                throw new IllegalStateException();
        }
    }
}
