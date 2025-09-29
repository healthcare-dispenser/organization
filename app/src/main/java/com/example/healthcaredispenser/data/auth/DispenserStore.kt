package com.example.healthcaredispenser.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dispenser_store")

object DispenserStore {
    private val KEY = stringPreferencesKey("dispenser_uuid")

    fun flow(context: Context): Flow<String?> =
        context.dataStore.data.map { it[KEY] }

    suspend fun set(context: Context, uuid: String) {
        context.dataStore.edit { it[KEY] = uuid }
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { it.remove(KEY) }
    }
}
