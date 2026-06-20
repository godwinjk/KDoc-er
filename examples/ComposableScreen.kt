package examples

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UserProfileScreen(userId: Long, onBack: () -> Unit, modifier: Modifier = Modifier) {
    // profile UI
}

@Composable
fun GreetingCard(userName: String, isOnline: Boolean) {
    // greeting card UI
}

@Composable
fun LoadingIndicator(message: String = "Loading…") {
    // spinner UI
}

@Preview
@Composable
fun UserProfileScreenPreview() {
    UserProfileScreen(userId = 1L, onBack = {})
}

@Preview
@Composable
fun GreetingCardPreview() {
    GreetingCard(userName = "Alice", isOnline = true)
}
