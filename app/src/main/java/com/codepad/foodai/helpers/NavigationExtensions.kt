package com.codepad.foodai.helpers

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
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

/**
 * Safely navigate to a destination using an action ID.
 * This will catch and log any exceptions that occur during navigation.
 *
 * @param actionId The ID of the action to navigate with
 * @param args Optional navigation arguments
 * @param navOptions Optional navigation options
 * @return true if navigation was successful, false otherwise
 */
fun NavController.navigateSafely(
    actionId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null
): Boolean {
    return try {
        val destinationExists = currentDestination?.getAction(actionId) != null
        if (destinationExists) {
            navigate(actionId, args, navOptions)
            true
        } else {
            Timber.e("Navigation failed: Cannot find action $actionId from destination ${currentDestination?.id}")
            false
        }
    } catch (e: Exception) {
        Timber.e(e, "Navigation failed: Error navigating to action $actionId")
        false
    }
}

/**
 * Safely navigate to a destination using NavDirections.
 * This will catch and log any exceptions that occur during navigation.
 *
 * @param directions The NavDirections to navigate with
 * @param navOptions Optional navigation options
 * @return true if navigation was successful, false otherwise
 */
fun NavController.navigateSafely(
    directions: NavDirections,
    navOptions: NavOptions? = null
): Boolean {
    return try {
        navigate(directions, navOptions)
        true
    } catch (e: Exception) {
        Timber.e(e, "Navigation failed: Error navigating with directions $directions")
        false
    }
} 