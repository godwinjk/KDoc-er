package examples

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    val userName: LiveData<String> = MutableLiveData("Guest")

    val isLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    val loadState: StateFlow<LoadState> = MutableStateFlow(LoadState.Loading)

    val events: SharedFlow<String> = MutableStateFlow("")

    fun loadUser(userId: Long) {
        // load user from repository
    }

    fun updateName(newName: String) {
        // update display name
    }

    fun logout() {
        // clear session
    }
}
