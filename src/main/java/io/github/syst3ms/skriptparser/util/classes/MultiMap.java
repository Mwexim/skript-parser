package io.github.syst3ms.skriptparser.util.classes;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple implementation of a Multimap, emulating Guava's. This implementation allows duplicate elements in the
 * values.
 */
public class MultiMap<K, V> extends HashMap<K, List<V>> {
    /**
     * Looks for a list that is mapped to the given key. If there is one, then the given value is added to that list.
     * If there isn't, then a new entry is created and has the value added to it.
     *
     * @param key the key
     * @param value the value
     */
    public void putOne(@Nullable K key, @Nullable V value) {
        if (this.containsKey(key)) {
            this.get(key).add(value);
        } else {
            List<V> values = new ArrayList<>();
            values.add(value);
            this.put(key, values);
        }
    }

    /**
     * @return all values of all keys of this MultiMap
     */
    public List<V> getAllValues() {
        List<V> values = new ArrayList<>();
        for (Iterable<V> v : values()) {
            for (var val : v) {
                values.add(val);
            }
        }
        return values;
    }
}
