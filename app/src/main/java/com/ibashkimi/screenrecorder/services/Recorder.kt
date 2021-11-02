/*
 * Copyright (C) 2019 Indrit Bashkimi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibashkimi.screenrecorder.services

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException
import androidx.core.content.FileProvider





class Recorder(private val context: Context) {
    private val DISPLAY_WIDTH: Int = 480
    private val DISPLAY_HEIGHT: Int = 640
    var isRecording: Boolean = false

    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjectionCallback: MediaProjectionCallback? = null

    fun start(result: Int, data: Intent): Boolean {
        if (isRecording) {
            throw IllegalStateException("start called but Recorder is already recording.")
        }
        val newMediaRecorder = MediaRecorder()
        if (!newMediaRecorder.init()) {
            isRecording = false
            return false
        }
        mediaRecorder = newMediaRecorder

        //Set Callback for MediaProjection
        mediaProjectionCallback = MediaProjectionCallback()
        val projectionManager =
            context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        //Initialize MediaProjection using data received from Intent
        mediaProjection = projectionManager.getMediaProjection(result, data)?.apply {
            registerCallback(mediaProjectionCallback, null)
            virtualDisplay = createVirtualDisplay(
                "ScreenRecorder",
                DISPLAY_WIDTH,
                DISPLAY_HEIGHT,
                1080,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                newMediaRecorder.surface,
                null,
                null
            )
        }

        return try {
            newMediaRecorder.start()
            isRecording = true
            true
        } catch (e: IllegalStateException) {
            isRecording = false
            mediaProjection?.stop()
            mediaRecorder = null
            mediaProjection = null
            mediaProjectionCallback = null
            false
        }
    }

    fun stop(): Boolean {
        return stopScreenSharing()
    }

    fun pause() {
        if (!isRecording) {
            throw IllegalStateException("Called pause but Recorder is not recording.")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
        }
    }

    fun resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
        }
    }

    private fun MediaRecorder.init(): Boolean {
        val file: File = File(context.filesDir, "video.mp4")
//        val file: File = File(context.cacheDir, "video.mp4")
//        <cache-path en file_paths

        val uri = FileProvider.getUriForFile(context, "com.ibashkimi.screenrecorder", file)

        val fileDescriptor = context.contentResolver
            .openFileDescriptor(uri, "w")?.fileDescriptor ?: return false

        try {
            setOutputFile(fileDescriptor)
            //setAudioSource(MediaRecorder.AudioSource.MIC);
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            //setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            setVideoEncodingBitRate(512 * 1000)
            setVideoFrameRate(30)
            setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            prepare()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    private fun stopScreenSharing(): Boolean {
        if (virtualDisplay == null) {
            Log.d("Recorder", "Virtual display is null. Screen sharing already stopped")
            return true
        }
        var success: Boolean
        try {
            mediaRecorder?.stop()
            Log.i("Recorder", "MediaProjection Stopped")
            success = true
        } catch (e: RuntimeException) {
            Log.e(
                "Recorder",
                "Fatal exception! Destroying media projection failed." + "\n" + e.message
            )
            success = false
        } finally {
            mediaRecorder?.reset()
            virtualDisplay?.release()
            mediaRecorder?.release()
            mediaProjection?.let {
                it.unregisterCallback(mediaProjectionCallback)
                it.stop()
                mediaProjection = null
            }
        }
        isRecording = false
        return success
    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            stopScreenSharing()
        }
    }
}