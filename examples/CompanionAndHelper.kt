package examples

class ApiClient private constructor(val baseUrl: String, val timeout: Long) {

    fun get(path: String): String = ""

    fun post(path: String, body: String): String = ""

    companion object {
        const val DEFAULT_TIMEOUT = 5000L

        fun create(baseUrl: String, timeout: Long = DEFAULT_TIMEOUT): ApiClient =
            ApiClient(baseUrl, timeout)

        fun fromEnvironment(): ApiClient =
            create(System.getenv("API_BASE_URL") ?: "http://localhost")
    }
}

class DateFormatHelper {

    fun formatEpochMillis(millis: Long): String = ""

    fun parseIsoDate(dateString: String): Long = 0L

    fun daysBetween(startMillis: Long, endMillis: Long): Int = 0
}

class ValidationHelper {

    fun isValidEmail(email: String): Boolean =
        email.contains("@") && email.contains(".")

    fun isStrongPassword(password: String): Boolean =
        password.length >= 8

    fun sanitizeInput(raw: String): String =
        raw.trim().replace(Regex("[<>]"), "")
}
