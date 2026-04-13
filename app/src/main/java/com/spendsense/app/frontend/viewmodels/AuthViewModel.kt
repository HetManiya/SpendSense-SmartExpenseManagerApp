package com.spendsense.app.frontend.viewmodels

import android.util.Log
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
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    fun signInWithGoogle(idToken: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                _isAuthenticated.value = true
                _isGuest.value = false
                result.user?.let { onComplete(it.uid) }
            } catch (e: Exception) {
                _authError.value = e.message ?: "Authentication failed"
                Log.e("Auth", "Google sign in failed", e)
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun setGuestMode(value: Boolean) {
        _isGuest.value = value
        _isAuthenticated.value = false
    }

    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false
        _isGuest.value = false
    }

    fun clearAuthError() {
        _authError.value = null
    }
}
