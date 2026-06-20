package examples

abstract class BaseRepository {
    abstract fun findById(id: Long): User?
    abstract fun deleteById(id: Long)
}

class StrictUserRepository : BaseRepository() {

    private val store: MutableMap<Long, User> = mutableMapOf()

    override fun findById(id: Long): User? {
        if (id <= 0) throw IllegalArgumentException("id must be positive")
        return store[id]
    }

    override fun deleteById(id: Long) {
        if (id <= 0) throw IllegalArgumentException("id must be positive")
        store.remove(id) ?: throw NoSuchElementException("User $id not found")
    }

    fun updateEmail(userId: Long, email: String) {
        if (email.isBlank()) throw IllegalArgumentException("email must not be blank")
        val user = findById(userId) ?: throw NoSuchElementException("User $userId not found")
        store[userId] = user.copy(email = email)
    }

    fun requireActive(userId: Long): User {
        val user = findById(userId) ?: throw NoSuchElementException("User not found")
        if (!user.active) throw IllegalStateException("User $userId is not active")
        return user
    }
}
