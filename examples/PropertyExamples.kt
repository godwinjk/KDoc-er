package examples

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AppConfig {
    const val APP_NAME = "KDoc-er"
    const val MAX_RETRIES = 3
    var debugMode: Boolean = false
}

class SessionManager {

    var currentUserId: Long? = null
        private set

    val isLoggedIn: Boolean
        get() = currentUserId != null

    val sessionToken: StateFlow<String?> = MutableStateFlow(null)

    fun login(userId: Long) {
        currentUserId = userId
    }

    fun logout() {
        currentUserId = null
    }
}

val defaultPageSize: Int = 20

var globalRetryCount: Int = 0
