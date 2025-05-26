package com.project.rekatrack.data.viewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.response.ForgotPasswordResponse
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

    private val _userForgotPasswordResult = MutableLiveData<ForgotPasswordResponse>()
    val userForgotPasswordResponse: LiveData<ForgotPasswordResponse> = _userForgotPasswordResult

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
                Log.e("LoginError", "Exception saat login", e)
                Toast.makeText(context,"Server Error!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun forgotPassword(email: String, password: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val response = repository.forgotPassword(email,password,newPassword)

                if (response.isSuccessful) {
                    _userForgotPasswordResult.value = response.body()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = JSONObject(errorBody).getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception) {
                Log.e("LoginError", "Exception saat login", e)
                Toast.makeText(context,"Server Error!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}