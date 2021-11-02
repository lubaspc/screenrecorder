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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ibashkimi.screenrecorder.R
import com.ibashkimi.screenrecorder.RECORDING_NOTIFICATION_CHANNEL_ID


class RecorderService : Service() {

    private var session: RecordingSession? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return when (intent.action) {
            ACTION_RECORDING_START -> onActionRecordingStart(intent)
            ACTION_RECORDING_STOP -> onActionRecordingStop()
            else -> START_NOT_STICKY
        }
    }

    private fun onActionRecordingStart(intent: Intent): Int {
        return when (session?.state) {
            RecorderState.State.RECORDING -> START_STICKY
            else -> {
                startForeground(
                    NOTIFICATION_ID_RECORDING,
                    createOnRecordingNotification().build()
                )
                return (createNewRecordingSession()?.let {
                    if (it.start(intent)) {
                        session = it
                        START_STICKY
                    } else {
                        START_NOT_STICKY
                    }
                } ?: START_NOT_STICKY).also {
                    if (it == START_NOT_STICKY) stopForeground(true)
                }
            }
        }
    }


    private fun onActionRecordingStop(): Int {
        session?.let {
            when (it.state) {
                RecorderState.State.RECORDING, RecorderState.State.PAUSED -> {
                    if (it.stop()) {
                        Log.d(TAG, "Recording finished.")
                        onRecordingCompleted()
                        session = null
                    } else {
                        //delete(options.output.uri)
                        Toast.makeText(
                            this,
                            getString(R.string.recording_error_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                RecorderState.State.STOPPED -> {
                }
            }
        }

        stopForeground(true)
        stopSelf()
        return START_NOT_STICKY
    }


    private fun createNewRecordingSession(): RecordingSession? {
        return RecordingSession(this)
    }

    private fun onRecordingCompleted() = broadcast(ACTION_RECORDING_COMPLETED)

    private fun broadcast(action: String) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(action))
    }


    private fun createOnRecordingNotification(): NotificationCompat.Builder {
        return createNotification()
    }

    private fun createNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, RECORDING_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_summary_recording))
            .setTicker(getString(R.string.notification_title))
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setColor(Color.RED)
           // .setContentIntent(openAppPendingIntent)
            .setPriority(NotificationManagerCompat.IMPORTANCE_LOW)

    }

    companion object {
        private val TAG = RecorderService::class.java.simpleName

        const val ACTION_RECORDING_START = "com.ibashkimi.screenrecorder.action.RECORDING_START"
        const val ACTION_RECORDING_STOP = "com.ibashkimi.screenrecorder.action.RECORDING_STOP"

        const val ACTION_RECORDING_COMPLETED =
            "com.ibashkimi.screenrecorder.action.RECORDING_COMPLETED"
        const val ACTION_RECORDING_DELETED = "com.ibashkimi.screenrecorder.action.RECORDING_DELETED"

        const val RECORDER_INTENT_DATA = "recorder_intent_data"
        const val RECORDER_INTENT_RESULT = "recorder_intent_result"

        const val NOTIFICATION_ID_RECORDING = 1001

        fun start(context: Context, resultCode: Int, data: Intent?) {
            Intent(context, RecorderService::class.java).apply {
                action = ACTION_RECORDING_START
                putExtra(RECORDER_INTENT_DATA, data)
                putExtra(RECORDER_INTENT_RESULT, resultCode)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                context.startService(this)
            }
        }

        fun stop(context: Context) {
            Intent(context, RecorderService::class.java).apply {
                action = ACTION_RECORDING_STOP
                context.startService(this)
            }
        }
    }
}
