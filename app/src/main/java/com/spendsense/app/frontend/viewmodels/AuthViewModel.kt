package com.spendsense.app.frontend.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null || sharedPrefs.getBoolean("is_guest", false))
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
                auth.signInWithEmailAndPassword(email, pass).await()
                _isAuthenticated.value = true
                _isGuest.value = false
                sharedPrefs.edit().putBoolean("is_guest", false).apply()
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Authentication failed"
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
                auth.createUserWithEmailAndPassword(email, pass).await()
                _isAuthenticated.value = true
                _isGuest.value = false
                sharedPrefs.edit().putBoolean("is_guest", false).apply()
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Signup failed"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _isAuthenticated.value = true
                _isGuest.value = false
                sharedPrefs.edit().putBoolean("is_guest", false).apply()
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Google Login failed"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun setGuestMode(value: Boolean) {
        _isGuest.value = value
        _isAuthenticated.value = value
        sharedPrefs.edit().putBoolean("is_guest", value).apply()
        if (value) {
            auth.signOut() // Ensure no user is signed in if guest mode is on
        }
    }

    fun logout() {
        auth.signOut()
        sharedPrefs.edit().putBoolean("is_guest", false).apply()
        _isAuthenticated.value = false
        _isGuest.value = false
    }

    fun clearAuthError() {
        _authError.value = null
    }
}
