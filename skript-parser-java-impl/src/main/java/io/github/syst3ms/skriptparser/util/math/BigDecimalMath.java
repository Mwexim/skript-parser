package io.github.syst3ms.skriptparser.util.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;


/**
 * BigDecimal special functions.
 * <a href="http://arxiv.org/abs/0908.3030">A Java Math.BigDecimal Implementation of Core Mathematical Functions</a>
 *
 * @author Richard J. Mathar
 * <a href="http://apfloat.org/">apfloat</a>
 * <a href="http://dfp.sourceforge.net/">dfp</a>
 * <a href="http://jscience.org/">JScience</a>
 * @since 2009-05-22
 */
public class BigDecimalMath {

	public static final MathContext DEFAULT_CONTEXT = new MathContext(100, RoundingMode.HALF_UP);
	/**
	 * The base of the natural logarithm in a predefined accuracy.
	 * http://www.cs.arizona.edu/icon/oddsends/e.htm
	 * The precision of the predefined constant is one less than
	 * the string's length, taking into account the decimal dot.
	 * static int E_PRECISION = E.length()-1 ;
	 */
	public static BigDecimal E = new BigDecimal(
		"2.71828182845904523536028747135266249775724709369995957496696762772407663035354" +
		"759457138217852516642742746639193200305992181741359662904357290033429526059563" +
		"073813232862794349076323382988075319525101901157383418793070215408914993488416" +
		"750924476146066808226480016847741185374234544243710753907774499206955170276183" +
		"860626133138458300075204493382656029760673711320070932870912744374704723069697" +
		"720931014169283681902551510865746377211125238978442505695369677078544996996794" +
		"686445490598793163688923009879312773617821542499922957635148220826989519366803" +
		"318252886939849646510582093923982948879332036250944311730123819706841614039701" +
		"983767932068328237646480429531180232878250981945581530175671736133206981125099" +
		"618188159304169035159888851934580727386673858942287922849989208680582574927961" +
		"048419844436346324496848756023362482704197862320900216099023530436994184914631" +
		"409343173814364054625315209618369088870701676839642437814059271456354906130310" +
		"720851038375051011574770417189861068739696552126715468895703503540212340784981" +
		"933432106817012100562788023519303322474501585390473041995777709350366041699732" +
		"972508868769664035557071622684471625607988265178713419512466520103059212366771" +
		"943252786753985589448969709640975459185695638023637016211204774272283648961342" +
		"251644507818244235294863637214174023889344124796357437026375529444833799801612" +
		"549227850925778256209262264832627793338656648162772516401910590049164499828931").round(DEFAULT_CONTEXT);

	/**
	 * Euler's constant Pi.
	 * http://www.cs.arizona.edu/icon/oddsends/pi.htm
	 */
	public static BigDecimal PI = new BigDecimal(
		"3.14159265358979323846264338327950288419716939937510582097494459230781640628620" +
		"899862803482534211706798214808651328230664709384460955058223172535940812848111" +
		"745028410270193852110555964462294895493038196442881097566593344612847564823378" +
		"678316527120190914564856692346034861045432664821339360726024914127372458700660" +
		"631558817488152092096282925409171536436789259036001133053054882046652138414695" +
		"194151160943305727036575959195309218611738193261179310511854807446237996274956" +
		"735188575272489122793818301194912983367336244065664308602139494639522473719070" +
		"217986094370277053921717629317675238467481846766940513200056812714526356082778" +
		"577134275778960917363717872146844090122495343014654958537105079227968925892354" +
		"201995611212902196086403441815981362977477130996051870721134999999837297804995" +
		"105973173281609631859502445945534690830264252230825334468503526193118817101000" +
		"313783875288658753320838142061717766914730359825349042875546873115956286388235" +
		"378759375195778185778053217122680661300192787661119590921642019893809525720106" +
		"548586327886593615338182796823030195203530185296899577362259941389124972177528" +
		"347913151557485724245415069595082953311686172785588907509838175463746493931925" +
		"506040092770167113900984882401285836160356370766010471018194295559619894676783" +
		"744944825537977472684710404753464620804668425906949129331367702898915210475216" +
		"205696602405803815019351125338243003558764024749647326391419927260426992279678" +
		"235478163600934172164121992458631503028618297455570674983850549458858692699569" +
		"092721079750930295532116534498720275596023648066549911988183479775356636980742" +
		"654252786255181841757467289097777279380008164706001614524919217321721477235014").round(DEFAULT_CONTEXT);

	/**
	 * Natural logarithm of 2.
	 * http://www.worldwideschool.org/library/books/sci/math/MiscellaneousMathematicalConstants/chap58.html
	 */
	private static BigDecimal LOG2 = new BigDecimal("0.693147180559945309417232121458176568075" +
													"50013436025525412068000949339362196969471560586332699641868754200148102057068573" +
													"368552023575813055703267075163507596193072757082837143519030703862389167347112335" +
													"011536449795523912047517268157493206515552473413952588295045300709532636664265410" +
													"423915781495204374043038550080194417064167151864471283996817178454695702627163106" +
													"454615025720740248163777338963855069526066834113727387372292895649354702576265209" +
													"885969320196505855476470330679365443254763274495125040606943814710468994650622016" +
													"772042452452961268794654619316517468139267250410380254625965686914419287160829380" +
													"317271436778265487756648508567407764845146443994046142260319309673540257444607030" +
													"809608504748663852313818167675143866747664789088143714198549423151997354880375165" +
													"861275352916610007105355824987941472950929311389715599820565439287170007218085761" +
													"025236889213244971389320378439353088774825970171559107088236836275898425891853530" +
													"243634214367061189236789192372314672321720534016492568727477823445353476481149418" +
													"642386776774406069562657379600867076257199184734022651462837904883062033061144630" +
													"073719489002743643965002580936519443041191150608094879306786515887090060520346842" +
													"973619384128965255653968602219412292420757432175748909770675268711581705113700915" +
													"894266547859596489065305846025866838294002283300538207400567705304678700184162404" +
													"418833232798386349001563121889560650553151272199398332030751408426091479001265168" +
													"243443893572472788205486271552741877243002489794540196187233980860831664811490930" +
													"667519339312890431641370681397776498176974868903887789991296503619270710889264105" +
													"230924783917373501229842420499568935992206602204654941510613").round(DEFAULT_CONTEXT);
	/**
	 * A suggestion for the maximum numter of terms in the Taylor expansion of the exponential.
	 */
	private static int TAYLOR_NTERM = 8;

	/**
	 * Euler's constant.
	 *
	 * @param mc The required precision of the result.
	 * @return 3.14159...
	 * @author Richard J. Mathar
	 * @since 2009-05-29
	 */
	private static BigDecimal pi(final MathContext mc) {
		if (mc.getPrecision() < PI.precision()) {
			return PI.round(mc);
		} else {
			int[] a = {1, 0, 0, -1, -1, -1, 0, 0};
			BigDecimal S = broadhurstBBP(1, 1, a, mc);
			return multiplyRound(S, 8);
		}
	}

	/**
	 * The square root.
	 *
	 * @param x  the non-negative argument.
	 * @param mc The required mathematical precision.
	 * @return the square root of the BigDecimal.
	 * @author Richard J. Mathar
	 * @since 2008-10-27
	 */
	public static BigDecimal sqrt(final BigDecimal x, final MathContext mc) {
		if (x.compareTo(BigDecimal.ZERO) < 0) {
			throw new ArithmeticException("negative argument " + x.toString() + " of square root");
		}
		if (x.abs().subtract(new BigDecimal(Math.pow(10., -mc.getPrecision()))).compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimalMath.scalePrec(BigDecimal.ZERO, mc);
		}
		BigDecimal s = new BigDecimal(Math.sqrt(x.doubleValue()), mc);
		final BigDecimal half = new BigDecimal("2");
		MathContext locmc = new MathContext(mc.getPrecision() + 2, mc.getRoundingMode());
		final double eps = Math.pow(10.0, -mc.getPrecision());
		for (; ; ) {
			if (Math.abs(BigDecimal.ONE.subtract(x.divide(s.pow(2, locmc), locmc)).doubleValue()) < eps) {
				break;
			}
			s = s.add(x.divide(s, locmc)).divide(half, locmc);
		}
		return s;
	}

	/**
	 * The integer root.
	 *
	 * @param n the positive argument.
	 * @param x the non-negative argument.
	 * @return The n-th root of the BigDecimal rounded to the precision implied by x, x^(1/n).
	 * @author Richard J. Mathar
	 * @since 2009-07-30
	 */
	public static BigDecimal root(final int n, final BigDecimal x) {
		boolean negate = false;
		if (x.compareTo(BigDecimal.ZERO) <= 0) {
			if ((n & 1) == 0) {
				throw new ArithmeticException("Non-positive argument " + x.toString() + " for root " + n);
			} else {
				negate = true;
			}
		}
		if (n <= 0) {
			throw new ArithmeticException("negative power " + n + " of root");
		}

		if (n == 1) {
			return x;
		}
		BigDecimal s = new BigDecimal(Math.pow(x.doubleValue(), 1.0 / n));
		final BigDecimal nth = new BigDecimal(n);
		final BigDecimal xhighpr = scalePrec(x, 2);
		MathContext mc = new MathContext(2 + x.precision());
		final double eps = x.ulp().doubleValue() / (2 * n * x.doubleValue());
		for (; ; ) {
			BigDecimal c = xhighpr.divide(s.pow(n - 1), mc);
			c = s.subtract(c);
			MathContext locmc = new MathContext(c.precision());
			c = c.divide(nth, locmc);
			s = s.subtract(c);
			if (Math.abs(c.doubleValue() / s.doubleValue()) < eps) {
				break;
			}
		}
		BigDecimal res = s.round(new MathContext(err2prec(eps)));
		return negate ? res.negate() : res;
	}

	/**
	 * The hypotenuse.
	 *
	 * @param n the first argument.
	 * @param x the second argument.
	 * @return the square root of the sum of the squares of the two arguments, sqrt(n^2+x^2).
	 * @author Richard J. Mathar
	 * @since 2009-08-05
	 */
	private static BigDecimal hypot(final int n, final BigDecimal x) {
		BigDecimal z = (new BigDecimal(n)).pow(2).add(x.pow(2));
		double zerr = x.doubleValue() * x.ulp().doubleValue();
		MathContext mc = new MathContext(2 + err2prec(z.doubleValue(), zerr));
		z = root(2, z.round(mc));
		mc = new MathContext(err2prec(z.doubleValue(), 0.5 * zerr / z.doubleValue()));
		return z.round(mc);
	}

	/**
	 * The exponential function.
	 *
	 * @param x the argument.
	 * @return exp(x).
	 * The precision of the result is implicitly defined by the precision in the argument.
	 * In particular this means that "Invalid Operation" errors are thrown if catastrophic
	 * cancellation of digits causes the result to have no valid digits left.
	 * @author Richard J. Mathar
	 * @since 2009-05-29
	 */
	private static BigDecimal exp(BigDecimal x) {
		if (x.compareTo(BigDecimal.ZERO) < 0) {
			final BigDecimal invx = exp(x.negate());
			MathContext mc = new MathContext(invx.precision());
			return BigDecimal.ONE.divide(invx, mc);
		} else if (x.compareTo(BigDecimal.ZERO) == 0) {
			return scalePrec(BigDecimal.ONE, -(int) (Math.log10(x.ulp().doubleValue())));
		} else {
			final double xDbl = x.doubleValue();
			final double xUlpDbl = x.ulp().doubleValue();
			if (Math.pow(xDbl, TAYLOR_NTERM) < TAYLOR_NTERM * (TAYLOR_NTERM - 1.0) * (TAYLOR_NTERM - 2.0) * xUlpDbl) {
				BigDecimal resul = BigDecimal.ONE;
				BigDecimal xpowi = BigDecimal.ONE;
				BigInteger ifac = BigInteger.ONE;
				MathContext mcTay = new MathContext(err2prec(1., xUlpDbl / TAYLOR_NTERM));
				for (int i = 1; i <= TAYLOR_NTERM; i++) {
					ifac = ifac.multiply(new BigInteger(String.valueOf(i)));
					xpowi = xpowi.multiply(x);
					final BigDecimal c = xpowi.divide(new BigDecimal(ifac), mcTay);
					resul = resul.add(c);
					if (Math.abs(xpowi.doubleValue()) < i && Math.abs(c.doubleValue()) < 0.5 * xUlpDbl) {
						break;
					}
				}
				MathContext mc = new MathContext(err2prec(xUlpDbl / 2.));
				return resul.round(mc);
			} else {
				int exSc = (int) (1.0 -
								  Math.log10(TAYLOR_NTERM * (TAYLOR_NTERM - 1.0) * (TAYLOR_NTERM - 2.0) * xUlpDbl /
											 Math.pow(xDbl, TAYLOR_NTERM)) / (TAYLOR_NTERM - 1.0)
				);
				BigDecimal xby10 = x.scaleByPowerOfTen(-exSc);
				BigDecimal expxby10 = exp(xby10);
				MathContext mc = new MathContext(expxby10.precision() - exSc);
				while (exSc > 0) {
					int exsub = Math.min(8, exSc);
					exSc -= exsub;
					MathContext mctmp = new MathContext(expxby10.precision() - exsub + 2);
					int pex = 1;
					while (exsub-- > 0) pex *= 10;
					expxby10 = expxby10.pow(pex, mctmp);
				}
				return expxby10.round(mc);
			}
		}
	}

	/**
	 * The base of the natural logarithm.
	 *
	 * @param mc the required precision of the result
	 * @return exp(1) = 2.71828....
	 * @author Richard J. Mathar
	 * @since 2009-05-29
	 */
	public static BigDecimal exp(final MathContext mc) {
		if (mc.getPrecision() < E.precision()) {
			return E.round(mc);
		} else {
			BigDecimal uni = scalePrec(BigDecimal.ONE, mc.getPrecision());
			return exp(uni);
		}
	}

	/**
	 * Power function.
	 *
	 * @param x Base of the power.
	 * @param y Exponent of the power.
	 * @return x^y.
	 * The estimation of the relative error in the result is |log(x)*err(y)|+|y*err(x)/x|
	 * @author Richard J. Mathar
	 * @since 2009-06-01
	 */
	static public BigDecimal pow(final BigDecimal x, final BigDecimal y) {
		if (x.compareTo(BigDecimal.ZERO) < 0) {
			throw new ArithmeticException("Cannot power negative " + x.toString());
		} else if (x.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		} else {
			BigDecimal logx = log(x);
			BigDecimal ylogx = y.multiply(logx);
			BigDecimal resul = exp(ylogx);
			double errR = Math.abs(logx.doubleValue() * y.ulp().doubleValue() / 2.) +
						  Math.abs(y.doubleValue() * x.ulp().doubleValue() / 2. / x.doubleValue());
			MathContext mcR = new MathContext(err2prec(1.0, errR));
			return resul.round(mcR);
		}
	}

	/**
	 * The natural logarithm.
	 *
	 * @param x the argument.
	 * @return ln(x).
	 * The precision of the result is implicitly defined by the precision in the argument.
	 * @author Richard J. Mathar
	 * @since 2009-05-29
	 */
	private static BigDecimal log(BigDecimal x) {
		if (x.compareTo(BigDecimal.ZERO) < 0) {
			throw new ArithmeticException("Cannot take log of negative " + x.toString());
		} else if (x.compareTo(BigDecimal.ONE) == 0) {
			return scalePrec(BigDecimal.ZERO, x.precision() - 1);
		} else if (Math.abs(x.doubleValue() - 1.0) <= 0.3) {
			BigDecimal z = scalePrec(x.subtract(BigDecimal.ONE), 2);
			BigDecimal zpown = z;
			double eps = 0.5 * x.ulp().doubleValue() / Math.abs(x.doubleValue());
			BigDecimal resul = z;
			for (int k = 2; ; k++) {
				zpown = multiplyRound(zpown, z);
				BigDecimal c = divideRound(zpown, k);
				if (k % 2 == 0) {
					resul = resul.subtract(c);
				} else {
					resul = resul.add(c);
				}
				if (Math.abs(c.doubleValue()) < eps) {
					break;
				}
			}
			MathContext mc = new MathContext(err2prec(resul.doubleValue(), eps));
			return resul.round(mc);
		} else {
			final double xDbl = x.doubleValue();
			final double xUlpDbl = x.ulp().doubleValue();
			int r = (int) (Math.log(xDbl) / 0.2);
			r = Math.max(2, r);
			BigDecimal xhighpr = scalePrec(x, 2);
			BigDecimal resul = root(r, xhighpr);
			resul = log(resul).multiply(new BigDecimal(r));
			MathContext mc = new MathContext(err2prec(resul.doubleValue(), xUlpDbl / xDbl));
			return resul.round(mc);
		}
	}

	/**
	 * The natural logarithm.
	 *
	 * @param n  The main argument, a strictly positive integer.
	 * @param mc The requirements on the precision.
	 * @return ln(n).
	 * @author Richard J. Mathar
	 * @since 2009-08-08
	 */
	private static BigDecimal log(int n, final MathContext mc) {
		if (n <= 0) {
			throw new ArithmeticException("Cannot take log of negative " + n);
		} else if (n == 1) {
			return BigDecimal.ZERO;
		} else if (n == 2) {
			if (mc.getPrecision() < LOG2.precision()) {
				return LOG2.round(mc);
			} else {
				int[] a = {2, -5, -2, -7, -2, -5, 2, -3};
				BigDecimal S = broadhurstBBP(2, 1, a, new MathContext(1 + mc.getPrecision()));
				S = S.multiply(new BigDecimal(8));
				S = root(2, divideRound(S, 3));
				return S.round(mc);
			}
		} else if (n == 3) {
			int kmax = (int) (mc.getPrecision() / 1.87);
			MathContext mcloc = new MathContext(mc.getPrecision() + 1 + (int) (Math.log10(kmax * 0.693 / 1.098)));
			BigDecimal log3 = multiplyRound(log(2, mcloc), 19);
			double eps = prec2err(1.098, mc.getPrecision()) / kmax;
			Rational r = new Rational(7153, 524288);
			Rational pk = new Rational(7153, 524288);
			for (int k = 1; ; k++) {
				Rational tmp = pk.divide(k);
				if (tmp.doubleValue() < eps) {
					break;
				}
				mcloc = new MathContext(err2prec(tmp.doubleValue(), eps));
				BigDecimal c = pk.divide(k).BigDecimalValue(mcloc);
				if (k % 2 != 0) {
					log3 = log3.add(c);
				} else {
					log3 = log3.subtract(c);
				}
				pk = pk.multiply(r);
			}
			log3 = divideRound(log3, 12);
			return log3.round(mc);
		} else if (n == 5) {
			int kmax = (int) (mc.getPrecision() / 1.33);
			MathContext mcloc = new MathContext(mc.getPrecision() + 1 + (int) (Math.log10(kmax * 0.693 / 1.609)));
			BigDecimal log5 = multiplyRound(log(2, mcloc), 14);
			double eps = prec2err(1.6, mc.getPrecision()) / kmax;
			Rational r = new Rational(759, 16384);
			Rational pk = new Rational(759, 16384);
			for (int k = 1; ; k++) {
				Rational tmp = pk.divide(k);
				if (tmp.doubleValue() < eps) {
					break;
				}
				mcloc = new MathContext(err2prec(tmp.doubleValue(), eps));
				BigDecimal c = pk.divide(k).BigDecimalValue(mcloc);
				log5 = log5.subtract(c);
				pk = pk.multiply(r);
			}
			log5 = divideRound(log5, 6);
			return log5.round(mc);
		} else if (n == 7) {
			int kmax = (int) (mc.getPrecision() / 0.903);
			MathContext mcloc = new MathContext(mc.getPrecision() + 1 + (int) (Math.log10(kmax * 3 * 0.693 / 1.098)));
			BigDecimal log7 = multiplyRound(log(2, mcloc), 3);
			double eps = prec2err(1.9, mc.getPrecision()) / kmax;
			Rational r = new Rational(1, 8);
			Rational pk = new Rational(1, 8);
			for (int k = 1; ; k++) {
				Rational tmp = pk.divide(k);
				if (tmp.doubleValue() < eps) {
					break;
				}
				mcloc = new MathContext(err2prec(tmp.doubleValue(), eps));
				BigDecimal c = pk.divide(k).BigDecimalValue(mcloc);
				log7 = log7.subtract(c);
				pk = pk.multiply(r);
			}
			return log7.round(mc);

		} else {
			double res = Math.log((double) n);
			double eps = prec2err(res, mc.getPrecision());
			eps *= n;
			final MathContext mcloc = new MathContext(1 + err2prec((double) n, eps));
			BigDecimal nb = scalePrec(new BigDecimal(n), mcloc);
			return log(nb);
		}
	}

	/**
	 * The natural logarithm.
	 *
	 * @param r  The main argument, a strictly positive value.
	 * @param mc The requirements on the precision.
	 * @return ln(r).
	 * @author Richard J. Mathar
	 * @since 2009-08-09
	 */
	public static BigDecimal log(final BigDecimal r, final MathContext mc) {
		if (r.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ArithmeticException("Cannot take log of negative " + r.toString());
		} else if (r.compareTo(BigDecimal.ONE) == 0) {
			return BigDecimal.ZERO;
		} else {
			double eps = prec2err(Math.log(r.doubleValue()), mc.getPrecision());

			final BigDecimal result = log(r.setScale(1 + err2prec(eps), mc.getRoundingMode()));

			return result.round(mc);
		}
	}

	/**
	 * Raise to an integer power and round.
	 *
	 * @param x The base.
	 * @param n The exponent.
	 * @return x^n.
	 * @author Richard J. Mathar
	 * @since 2009-08-13
	 * @since 2010-05-26 handle also cases where n is less than zero.
	 */
	private static BigDecimal powRound(final BigDecimal x, final int n) {
		/** Special cases: x^1=x and x^0 = 1
		 */
		if (n == 1) {
			return x;
		} else if (n == 0) {
			return BigDecimal.ONE;
		} else {
			MathContext mc = new MathContext(x.precision() - (int) Math.log10((double) (Math.abs(n))));
			if (n > 0) {
				return x.pow(n, mc);
			} else {
				return BigDecimal.ONE.divide(x.pow(-n), mc);
			}
		}
	}

	/**
	 * Trigonometric sine.
	 *
	 * @param x The argument in radians.
	 * @return sin(x) in the range -1 to 1.
	 * @author Richard J. Mathar
	 * @since 2009-06-01
	 */
	public static BigDecimal sin(final BigDecimal x) {
		if (x.compareTo(BigDecimal.ZERO) < 0) {
			return sin(x.negate()).negate();
		} else if (x.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		} else {
			BigDecimal res = mod2pi(x);
			double errpi = 0.5 * Math.abs(x.ulp().doubleValue());
			MathContext mc = new MathContext(2 + err2prec(3.14159, errpi));
			BigDecimal p = pi(mc);
			mc = new MathContext(x.precision());
			if (res.compareTo(p) > 0) {
				return sin(subtractRound(res, p)).negate();
			} else if (res.multiply(new BigDecimal("2")).compareTo(p) > 0) {
				return sin(subtractRound(p, res));
			} else {
				if (res.multiply(new BigDecimal("4")).compareTo(p) > 0) {
					return cos(subtractRound(p.divide(new BigDecimal("2")), res));
				} else {
					BigDecimal resul = res;
					BigDecimal xpowi = res;
					BigInteger ifac = BigInteger.ONE;
					double xUlpDbl = res.ulp().doubleValue();
					int k = (int) (res.precision() / Math.log10(1.0 / res.doubleValue())) / 2;
					MathContext mcTay = new MathContext(err2prec(res.doubleValue(), xUlpDbl / k));
					for (int i = 1; ; i++) {
						ifac = ifac.multiply(new BigInteger(String.valueOf(2 * i)));
						ifac = ifac.multiply(new BigInteger(String.valueOf(2 * i + 1)));
						xpowi = xpowi.multiply(res).multiply(res).negate();
						BigDecimal corr = xpowi.divide(new BigDecimal(ifac), mcTay);
						resul = resul.add(corr);
						if (corr.abs().doubleValue() < 0.5 * xUlpDbl) {
							break;
						}
					}
					mc = new MathContext(res.precision());
					return resul.round(mc);
				}
			}
		}
	}

	/**
	 * Trigonometric cosine.
	 *
	 * @param x The argument in radians.
	 * @return cos(x) in the range -1 to 1.
	 * @author Richard J. Mathar
	 * @since 2009-06-01
	 */
	public static BigDecimal cos(final BigDecimal x) {
		if (x.compareTo(BigDecimal.ZERO) < 0) {
			return cos(x.negate());
		} else if (x.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ONE;
		} else {
			BigDecimal res = mod2pi(x);
			double errpi = 0.5 * Math.abs(x.ulp().doubleValue());
			MathContext mc = new MathContext(2 + err2prec(3.14159, errpi));
			BigDecimal p = pi(mc);
			mc = new MathContext(x.precision());
			if (res.compareTo(p) > 0) {
				return cos(subtractRound(res, p)).negate();
			} else if (res.multiply(new BigDecimal("2")).compareTo(p) > 0) {
				return cos(subtractRound(p, res)).negate();
			} else {
				if (res.multiply(new BigDecimal("4")).compareTo(p) > 0) {
					return sin(subtractRound(p.divide(new BigDecimal("2")), res));
				} else {
					BigDecimal resul = BigDecimal.ONE;
					BigDecimal xpowi = BigDecimal.ONE;
					BigInteger ifac = BigInteger.ONE;
					double xUlpDbl = 0.5 * res.ulp().doubleValue() * res.doubleValue();
					int k = (int) (Math.log(xUlpDbl) / Math.log(res.doubleValue())) / 2;
					MathContext mcTay = new MathContext(err2prec(1., xUlpDbl / k));
					for (int i = 1; ; i++) {
						ifac = ifac.multiply(new BigInteger(String.valueOf(2 * i - 1)));
						ifac = ifac.multiply(new BigInteger(String.valueOf(2 * i)));
						xpowi = xpowi.multiply(res).multiply(res).negate();
						BigDecimal corr = xpowi.divide(new BigDecimal(ifac), mcTay);
						resul = resul.add(corr);
						if (corr.abs().doubleValue() < 0.5 * xUlpDbl) {
							break;
						}
					}
					mc = new MathContext(err2prec(resul.doubleValue(), xUlpDbl));
					return resul.round(mc);
				}
			}
		}
	}

	/**
	 * The trigonometric tangent.
	 *
	 * @param x the argument in radians.
	 * @return the tan(x)
	 * @author Richard J. Mathar
	 */
	public static BigDecimal tan(final BigDecimal x) {
		if (x.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		} else if (x.compareTo(BigDecimal.ZERO) < 0) {
			return tan(x.negate()).negate();
		} else {
			BigDecimal res = modpi(x);
			final double xDbl = res.doubleValue();
			final double xUlpDbl = x.ulp().doubleValue() / 2.;
			final double eps = xUlpDbl / 2. / Math.pow(Math.cos(xDbl), 2.);

			if (xDbl > 0.8) {
				BigDecimal co = cot(x);
				MathContext mc = new MathContext(err2prec(1. / co.doubleValue(), eps));
				return BigDecimal.ONE.divide(co, mc);
			} else {
				final BigDecimal xhighpr = scalePrec(res, 2);
				final BigDecimal xhighprSq = multiplyRound(xhighpr, xhighpr);

				BigDecimal resul = xhighpr.plus();
				BigDecimal xpowi = xhighpr;

				Bernoulli b = new Bernoulli();
				BigInteger fourn = new BigInteger("4");
				BigInteger fac = new BigInteger("2");

				for (int i = 2; ; i++) {
					Rational f = b.at(2 * i).abs();
					fourn = fourn.shiftLeft(2);
					fac = fac.multiply(new BigInteger(String.valueOf(2 * i)))
							 .multiply(new BigInteger(String.valueOf(2 * i - 1)));
					f = f.multiply(fourn).multiply(fourn.subtract(BigInteger.ONE)).divide(fac);
					xpowi = multiplyRound(xpowi, xhighprSq);
					BigDecimal c = multiplyRound(xpowi, f);
					resul = resul.add(c);
					if (Math.abs(c.doubleValue()) < 0.1 * eps) {
						break;
					}
				}
				MathContext mc = new MathContext(err2prec(resul.doubleValue(), eps));
				return resul.round(mc);
			}
		}
	}

	/**
	 * The trigonometric co-tangent.
	 *
	 * @param x the argument in radians.
	 * @return the cot(x)
	 * @author Richard J. Mathar
	 * @since 2009-07-31
	 */
	private static BigDecimal cot(final BigDecimal x) {
		if (x.compareTo(BigDecimal.ZERO) == 0) {
			throw new ArithmeticException("Cannot take cot of zero " + x.toString());
		} else if (x.compareTo(BigDecimal.ZERO) < 0) {
			return cot(x.negate()).negate();
		} else {
			BigDecimal res = modpi(x);
			final double xDbl = res.doubleValue();
			final double xUlpDbl = x.ulp().doubleValue() / 2.;
			final double eps = xUlpDbl / 2. / Math.pow(Math.sin(xDbl), 2.);

			final BigDecimal xhighpr = scalePrec(res, 2);
			final BigDecimal xhighprSq = multiplyRound(xhighpr, xhighpr);

			MathContext mc = new MathContext(err2prec(xhighpr.doubleValue(), eps));
			BigDecimal resul = BigDecimal.ONE.divide(xhighpr, mc);
			BigDecimal xpowi = xhighpr;

			Bernoulli b = new Bernoulli();
			BigInteger fourn = new BigInteger("4");
			BigInteger fac = BigInteger.ONE;

			for (int i = 1; ; i++) {
				Rational f = b.at(2 * i);
				fac = fac.multiply(new BigInteger(String.valueOf(2 * i)))
						 .multiply(new BigInteger(String.valueOf(2 * i - 1)));
				f = f.multiply(fourn).divide(fac);
				BigDecimal c = multiplyRound(xpowi, f);
				if (i % 2 == 0) {
					resul = resul.add(c);
				} else {
					resul = resul.subtract(c);
				}
				if (Math.abs(c.doubleValue()) < 0.1 * eps) {
					break;
				}

				fourn = fourn.shiftLeft(2);
				xpowi = multiplyRound(xpowi, xhighprSq);
			}
			mc = new MathContext(err2prec(resul.doubleValue(), eps));
			return resul.round(mc);
		}
	}

	/**
	 * The inverse trigonometric sine.
	 *
	 * @param x the argument.
	 * @return the arcsin(x) in radians.
	 * @author Richard J. Mathar
	 */
	public static BigDecimal asin(final BigDecimal x) {
		if (x.compareTo(BigDecimal.ONE) > 0 || x.compareTo(BigDecimal.ONE.negate()) < 0) {
			throw new ArithmeticException("Out of range argument " + x.toString() + " of asin");
		} else if (x.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		} else if (x.compareTo(BigDecimal.ONE) == 0) {
			double errpi = Math.sqrt(x.ulp().doubleValue());
			MathContext mc = new MathContext(err2prec(3.14159, errpi));
			return pi(mc).divide(new BigDecimal(2));
		} else if (x.compareTo(BigDecimal.ZERO) < 0) {
			return asin(x.negate()).negate();
		} else if (x.doubleValue() > 0.7) {
			final BigDecimal xCompl = BigDecimal.ONE.subtract(x);
			final double xDbl = x.doubleValue();
			final double xUlpDbl = x.ulp().doubleValue() / 2.;
			final double eps = xUlpDbl / 2. / Math.sqrt(1. - Math.pow(xDbl, 2.));

			final BigDecimal xhighpr = scalePrec(xCompl, 3);
			final BigDecimal xhighprV = divideRound(xhighpr, 4);

			BigDecimal resul = BigDecimal.ONE;
			BigDecimal xpowi = BigDecimal.ONE;
			BigInteger ifacN = BigInteger.ONE;
			BigInteger ifacD = BigInteger.ONE;

			for (int i = 1; ; i++) {
				ifacN = ifacN.multiply(new BigInteger(String.valueOf(2 * i - 1)));
				ifacD = ifacD.multiply(new BigInteger(String.valueOf(i)));
				if (i == 1) {
					xpowi = xhighprV;
				} else {
					xpowi = multiplyRound(xpowi, xhighprV);
				}
				BigDecimal c = divideRound(multiplyRound(xpowi, ifacN),
										   ifacD.multiply(new BigInteger(String.valueOf(2 * i + 1)))
				);
				resul = resul.add(c);
				if (Math.abs(c.doubleValue()) < xUlpDbl / 120.) {
					break;
				}
			}
			xpowi = root(2, xhighpr.multiply(new BigDecimal(2)));
			resul = multiplyRound(xpowi, resul);

			MathContext mc = new MathContext(resul.precision());
			BigDecimal pihalf = pi(mc).divide(new BigDecimal(2));

			mc = new MathContext(err2prec(resul.doubleValue(), eps));
			return pihalf.subtract(resul, mc);
		} else {
			final double xDbl = x.doubleValue();
			final double xUlpDbl = x.ulp().doubleValue() / 2.;
			final double eps = xUlpDbl / 2. / Math.sqrt(1. - Math.pow(xDbl, 2.));

			final BigDecimal xhighpr = scalePrec(x, 2);
			final BigDecimal xhighprSq = multiplyRound(xhighpr, xhighpr);

			BigDecimal resul = xhighpr.plus();
			BigDecimal xpowi = xhighpr;
			BigInteger ifacN = BigInteger.ONE;
			BigInteger ifacD = BigInteger.ONE;

			for (int i = 1; ; i++) {
				ifacN = ifacN.multiply(new BigInteger(String.valueOf(2 * i - 1)));
				ifacD = ifacD.multiply(new BigInteger(String.valueOf(2 * i)));
				xpowi = multiplyRound(xpowi, xhighprSq);
				BigDecimal c = divideRound(multiplyRound(xpowi, ifacN),
										   ifacD.multiply(new BigInteger(String.valueOf(2 * i + 1)))
				);
				resul = resul.add(c);
				if (Math.abs(c.doubleValue()) < 0.1 * eps) {
					break;
				}
			}
			MathContext mc = new MathContext(err2prec(resul.doubleValue(), eps));
			return resul.round(mc);
		}
	}

	/**
	 * The inverse trigonometric cosine.
	 *
	 * @param x the argument.
	 * @return the arccos(x) in radians.
	 * @author Richard J. Mathar
	 * @since 2009-09-29
	 */
	public static BigDecimal acos(final BigDecimal x) {
		final BigDecimal xhighpr = scalePrec(x, 2);
		BigDecimal resul = asin(xhighpr);
		double eps = resul.ulp().doubleValue() / 2.;

		MathContext mc = new MathContext(err2prec(3.14159, eps));
		BigDecimal pihalf = pi(mc).divide(new BigDecimal(2));
		resul = pihalf.subtract(resul);
		final double xDbl = x.doubleValue();
		final double xUlpDbl = x.ulp().doubleValue() / 2.;
		eps = xUlpDbl / 2. / Math.sqrt(1. - Math.pow(xDbl, 2.));

		mc = new MathContext(err2prec(resul.doubleValue(), eps));
		return resul.round(mc);

	}

	/**
	 * The inverse trigonometric tangent.
	 *
	 * @param x the argument.
	 * @return the principal value of arctan(x) in radians in the range -pi/2 to +pi/2.
	 * @author Richard J. Mathar
	 * @since 2009-08-03
	 */
	public static BigDecimal atan(final BigDecimal x) {
		if (x.compareTo(BigDecimal.ZERO) < 0) {
			return atan(x.negate()).negate();
		} else if (x.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		} else if (x.doubleValue() > 0.7 && x.doubleValue() < 3.0) {
			BigDecimal y = scalePrec(x, 2);
			BigDecimal newx = divideRound(hypot(1, y).subtract(BigDecimal.ONE), y);
			BigDecimal resul = multiplyRound(atan(newx), 2);
			double eps = x.ulp().doubleValue() / (2.0 * Math.hypot(1.0, x.doubleValue()));
			MathContext mc = new MathContext(err2prec(resul.doubleValue(), eps));
			return resul.round(mc);
		} else if (x.doubleValue() < 0.71) {

			final BigDecimal xhighpr = scalePrec(x, 2);
			final BigDecimal xhighprSq = multiplyRound(xhighpr, xhighpr).negate();

			BigDecimal resul = xhighpr.plus();
			BigDecimal xpowi = xhighpr;
			double eps = x.ulp().doubleValue() / (2.0 * Math.hypot(1.0, x.doubleValue()));

			for (int i = 1; ; i++) {
				xpowi = multiplyRound(xpowi, xhighprSq);
				BigDecimal c = divideRound(xpowi, 2 * i + 1);

				resul = resul.add(c);
				if (Math.abs(c.doubleValue()) < 0.1 * eps) {
					break;
				}
			}
			MathContext mc = new MathContext(err2prec(resul.doubleValue(), eps));
			return resul.round(mc);
		} else {
			double eps = x.ulp().doubleValue() / (2.0 * Math.hypot(1.0, x.doubleValue()));
			MathContext mc = new MathContext(2 + err2prec(3.1416, eps));
			BigDecimal onepi = pi(mc);
			BigDecimal resul = onepi.divide(new BigDecimal(2));

			final BigDecimal xhighpr = divideRound(-1, scalePrec(x, 2));
			final BigDecimal xhighprSq = multiplyRound(xhighpr, xhighpr).negate();
			BigDecimal xpowi = xhighpr;

			for (int i = 0; ; i++) {
				BigDecimal c = divideRound(xpowi, 2 * i + 1);

				resul = resul.add(c);
				if (Math.abs(c.doubleValue()) < 0.1 * eps) {
					break;
				}
				xpowi = multiplyRound(xpowi, xhighprSq);
			}
			mc = new MathContext(err2prec(resul.doubleValue(), eps));
			return resul.round(mc);
		}
	}

	/**
	 * Reduce value to the interval [0,2*Pi].
	 *
	 * @param x the original value
	 * @return the value modulo 2*pi in the interval from 0 to 2*pi.
	 * @author Richard J. Mathar
	 * @since 2009-06-01
	 */
	private static BigDecimal mod2pi(BigDecimal x) {
		int k = (int) (0.5 * x.doubleValue() / Math.PI);
		double err2pi;
		if (k != 0) {
			err2pi = 0.25 * Math.abs(x.ulp().doubleValue() / k);
		} else {
			err2pi = 0.5 * Math.abs(x.ulp().doubleValue());
		}
		MathContext mc = new MathContext(2 + err2prec(6.283, err2pi));
		BigDecimal twopi = pi(mc).multiply(new BigDecimal(2));
		BigDecimal res = x.remainder(twopi);
		if (res.compareTo(BigDecimal.ZERO) < 0) {
			res = res.add(twopi);
		}
		mc = new MathContext(err2prec(res.doubleValue(), x.ulp().doubleValue() / 2.));
		return res.round(mc);
	}

	/**
	 * Reduce value to the interval [-Pi/2,Pi/2].
	 *
	 * @param x The original value
	 * @return The value modulo pi, shifted to the interval from -Pi/2 to Pi/2.
	 * @author Richard J. Mathar
	 * @since 2009-07-31
	 */
	private static BigDecimal modpi(BigDecimal x) {
		int k = (int) (x.doubleValue() / Math.PI);
		double errpi;
		if (k != 0) {
			errpi = 0.5 * Math.abs(x.ulp().doubleValue() / k);
		} else {
			errpi = 0.5 * Math.abs(x.ulp().doubleValue());
		}
		MathContext mc = new MathContext(2 + err2prec(3.1416, errpi));
		BigDecimal onepi = pi(mc);
		BigDecimal pihalf = onepi.divide(new BigDecimal(2));
		BigDecimal res = x.remainder(onepi);
		if (res.compareTo(pihalf) > 0) {
			res = res.subtract(onepi);
		} else if (res.compareTo(pihalf.negate()) < 0) {
			res = res.add(onepi);
		}
		mc = new MathContext(err2prec(res.doubleValue(), x.ulp().doubleValue() / 2.));
		return res.round(mc);
	}


	/**
	 * Broadhurst ladder sequence.
	 *
	 * @param n
	 * @param p
	 * @param mc Specification of the accuracy of the result
	 * @return S_(n, p)(a)
	 * @author Richard J. Mathar
	 * @since 2009-08-09
	 * <a href="http://arxiv.org/abs/math/9803067">arXiv:math/9803067</a>
	 */
	private static BigDecimal broadhurstBBP(final int n, final int p, final int a[], MathContext mc) {
		double x = 0.0;
		for (int k = 1; k < 10; k++)
			x += a[(k - 1) % 8] / Math.pow(2., p * (k + 1) / 2) / Math.pow((double) k, n);
		double eps = prec2err(x, mc.getPrecision());
		int kmax = (int) (6.6 * mc.getPrecision() / p);
		eps /= kmax;
		BigDecimal res = BigDecimal.ZERO;
		for (int c = 0; ; c++) {
			Rational r = new Rational();
			for (int k = 0; k < 8; k++) {
				Rational tmp = new Rational(new BigInteger(String.valueOf(a[k])),
											(new BigInteger(String.valueOf(1 + 8 * c + k))).pow(n)
				);
				int pk1h = p * (2 + 8 * c + k) / 2;
				tmp = tmp.divide(BigInteger.ONE.shiftLeft(pk1h));
				r = r.add(tmp);
			}

			if (Math.abs(r.doubleValue()) < eps) {
				break;
			}
			MathContext mcloc = new MathContext(1 + err2prec(r.doubleValue(), eps));
			res = res.add(r.BigDecimalValue(mcloc));
		}
		return res.round(mc);
	}


	/**
	 * Add a BigDecimal and a BigInteger.
	 *
	 * @param x The left summand
	 * @param y The right summand
	 * @return The sum x+y.
	 * @author Richard J. Mathar
	 * @since 2012-03-02
	 */
	public static BigDecimal add(final BigDecimal x, final BigInteger y) {
		return x.add(new BigDecimal(y));
	}


	/**
	 * Subtract and round according to the larger of the two ulp's.
	 *
	 * @param x The left term.
	 * @param y The right term.
	 * @return The difference x-y.
	 * @since 2009-07-30
	 */
	private static BigDecimal subtractRound(final BigDecimal x, final BigDecimal y) {
		BigDecimal resul = x.subtract(y);
		double errR = Math.abs(y.ulp().doubleValue() / 2.) + Math.abs(x.ulp().doubleValue() / 2.);
		MathContext mc = new MathContext(err2prec(resul.doubleValue(), errR));
		return resul.round(mc);
	}

	/**
	 * Multiply and round.
	 *
	 * @param x The left factor.
	 * @param y The right factor.
	 * @return The product x*y.
	 * @author Richard J. Mathar
	 * @since 2009-07-30
	 */
	private static BigDecimal multiplyRound(final BigDecimal x, final BigDecimal y) {
		BigDecimal resul = x.multiply(y);
		MathContext mc = new MathContext(Math.min(x.precision(), y.precision()));
		return resul.round(mc);
	}

	/**
	 * Multiply and round.
	 *
	 * @param x The left factor.
	 * @param f The right factor.
	 * @return The product x*f.
	 * @author Richard J. Mathar
	 * @since 2009-07-30
	 */
	private static BigDecimal multiplyRound(final BigDecimal x, final Rational f) {
		if (f.compareTo(BigInteger.ZERO) == 0) {
			return BigDecimal.ZERO;
		} else {
			MathContext mc = new MathContext(2 + x.precision());
			BigDecimal fbd = f.BigDecimalValue(mc);
			return multiplyRound(x, fbd);
		}
	}

	/**
	 * Multiply and round.
	 *
	 * @param x The left factor.
	 * @param n The right factor.
	 * @return The product x*n.
	 * @author Richard J. Mathar
	 * @since 2009-07-30
	 */
	private static BigDecimal multiplyRound(final BigDecimal x, final int n) {
		BigDecimal resul = x.multiply(new BigDecimal(n));
		MathContext mc = new MathContext(n != 0 ? x.precision() : 0);
		return resul.round(mc);
	}

	/**
	 * Multiply and round.
	 *
	 * @param x The left factor.
	 * @param n The right factor.
	 * @return the product x*n
	 * @author Richard J. Mathar
	 * @since 2009-07-30
	 */
	private static BigDecimal multiplyRound(final BigDecimal x, final BigInteger n) {
		BigDecimal resul = x.multiply(new BigDecimal(n));
		MathContext mc = new MathContext(n.compareTo(BigInteger.ZERO) != 0 ? x.precision() : 0);
		return resul.round(mc);
	}

	/**
	 * Divide and round.
	 *
	 * @param x The numerator
	 * @param y The denominator
	 * @return the divided x/y
	 * @author Richard J. Mathar
	 * @since 2009-07-30
	 */
	private static BigDecimal divideRound(final BigDecimal x, final BigDecimal y) {
		MathContext mc = new MathContext(Math.min(x.precision(), y.precision()));
		BigDecimal resul = x.divide(y, mc);
		return scalePrec(resul, mc);
	}

	/**
	 * Divide and round.
	 *
	 * @param x The numerator
	 * @param n The denominator
	 * @return the divided x/n
	 * @author Richard J. Mathar
	 * @since 2009-07-30
	 */
	private static BigDecimal divideRound(final BigDecimal x, final int n) {
		MathContext mc = new MathContext(x.precision());
		return x.divide(new BigDecimal(n), mc);
	}

	/**
	 * Divide and round.
	 *
	 * @param x The numerator
	 * @param n The denominator
	 * @return the divided x/n
	 * @author Richard J. Mathar
	 * @since 2009-07-30
	 */
	private static BigDecimal divideRound(final BigDecimal x, final BigInteger n) {
		MathContext mc = new MathContext(x.precision());
		return x.divide(new BigDecimal(n), mc);
	}

	/**
	 * Divide and round.
	 *
	 * @param n The numerator.
	 * @param x The denominator.
	 * @return the divided n/x.
	 * @author Richard J. Mathar
	 * @since 2009-08-05
	 */
	private static BigDecimal divideRound(final int n, final BigDecimal x) {
		MathContext mc = new MathContext(x.precision());
		return new BigDecimal(n).divide(x, mc);
	}

	/**
	 * Append decimal zeros to the value. This returns a value which appears to have
	 * a higher precision than the input.
	 *
	 * @param x The input value
	 * @param d The (positive) value of zeros to be added as least significant digits.
	 * @return The same value as the input but with increased (pseudo) precision.
	 * @author Richard J. Mathar
	 */
	private static BigDecimal scalePrec(final BigDecimal x, int d) {
		return x.setScale(d + x.scale());
	}

	/**
	 * Boost the precision by appending decimal zeros to the value. This returns a value which appears to have
	 * a higher precision than the input.
	 *
	 * @param x  The input value
	 * @param mc The requirement on the minimum precision on return.
	 * @return The same value as the input but with increased (pseudo) precision.
	 * @author Richard J. Mathar
	 */
	public static BigDecimal scalePrec(final BigDecimal x, final MathContext mc) {
		final int diffPr = mc.getPrecision() - x.precision();
		if (diffPr > 0) {
			return scalePrec(x, diffPr);
		} else {
			return x;
		}
	}

	/**
	 * Convert an absolute error to a precision.
	 *
	 * @param x    The value of the variable
	 *             The value returned depends only on the absolute value, not on the sign.
	 * @param xerr The absolute error in the variable
	 *             The value returned depends only on the absolute value, not on the sign.
	 * @return The number of valid digits in x.
	 * Derived from the representation x+- xerr, as if the error was represented
	 * in a "half width" (half of the error bar) form.
	 * The value is rounded down, and on the pessimistic side for that reason.
	 * @author Richard J. Mathar
	 * @since 2009-05-30
	 */
	private static int err2prec(double x, double xerr) {
		return 1 + (int) (Math.log10(Math.abs(0.5 * x / xerr)));
	}

	/**
	 * Convert a relative error to a precision.
	 *
	 * @param xerr The relative error in the variable.
	 *             The value returned depends only on the absolute value, not on the sign.
	 * @return The number of valid digits in x.
	 * The value is rounded down, and on the pessimistic side for that reason.
	 * @author Richard J. Mathar
	 * @since 2009-08-05
	 */
	private static int err2prec(double xerr) {
		return 1 + (int) (Math.log10(Math.abs(0.5 / xerr)));
	}

	/**
	 * Convert a precision (relative error) to an absolute error.
	 * The is the inverse functionality of err2prec().
	 *
	 * @param x    The value of the variable
	 *             The value returned depends only on the absolute value, not on the sign.
	 * @param prec The number of valid digits of the variable.
	 * @return the absolute error in x.
	 * Derived from the an accuracy of one half of the ulp.
	 * @author Richard J. Mathar
	 * @since 2009-08-09
	 */
	private static double prec2err(final double x, final int prec) {
		return 5. * Math.abs(x) * Math.pow(10., -prec);
	}

	public static BigDecimal getBigDecimal(Number n) {
		return n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal(n.toString());
	}

	public static BigInteger getBigInteger(Number n) {
		return n instanceof BigInteger ? (BigInteger) n : BigInteger.valueOf(n.longValue());
	}
}
