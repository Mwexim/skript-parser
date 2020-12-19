package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.registration.DefaultRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.FileUtils;
import io.github.syst3ms.skriptparser.util.SkriptDate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.time.ZoneOffset;

public class TestRegistration {

	public static void register() {
		try {
			Field regField = Parser.class.getDeclaredField("registration");
			regField.setAccessible(true);
			regField.set(null, new SkriptRegistration(new TestAddon()));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		DefaultRegistration.register();
		try {
			FileUtils.loadClasses(
				Path.of("build/classes/java/main"),
				"io.github.syst3ms.skriptparser",
				"effects",
				"expressions",
				"lang",
				"sections",
				"tags"
			);
			FileUtils.loadClasses(
				Path.of("build/classes/java/test"),
				"io.github.syst3ms.skriptparser",
				"syntax"
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Parser.getMainRegistration().register();

		// Now, we need to change some fields to keep all tests consistent.
		fields();
	}

	public static void fields() {
		// ZONE_ID field: all tests need to be executed from the same zone (UTC)
		try {
			Field zoneId = SkriptDate.class.getDeclaredField("ZONE_ID");
			zoneId.setAccessible(true);
			removeModifiers(zoneId, Modifier.FINAL);
			zoneId.set(null, ZoneOffset.UTC);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static void removeModifiers(Field field, int... modifiers) throws NoSuchFieldException, IllegalAccessException {
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		for (int mod : modifiers) {
			modifiersField.setInt(field, field.getModifiers() & ~mod);
		}
	}
}