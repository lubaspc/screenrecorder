package com.ibashkimi.screenrecorder.services

import android.media.MediaRecorder
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Options(
    val output: OutputOptions
)

class OutputOptions(val uri: SaveUri, val format: Int = MediaRecorder.OutputFormat.DEFAULT)

@Parcelize
data class SaveUri(val uri: Uri, val type: UriType) : Parcelable

@Parcelize
enum class UriType : Parcelable {
    MEDIA_STORE, SAF
}