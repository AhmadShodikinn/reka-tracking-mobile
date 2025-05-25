package com.project.rekatrack.data.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.response.UserLoginResponse
import com.project.rekatrack.support.TokenHandler
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel(
    private val repository: Repository,
    private val tokenHandler: TokenHandler,
    private val context: Context
): ViewModel() {
    private val _userLoginResult = MutableLiveData<UserLoginResponse>()
    val userLoginResult: LiveData<UserLoginResponse> = _userLoginResult

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.authLogin(email, password)

                if (response.isSuccessful) {
                    _userLoginResult.value = response.body()

                    response.body()?.accessToken?.let {
                        tokenHandler.saveToken(it)
                    }

                    response.body()?.data?.let { userData ->
                        val userName = userData.name
                        val userRole = userData.role?.name ?: "Tidak diketahui"

                        if (userName != null) {
                            tokenHandler.setUserInfo(userName, userRole)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = JSONObject(errorBody).getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context,"Server Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}