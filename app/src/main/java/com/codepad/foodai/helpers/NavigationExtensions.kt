package com.codepad.foodai.helpers

import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import timber.log.Timber

/**
 * Sets up a [BottomNavigationView] for use with a [NavController] in a safe way
 * that prevents crashes when navigation fails.
 */
fun BottomNavigationView.setupWithNavControllerSafely(navController: NavController) {
    try {
        SafeNavigationUI.setupWithNavController(this, navController)
    } catch (e: Exception) {
        Timber.e(e, "Error setting up BottomNavigationView with NavController")
    }
}

/**
 * Sets up any [NavigationBarView] for use with a [NavController] in a safe way
 * that prevents crashes when navigation fails.
 */
fun NavigationBarView.setupWithNavControllerSafely(navController: NavController) {
    try {
        SafeNavigationUI.setupWithNavController(this, navController)
    } catch (e: Exception) {
        Timber.e(e, "Error setting up NavigationBarView with NavController")
    }
} 