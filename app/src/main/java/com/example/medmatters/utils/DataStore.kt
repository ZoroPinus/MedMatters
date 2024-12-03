package com.example.medmatters.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_info")

class UserDataStore(private val context: Context) {

    companion object {
        val USER_UID_KEY = stringPreferencesKey("uid")
        val USER_EMAIL_KEY = stringPreferencesKey("email")
        val USER_NAME_KEY = stringPreferencesKey("name")
    }

    suspend fun storeUserInfo(uid: String, email: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_UID_KEY] = uid
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_NAME_KEY] = name
        }
    }

    val userInfoFlow: Flow<Map<String, String?>> = context.dataStore.data.map { preferences ->
        mapOf(
            "uid" to preferences[USER_UID_KEY],
            "email" to preferences[USER_EMAIL_KEY],
            "name" to preferences[USER_NAME_KEY]
        )
    }
}