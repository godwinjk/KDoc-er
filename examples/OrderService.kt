package examples

class OrderService(
    val repository: UserRepository,
    val taxRate: Double,
    maxRetries: Int,
) {

    private var processedCount: Int = 0

    fun placeOrder(userId: Long, amount: Double): Boolean {
        val user = repository.findById(userId) ?: return false
        if (!user.active) return false
        processedCount++
        return true
    }

    fun calculateTotal(subtotal: Double, discount: Double): Double =
        (subtotal - discount) * (1 + taxRate)

    fun summarize(userId: Long): String {
        val user = repository.findById(userId)
        return user?.displayName ?: "unknown"
    }

    fun retry(block: () -> Unit) {
        block()
    }
}
