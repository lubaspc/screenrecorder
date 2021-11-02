package com.ibashkimi.screenrecorder.fragment

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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ibashkimi.screenrecorder.R
import com.ibashkimi.screenrecorder.databinding.FragmentHomeRecorderBinding
import com.ibashkimi.screenrecorder.services.RecorderService
import com.ibashkimi.screenrecorder.services.UriType
import com.ibashkimi.screenrecorder.settings.PreferenceHelper
import kotlinx.android.synthetic.main.activity_main.*

class HomeRecorderFragment : Fragment() {
    private lateinit var vBinding: FragmentHomeRecorderBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        vBinding = FragmentHomeRecorderBinding.inflate(inflater, container, false)
        return vBinding.root
    }
}