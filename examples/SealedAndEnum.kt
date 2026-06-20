package examples

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>
    data object Loading : NetworkResult<Nothing>
}

sealed class NavigationEvent {
    data class NavigateTo(val route: String) : NavigationEvent()
    data object GoBack : NavigationEvent()
    data class ShowDialog(val title: String, val message: String) : NavigationEvent()
}

enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
}

enum class LogLevel(val priority: Int) {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3),
}
