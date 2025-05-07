package com.project.rekatrack.viewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.project.rekatrack.data.repository.Repository

class ViewModelFactory(
    private val repository: Repository,
    private val context: Context
): ViewModelProvider.Factory {
// TODO
}