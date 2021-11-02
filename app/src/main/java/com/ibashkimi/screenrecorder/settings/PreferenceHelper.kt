package com.ibashkimi.screenrecorder.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.preference.PreferenceManager
import com.ibashkimi.screenrecorder.R
import com.ibashkimi.screenrecorder.services.SaveUri
import com.ibashkimi.screenrecorder.services.UriType
import java.util.*

class PreferenceHelper(
    private val context: Context,
    val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
) {

    val saveLocation: SaveUri?
        get() {
            getString(R.string.pref_key_save_location)?.let { Uri.parse(it) }?.let { uri ->
                getString(R.string.pref_key_save_location_type)?.let { type ->
                    val uriType = when (type) {
                        "media_store" -> UriType.MEDIA_STORE
                        "saf" -> UriType.SAF
                        else -> throw IllegalArgumentException("Unknown uri type $type.")
                    }
                    return SaveUri(uri, uriType)
                }
            } ?: return null
        }

    fun setSaveLocation(uri: Uri?, uriType: UriType?) {
        sharedPreferences.edit()
            .putString(context.getString(R.string.pref_key_save_location), uri.toString())
            .putString(
                context.getString(R.string.pref_key_save_location_type),
                uriType?.name?.toLowerCase(Locale.ENGLISH)
            )
            .apply()
    }

    fun resetSaveLocation() {
        if (Build.VERSION.SDK_INT < 29) {
            setSaveLocation(null, null)
        } else {
            setSaveLocation(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, UriType.MEDIA_STORE)
        }
    }

    private fun getString(stringRes: Int): String? {
        return sharedPreferences.getString(context.getString(stringRes), null)
    }
}