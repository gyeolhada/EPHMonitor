package com.example.ephmonitor.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import com.example.ephmonitor.MainActivity
import com.tbruyelle.rxpermissions3.RxPermissions

object PermissionUtil {
    private lateinit var rxPermissions: RxPermissions
    fun setUp(context: Context) {
        rxPermissions = RxPermissions(context as MainActivity)
    }

    @SuppressLint("CheckResult")
    fun checkPermission(): Boolean {
        var flag = true
        rxPermissions.requestEach(
            Manifest.permission.MANAGE_MEDIA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_MEDIA,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ).subscribe { permission ->
            if (!permission.granted) flag = false
        }
        return flag
    }
}