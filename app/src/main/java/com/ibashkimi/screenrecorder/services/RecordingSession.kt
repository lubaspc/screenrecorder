package com.ibashkimi.screenrecorder.services

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.ibashkimi.screenrecorder.data.DataManager
import java.io.File

class RecordingSession(
    val context: Context,
) {

    private var recorder: Recorder? = null

    var state: RecorderState.State
        get() = RecorderState.state.value ?: RecorderState.State.STOPPED
        set(value) {
            RecorderState.state.value = value
        }

    var startTime: Long = 0

    var elapsedTime: Long = 0

    fun start(intent: Intent): Boolean {
        return if (state == RecorderState.State.STOPPED) {
            intent.getParcelableExtra<Intent?>(RecorderService.RECORDER_INTENT_DATA)?.let { intentData ->
                val result =
                    intent.getIntExtra(RecorderService.RECORDER_INTENT_RESULT, Activity.RESULT_OK)
                val newRecorder = Recorder(context)
                if (newRecorder.start(result, intentData)) {
                    startTime = System.currentTimeMillis()
                    state = RecorderState.State.RECORDING
                    recorder = newRecorder
                    true
                } else {
                    state = RecorderState.State.STOPPED
                    //recorder = null
                    false
                }
            } ?: false
        } else {
            true
        }
    }


    fun stop(): Boolean {
        recorder?.let {
            if (state == RecorderState.State.RECORDING || state == RecorderState.State.PAUSED) {
                if (it.stop()) {
                    val now = System.currentTimeMillis()
                    val values = ContentValues()
                    values.put(MediaStore.Video.Media.DATE_ADDED, now)
                    values.put(MediaStore.Video.Media.DATE_MODIFIED, now / 1000)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    }

                    //context.contentResolver.update(Uri.fromFile(context.externalCacheDir), values, null, null)
                    //val file: File = File(context.filesDir, "video.mp4")
                    //dataManager.update(options.output.uri.uri, values)
                }
            }
        }

        recorder = null
        state = RecorderState.State.STOPPED
        return true
    }

    private fun getFilePath(): String? {
        val directory: String =
            context.externalCacheDir.toString() + File.separator + "Recordings"
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            Toast.makeText(context, "Failed to get External Storage", Toast.LENGTH_SHORT).show()
            return null
        }
        val folder = File(directory)
        var success = true
        if (!folder.exists()) {
            success = folder.mkdir()
        }
        val filePath: String = if (success) {
            val videoName = "capture_video_cache.mp4"
            directory + File.separator + videoName
        } else {
            Toast.makeText(context, "Failed to create Recordings directory", Toast.LENGTH_SHORT).show()
            return null
        }
        return filePath
    }
}