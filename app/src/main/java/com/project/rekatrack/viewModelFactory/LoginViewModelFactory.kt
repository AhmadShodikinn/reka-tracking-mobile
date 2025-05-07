package com.project.rekatrack.viewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.viewModel.LoginViewModel
import com.project.rekatrack.support.TokenHandler

class LoginViewModelFactory(
    private val repository: Repository,
    private val tokenHandler: TokenHandler,
    private val context: Context
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(repository, tokenHandler, context) as T
    }
}