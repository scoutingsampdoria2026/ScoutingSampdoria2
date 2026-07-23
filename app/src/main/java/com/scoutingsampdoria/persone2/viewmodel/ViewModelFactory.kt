package com.scoutingsampdoria.persone2.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scoutingsampdoria.persone2.data.db.ScoutingDatabase
import com.scoutingsampdoria.persone2.data.repository.Repository

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val repository: Repository by lazy {
        Repository(ScoutingDatabase.get(context))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(context.applicationContext) as T
            modelClass.isAssignableFrom(PersoneViewModel::class.java) ->
                PersoneViewModel(repository) as T
            modelClass.isAssignableFrom(ConvocazioniViewModel::class.java) ->
                ConvocazioniViewModel(repository) as T
            modelClass.isAssignableFrom(ConfigViewModel::class.java) ->
                ConfigViewModel(repository) as T
            else -> throw IllegalArgumentException("ViewModel sconosciuto: ${modelClass.name}")
        }
    }
}
