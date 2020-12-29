package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Hashes the given text using the MD5 or SHA-256 algorithms.
 * Each algorithm is suitable for different use cases.
 * <ul>
 * 	<li>MD5 is provided mostly for backwards compatibility, as it is outdated and not secure.</li>
 * 	<li>SHA-256 is more secure, and can used to hash somewhat confidential data like IP addresses and even passwords.</li>
 * </ul>
 * It is not <i>that</i> secure out of the box, so please consider using salt when dealing with passwords.
 * When hashing data, you <strong>must</strong> specify algorithms that will be used for security reasons.
 * Please note that a hash cannot be reversed under normal circumstances.
 * You will not be able to get original value from a hash with Skript.
 *
 * @name Hashed String
 * @pattern %strings% hash[ed] with (MD5|SHA[(-| )]256)
 * @since ALPHA
 * @author ShaneBee, Mwexim
 */
public class ExprHashedString implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
			ExprHashedString.class,
			String.class,
			false,
			"%strings% hash[ed] with (0:MD5|1:SHA[(-| )]256)"
		);
	}

	private Expression<String> value;
	private int type;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		value = (Expression<String>) expressions[0];
		type = parseContext.getParseMark();
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		MessageDigest hash;
		try {
			hash = type == 0 ? MessageDigest.getInstance("MD5") : MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ignored) {
			return new String[0];
		}

		return Arrays.stream(value.getValues(ctx))
				.map(val -> toHex(hash.digest(val.getBytes(StandardCharsets.UTF_8))))
				.toArray(String[]::new);
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return value.toString(ctx, debug) + " hashed with " + (type == 0 ? "MD5" : "SHA-256");
	}

	private static String toHex(final byte[] b) {
		final char[] r = new char[2 * b.length];
		for (int i = 0; i < b.length; i++) {
			r[2 * i] = Character.forDigit((b[i] & 0xF0) >> 4, 16);
			r[2 * i + 1] = Character.forDigit(b[i] & 0x0F, 16);
		}
		return new String(r).toLowerCase();
	}
}
