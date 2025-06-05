package com.project.rekatrack.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.viewModel.LoginViewModel
import com.project.rekatrack.databinding.ActivityForgotPasswordBinding
import com.project.rekatrack.network.ApiConfig
import com.project.rekatrack.support.TokenHandler
import com.project.rekatrack.viewModelFactory.LoginViewModelFactory

class ForgotPasswordActivity: AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val tokenHandler = TokenHandler(this)

        val repository = Repository(ApiConfig.getApiService(tokenHandler))
        val factory = LoginViewModelFactory(repository, tokenHandler, this)
        loginViewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)

        binding.submitBtn.setOnClickListener {
            val email = binding.emailInputLayout.editText?.text.toString()
            val password = binding.newPasswordInputLayout.editText?.text.toString()
            val confirmPassword = binding.confirmPasswordInputLayout.editText?.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                loginViewModel.forgotPassword(email, password, confirmPassword)
            } else {
                Toast.makeText(this, "field kosong!", Toast.LENGTH_SHORT).show()
            }
        }

        loginViewModel.userForgotPasswordResponse.observe(this) { response ->
            if (response != null) {
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()

                if (response.message == "Password berhasil diubah.") {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

            }
        }
    }

}