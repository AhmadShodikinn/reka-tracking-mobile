package com.project.rekatrack.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.project.rekatrack.R
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.viewModel.LoginViewModel
import com.project.rekatrack.databinding.ActivityLoginBinding
import com.project.rekatrack.network.ApiConfig
import com.project.rekatrack.support.TokenHandler
import com.project.rekatrack.viewModelFactory.LoginViewModelFactory

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val tokenHandler = TokenHandler(this)
        val token = tokenHandler.getToken() ?: ""

        val repository = Repository(ApiConfig.getApiService(token))
        val factory = LoginViewModelFactory(repository, tokenHandler, this)
        loginViewModel = ViewModelProvider(this, factory).get(LoginViewModel::class.java)

        binding.filledButton.setOnClickListener {
            val email  = binding.emailInputLayout.editText?.text.toString()
            val password = binding.passwordInputLayout.editText?.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginViewModel.loginUser(email, password)
            } else {
                Toast.makeText(this, "field kosong!", Toast.LENGTH_SHORT).show()
            }
        }

        loginViewModel.userLoginResult.observe(this, {response ->
            if (response != null ){
                Toast.makeText(this, "Sukses!, Mengalihkan...", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MenusActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Gagal!...", Toast.LENGTH_SHORT).show()
            }
        })
    }
}