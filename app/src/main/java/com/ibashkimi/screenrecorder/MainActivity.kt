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

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.selection.SelectionTracker
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ibashkimi.screenrecorder.data.Recording
import com.ibashkimi.screenrecorder.fragment.HomeRecorderFragment
import com.ibashkimi.screenrecorder.services.RecorderService
import com.ibashkimi.screenrecorder.services.RecorderState
import com.ibashkimi.screenrecorder.services.UriType
import com.ibashkimi.screenrecorder.settings.PreferenceHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var recorderState: LiveData<RecorderState.State>
    //private lateinit var selectionTracker: SelectionTracker<Recording>

    //private val SCREEN_RECORD_REQUEST_CODE = 1003
    //*******************************************
    //************
    private val REQUEST_GALERY = 1001
    private val REQUEST_CAMERA = 1002
    var foto: Uri? = null
    var video1: Uri? =null
    //************
    //*******************************************

    private lateinit var preferences: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannels()
        setContentView(R.layout.activity_main)

        abreGaleria()
        abreCamaraPrincipal()
        /*btn_scr_recorder.setOnClickListener {         launchFragment()
            Toast.makeText(this, "se presiono boton", Toast.LENGTH_SHORT).show()

        }*/

        findViewById<FloatingActionButton>(R.id.fab).apply {
            setOnClickListener {
                startRecording()
            }
        }

        btn_stop.setOnClickListener {
            //stopRecording()
            onStop()
        }

        /*findViewById<FloatingActionButton>(R.id.fab).apply {
            setOnClickListener {
                //startRecording()
                when {
                    selectionTracker.hasSelection() -> selectionTracker.clearSelection()
                    isRecording -> stopRecording()
                    else -> {
                            startRecording()
                    }
                }
            }
            setOnLongClickListener {
                Toast.makeText(this@MainActivity, R.string.home_fab_record_hint, Toast.LENGTH_SHORT)
                    .show()
                true
            }
        }*/

        preferences = PreferenceHelper(this).apply {
            // preferenceHelper.doPostInitCheck()
            // preferenceHelper.checkOutputDirectory()
            // preferenceHelper.checkRecordAudio()
            saveLocation?.let { uri ->
                if (uri.type == UriType.SAF) {
                    this@MainActivity.contentResolver.takePersistableUriPermission(
                        uri.uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    this@MainActivity.contentResolver.persistedUriPermissions.filter { it.uri == uri.uri }
                        .apply {
                            if (isEmpty()) {
                                resetSaveLocation()
                            }
                        }
                }
            }
        }

        /*selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Recording>() {
            override fun onSelectionChanged() {
                when {
                    !selectionTracker.hasSelection() -> bottomBarOnReset()
                    selectionTracker.selection.size() == 1 -> bottomBarOnItemSelected()
                    selectionTracker.selection.size() == 2 -> bottomBarOnItemsSelected()
                }
            }
        })*/
    }

    /*private fun bottomBarOnReset() {
        configureFab(isRecording)
    }*/

    /*private fun configureFab(isRecording: Boolean) {
        fab.setImageDrawable(if (isRecording) {
            R.drawable.ic_stop
        } else {
            R.drawable.ic_record
        })
    }*/

    private fun stopRecording() {
        RecorderService.stop(this)
    }

    private val isRecording: Boolean
        get() = recorderState.value?.run {
            this != RecorderState.State.STOPPED
        } ?: false

    /*private fun launchFragment() {
        val fragment = HomeRecorderFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.container_main, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }*/

    //*******************************************
    //************
    //Detectamos cuando se pulse el boton para abrir la camara
    private fun abreCamaraPrincipal(){
        btn_camera.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    //Pedirle permiso al usuario
                    val permisosCamara = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permisosCamara, REQUEST_CAMERA)
                }else
                    abreCamara()
            }else
                abreCamara()
        }
    }

    /*private fun recordScreenPrincipal(){
        btn_scr_recorder.setOnClickListener {
            val permisosRecScreen = arrayListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permisosRecScreen, SCREEN_RECORD_REQUEST_CODE)
        }else
            startRecording()
    }*/

    //Detectamos cuando se pulse el boton para abrir la galeria
    private fun abreGaleria(){
        btn_galeria.setOnClickListener {
            //Verificamos que version de android esta instalada
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Preguntamos si tiene permiso
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    //Pedir permiso al usuario
                    val permisoArchivos = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permisoArchivos, REQUEST_GALERY)
                } else {
                    muestraGaleria()
                }
            } else {
                //Tiene version de Lolipop hacia abajo y por default tiene permiso
                muestraGaleria()
            }
        }
    }

    //Checamos si el usuario dio permiso a la app
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_GALERY ->{
                if (grantResults[0]== PackageManager.PERMISSION_GRANTED)
                    muestraGaleria()
                else
                    Toast.makeText(this, "No puedes acceder a tus imagenes", Toast.LENGTH_SHORT).show()
            }
            REQUEST_CAMERA ->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    abreCamara()
                else
                    Toast.makeText(this, "No puedes acceder a tus imagenes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Abre la ventana donde se muestra la galeria de fotos
    private fun muestraGaleria(){
        val intentGaleria = Intent(Intent.ACTION_PICK)
        intentGaleria.type = "image/, video/*"
        startActivityForResult(intentGaleria, REQUEST_GALERY)
    }

    //Abre la camara del dispositivo
    private fun abreCamara(){
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "Nueva imagen")
        foto = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
        val camaraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        camaraIntent.putExtra(MediaStore.EXTRA_OUTPUT, foto)
        startActivityForResult(camaraIntent, REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_GALERY){
            img.setImageURI(data?.data)
            //video.setVideoURI(data?.data)
        }
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA){
            img.setImageURI(foto)
        }
        if(resultCode == Activity.RESULT_OK && requestCode == SCREEN_RECORD_REQUEST_CODE){
            RecorderService.start(this, resultCode, data)
            video.setVideoURI(video1)
        }
    }
    //************
    //*******************************************

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

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                SCREEN_RECORD_REQUEST_CODE -> {
//                    RecorderService.start(this, resultCode, data)
//                }
//            }
//        }
//    }


    companion object {
        const val SCREEN_RECORD_REQUEST_CODE = 1003
    }

}