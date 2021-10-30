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
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ibashkimi.screenrecorder.services.RecorderService

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannels()
        setContentView(R.layout.activity_main)
        findViewById<FloatingActionButton>(R.id.fab).apply {
            setOnClickListener {
                startRecording()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        RecorderService.stop(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SCREEN_RECORD_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    RecorderService.start(this, resultCode, data)
                }
            }
        }
    }


    companion object {
        const val SCREEN_RECORD_REQUEST_CODE = 1003
    }

}