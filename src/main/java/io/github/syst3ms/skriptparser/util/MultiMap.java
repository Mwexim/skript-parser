package io.github.syst3ms.skriptparser.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple implementation of a Multimap, emulating Guava's. This implementation allows duplicate elements in the
 * values.
 */
// I know classes like this are out there but the ones available to me didn't work
public class MultiMap<K, V> extends HashMap<K, List<V>> {
    /**
     * Looks for a list that is mapped to the given key. If there is not one then a new one is created
     * mapped and has the value added to it.
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

    public List<V> getAllValues() {
        List<V> values = new ArrayList<>();
        for (Iterable<V> v : values()) {
            for (V val : v) {
                values.add(val);
            }
        }
        return values;
    }
}
