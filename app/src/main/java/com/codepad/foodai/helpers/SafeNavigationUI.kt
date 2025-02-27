package com.codepad.foodai.helpers

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import timber.log.Timber

/**
 * Utility class that provides safe navigation methods to prevent crashes
 * when navigation actions fail.
 */
object SafeNavigationUI {

    /**
     * Sets up a [NavigationBarView] for use with a [NavController]. This will call
     * [onNavDestinationSelected] when a menu item is selected.
     *
     * @param navigationBarView The NavigationBarView (BottomNavigationView) to set up
     * @param navController The NavController that controls the navigation
     */
    fun setupWithNavController(
        navigationBarView: NavigationBarView,
        navController: NavController
    ) {
        navigationBarView.setOnItemSelectedListener { item ->
            try {
                onNavDestinationSelected(item, navController)
            } catch (e: Exception) {
                Timber.e(e, "Navigation error when selecting item with id: ${item.itemId}")
                false
            }
        }
    }

    /**
     * Attempts to navigate to the destination associated with the given MenuItem.
     * This handles null checks and exceptions to prevent app crashes.
     *
     * @param item The selected menu item
     * @param navController The NavController that controls the navigation
     * @return true if the navigation is successful, false otherwise
     */
    private fun onNavDestinationSelected(
        item: MenuItem,
        navController: NavController
    ): Boolean {
        val builder = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
        
        // Get the current destination
        val currentDestination = navController.currentDestination
        if (currentDestination == null) {
            Timber.w("Current destination is null, cannot navigate")
            return false
        }
        
        // Try to find the destination in the navigation graph
        val destinationId = item.itemId
        try {
            // Check if the destination exists in the graph
            val destinationExists = navController.graph.findNode(destinationId) != null
            
            if (!destinationExists) {
                Timber.w("Destination with id $destinationId not found in the navigation graph")
                return false
            }
            
            // If we're already at this destination, just return true
            if (currentDestination.id == destinationId) {
                return true
            }
            
            // Navigate to the destination
            navController.navigate(destinationId, null, builder.build())
            return true
        } catch (e: Exception) {
            Timber.e(e, "Error navigating to destination with id: $destinationId")
            return false
        }
    }
} 