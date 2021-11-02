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

package com.ibashkimi.screenrecorder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ibashkimi.screenrecorder.services.RecorderService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //private val SCREEN_RECORD_REQUEST_CODE = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannels()
        setContentView(R.layout.activity_main)

        findViewById<FloatingActionButton>(R.id.fab).apply {
            setOnClickListener {
                startRecording()
            }
        }

        btn_record.setOnClickListener {
            startRecording()
        }

        btn_stop.setOnClickListener {
            stopRecording()
        }
    }

    private fun stopRecording() {
        RecorderService.stop(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == SCREEN_RECORD_REQUEST_CODE){
            RecorderService.start(this, resultCode, data)
        }
    }

    private fun startRecording() {
        // Request Screen recording permission
        val projectionManager =
          getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            projectionManager.createScreenCaptureIntent(),
            SCREEN_RECORD_REQUEST_CODE
        )
    }

    companion object {
        const val SCREEN_RECORD_REQUEST_CODE = 1003
    }
}