package io.github.syst3ms.skriptparser.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

public class CollectionUtils {
	private static final Random rnd = new Random();

	@SafeVarargs
	public static <T> Iterator<T> iterator(T... elements) {
		return new Iterator<T>() {
			T[] data = elements;
			int index;

			@Override
			public boolean hasNext() {
				return index < data.length;
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return data[index++];
			}
		};
	}

	public static boolean containsSuperclass(Class<?>[] classes, Class<?> c) {
		for (Class<?> clazz : classes) {
			if (clazz == Object.class || clazz.isAssignableFrom(c)) {
				return true;
			}
		}
		return false;
	}

	public static <T> T getRandom(T[] array) {
		return array[rnd.nextInt(array.length)];
	}
}
