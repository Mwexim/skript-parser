package io.github.syst3ms.skriptparser.util

fun <T> Collection<Collection<T>>.flatMap() = flatMap { it }

/**
 * A simple implementation of a Multimap, emulating Guava's. This implementation allows duplicate elements in the the
 * values. (I know classes like this are out there but the ones available to me didn't work).
 */
class MultiMap<K, V> : LinkedHashMap<K, MutableList<V>>() {

    val allValues: List<V>
        get() = this.values.flatMap()
    /**
     * Looks for a list that is mapped to the given key. If there is not one then a new one is created
     * mapped and has the value added to it.
     *
     * @param key the key
     * @param value the value
     * @return true if the list has already been created, false if a new list is created.
     */
    fun putOne(key: K, value: V): Boolean {
        return if (this.containsKey(key)) {
            this[key]!!.add(value)
            true
        } else {
            val values = arrayListOf<V>()
            values.add(value)
            this.put(key, values)
            false
        }
    }
}

