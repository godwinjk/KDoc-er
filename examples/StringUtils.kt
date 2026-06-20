package examples

object StringUtils {

    fun isBlankOrNull(value: String?): Boolean = value.isNullOrBlank()

    fun truncate(value: String, maxLength: Int): String =
        if (value.length <= maxLength) value else value.take(maxLength) + "…"

    fun countWords(text: String): Int =
        text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
}

fun String.toSlug(): String =
    lowercase().trim().replace(Regex("[^a-z0-9]+"), "-").trim('-')

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

fun List<String>.longest(): String? = maxByOrNull { it.length }
