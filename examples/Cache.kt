package examples

/**
 *
 */
class Cache<K, V>(val maxSize: Int) {

    private val entries: LinkedHashMap<K, V> = LinkedHashMap()

    val size: Int
        get() = entries.size

    val isEmpty: Boolean
        get() = entries.isEmpty()

    fun get(key: K): V? = entries[key]

    fun put(key: K, value: V) {
        if (entries.size >= maxSize) {
            val oldest = entries.keys.firstOrNull()
            if (oldest != null) entries.remove(oldest)
        }
        entries[key] = value
    }

    fun containsKey(key: K): Boolean = entries.containsKey(key)

    fun clear() {
        entries.clear()
    }
}

object Defaults {
    const val MAX_CACHE_SIZE = 128
    const val DEFAULT_TIMEOUT_MS = 5000L
}

val supportedLocales: List<String> = listOf("en", "fr", "de", "es")
