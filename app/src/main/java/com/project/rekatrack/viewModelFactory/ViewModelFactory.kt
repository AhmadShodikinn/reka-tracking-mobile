package com.project.rekatrack.viewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.viewModel.GeneralViewModel

class ViewModelFactory(
    private val repository: Repository,
    private val context: Context
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GeneralViewModel(repository, context) as T
    }
}