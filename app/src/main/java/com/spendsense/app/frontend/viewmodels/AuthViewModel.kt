package com.spendsense.app.frontend.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _isGuest = MutableStateFlow(sharedPrefs.getBoolean("is_guest", false))
    val isGuest: StateFlow<Boolean> = _isGuest

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val storedEmail = sharedPrefs.getString("user_email", null)
                val storedPass = sharedPrefs.getString("user_pass", null)
                
                if (email == storedEmail && pass == storedPass) {
                    sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
                    _isAuthenticated.value = true
                    _isGuest.value = false
                } else {
                    _authError.value = "Invalid credentials"
                }
            } catch (e: Exception) {
                _authError.value = "Auth failed"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                sharedPrefs.edit()
                    .putString("user_email", email)
                    .putString("user_pass", pass)
                    .putString("user_id", UUID.randomUUID().toString())
                    .putBoolean("is_logged_in", true)
                    .apply()
                _isAuthenticated.value = true
                _isGuest.value = false
            } catch (e: Exception) {
                _authError.value = "Signup failed"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun signInWithGoogle(idToken: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            try {
                val userId = "google_" + UUID.randomUUID().toString().take(8)
                sharedPrefs.edit()
                    .putString("user_id", userId)
                    .putBoolean("is_logged_in", true)
                    .putBoolean("is_guest", false)
                    .apply()
                _isAuthenticated.value = true
                _isGuest.value = false
                onComplete(userId)
            } catch (e: Exception) {
                _authError.value = "Google Login failed"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun setGuestMode(value: Boolean) {
        _isGuest.value = value
        _isAuthenticated.value = value
        sharedPrefs.edit().putBoolean("is_guest", value).putBoolean("is_logged_in", value).apply()
    }

    fun logout() {
        sharedPrefs.edit().clear().apply()
        _isAuthenticated.value = false
        _isGuest.value = false
    }

    fun clearAuthError() {
        _authError.value = null
    }
}
