package com.codepad.foodai.ui.home.settings.health

import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import com.codepad.foodai.ui.home.HomeFragment
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    private val healthConnectClient: HealthConnectClient,
    private var healthConnectReader: HealthConnectReader,
) {

    var fragment: Fragment? = null
    var onGoogleFitBodyDataRead: ((Pair<MutableList<Int>, MutableList<Int>>) -> (Unit))? = null

    private val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()
    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    private var requestPermissions: ActivityResultLauncher<Set<String>>? = null

    fun initContent(fragment: HomeFragment) {
        this.fragment = fragment
        requestPermissions =
            fragment.registerForActivityResult(requestPermissionActivityContract) { granted ->
                if (granted.containsAll(PERMISSIONS)) {
                    this.fragment?.lifecycleScope?.launch {
                        checkPermissionsAndRun(healthConnectClient, onGoogleFitBodyDataRead)
                    }
                } else {
                    PermissionController.createRequestPermissionResultContract()
                }
            }
    }

    fun hasUnlockedIntegration(hasUnlocked: ((Boolean) -> Unit)) {
        fragment?.lifecycleScope?.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            hasUnlocked(granted.containsAll(PERMISSIONS))
        }
    }


    fun readData() {
        fragment?.lifecycleScope?.launch {
            checkPermissionsAndRun(healthConnectClient, onGoogleFitBodyDataRead)
        }
    }

    suspend fun checkPermissionsAndRun(
        healthConnectClient: HealthConnectClient,
        onGoogleFitBodyDataRead: ((Pair<MutableList<Int>, MutableList<Int>>) -> Unit)? = null,
    ) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            healthConnectReader.performHealthAction {
                onGoogleFitBodyDataRead?.invoke(it)
            }
        } else {
            requestPermissions?.launch(PERMISSIONS)
        }
    }
}