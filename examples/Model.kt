package examples

data class User(
    val id: Long,
    var displayName: String,
    val email: String,
    val active: Boolean,
)

data class Address(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
)

enum class Role {
    ADMIN,
    EDITOR,
    VIEWER,
    GUEST,
}

sealed class LoadState {
    object Loading : LoadState()
    data class Success(val items: List<User>) : LoadState()
    data class Failure(val reason: String) : LoadState()
}

data class Page<T>(
    val content: List<T>,
    val pageNumber: Int,
    val totalPages: Int,
)
