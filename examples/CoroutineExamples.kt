package examples

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

suspend fun fetchUserProfile(userId: Long): User? {
    // network call
    return null
}

suspend fun saveUserProfile(user: User): Boolean {
    // network call
    return true
}

fun observeActiveUsers(): Flow<List<User>> {
    // returns a reactive stream
    TODO()
}

fun watchOrderUpdates(orderId: Long): Flow<String> {
    TODO()
}

suspend fun computeReportAsync(dateRange: String): Deferred<String> {
    TODO()
}

fun createNotificationChannel(capacity: Int): Channel<String> {
    TODO()
}

suspend fun List<User>.filterActive(): List<User> =
    filter { it.active }

suspend fun String.resolveUserId(): Long? {
    return null
}
