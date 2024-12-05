package com.codepad.foodai.helpers

import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.provider.Settings
import java.util.UUID

val foodAIUUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)

object MediaDrmIdProvider {
    fun getDeviceUniqueId(): String {
        var mediaDrm: MediaDrm? = null
        return try {
            mediaDrm = MediaDrm(foodAIUUID)
            mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID).toHexString()
        } catch (e: UnsupportedSchemeException) {
            null
        } catch (e: Exception) {
            null
        } finally {
            mediaDrm?.release()
        } ?: Settings.Secure.ANDROID_ID
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
}