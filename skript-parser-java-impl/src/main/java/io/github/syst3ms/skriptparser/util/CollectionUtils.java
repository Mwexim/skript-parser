package io.github.syst3ms.skriptparser.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CollectionUtils {
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
}
