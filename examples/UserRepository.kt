package examples

interface UserRepository {

    fun findById(id: Long): User?

    fun findByRole(role: Role): List<User>

    fun existsByEmail(email: String): Boolean

    fun save(user: User): User

    fun deleteById(id: Long)

    fun countActive(): Int
}

class InMemoryUserRepository : UserRepository {

    private val store: MutableMap<Long, User> = mutableMapOf()

    override fun findById(id: Long): User? = store[id]

    override fun findByRole(role: Role): List<User> =
        store.values.filter { it.active }

    override fun existsByEmail(email: String): Boolean =
        store.values.any { it.email == email }

    override fun save(user: User): User {
        store[user.id] = user
        return user
    }

    override fun deleteById(id: Long) {
        store.remove(id)
    }

    override fun countActive(): Int = store.values.count { it.active }
}
