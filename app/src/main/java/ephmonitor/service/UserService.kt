package com.example.ephmonitor.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.MutableLiveData
import com.example.ephmonitor.MainActivity
import com.example.ephmonitor.R
import com.example.ephmonitor.enums.DeviceStatus
import com.example.ephmonitor.model.BLEDeviceInfo

class UserService : Service() {

    //scan到的设备
    val scannedDevices: MutableLiveData<MutableList<BLEDeviceInfo>> by lazy {
        MutableLiveData<MutableList<BLEDeviceInfo>>()
    }

    //使用的设备
    val connectedDevice: MutableLiveData<BLEDeviceInfo?> =
        MutableLiveData<BLEDeviceInfo?>(null)

    val isScanning: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }


    companion object {
        const val BLE_ServiceUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
        const val BLE_CharacterUUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
    }

    private val binder = UserBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class UserBinder : Binder() {
        fun getService(): UserService = this@UserService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        return START_STICKY//保持服务不被杀死
    }

    override fun onDestroy() {
        Log.i("UserService", "onDestroy")
        super.onDestroy()
    }

    private fun startForeground() {
        try {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "CHANNEL_ID", "设备通知", NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
            val notification =
                NotificationCompat.Builder(this, "CHANNEL_ID").setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("正在开启蓝牙服务").setContentText("点击查看详情")
                    .setContentIntent(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            PendingIntent.getActivity(
                                this,
                                0,
                                Intent(this, MainActivity::class.java),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        } else {
                            PendingIntent.getActivity(
                                this,
                                0,
                                Intent(this, MainActivity::class.java),
                                PendingIntent.FLAG_MUTABLE
                            )
                        }
                    ).build()
            if (Build.VERSION.SDK_INT >= 34) {
                ServiceCompat.startForeground(/* service = */ this,/* id = */ 100, // Cannot be 0
                    /* notification = */ notification,/* foregroundServiceType = */
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                    } else {
                        0
                    }
                )
            } else {
                Log.i("UserService", "startForeground")
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            Log.i("BLEServiceE", e.toString())
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result == null) return
            getDeviceScanned(result.device)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        //状态改变
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    //提高MTU
                    gatt!!.requestMtu(224)
                    connectedDevice.postValue(
                        BLEDeviceInfo(
                            gatt.device.name,
                            DeviceStatus.CONNECTED,
                            gatt.device.address,
                            gatt.device.type.toString(),
                            gatt.device,
                            null,
                            null,
                            null,
                            null
                        )
                    )
                }

                BluetoothGatt.STATE_DISCONNECTED -> {
                    disconnectDevice()
                    Log.i("BLEStatus", "STATE_DISCONNECTED")
                }

                BluetoothGatt.STATE_CONNECTING -> {
                    Log.d("BLEStatus", "正在连接");
                }

                BluetoothGatt.STATE_DISCONNECTING -> {
                    Log.d("BLEStatus", "正在断开");
                }

                else -> {
                    Log.d("BLEStatus", "未知状态");
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            ///设置mtu值，即bluetoothGatt.requestMtu()时触发，提示该操作是否成功
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices()
                Log.w("ble", "设置MTU成功，新的MTU值：" + (mtu - 3) + ",status" + status);
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                Log.e("ble", "设置MTU值失败：" + (mtu - 3) + ",status" + status);
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.i("UserService", "onServicesDiscovered")
            //gatt 通信协议
            super.onServicesDiscovered(gatt, status)
            for (blueToothService in gatt.services) {
                if (blueToothService.uuid.toString() == BLE_ServiceUUID) {
                    for (bluetoothGattCharacteristic in blueToothService.characteristics) {
                        if (bluetoothGattCharacteristic.uuid.toString() == BLE_CharacterUUID) {
                            //打开通知，否则接收不到信号
                            val isEnableNotification = gatt.setCharacteristicNotification(
                                bluetoothGattCharacteristic, true
                            )
                            if (isEnableNotification) {
                                val descriptorList = bluetoothGattCharacteristic.descriptors
                                if (descriptorList != null && descriptorList.size > 0) {
                                    for (descriptor in descriptorList) {
                                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                                        gatt.writeDescriptor(descriptor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private var bluetoothGatt: BluetoothGatt? = null
    fun switchScanState() {
        isScanning.value = isScanning.value?.not()
        if (isScanning.value == true) {
            startScan()
        } else {
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun getDeviceScanned(device: BluetoothDevice?) {
        if (device == null || device.name == null) return
        //避免重复添加
        var list = scannedDevices.value
        if (list != null) {
            for (item in list) {
                if (item.mDeviceName == device.name) {
                    return
                }
            }
        } else {
            list = mutableListOf()
        }
        list.add(
            BLEDeviceInfo(
                device.name,
                DeviceStatus.DISCONNECTED,
                device.address,
                device.type.toString(),
                device,
                null,
                null,
                null,
                null
            )
        )
        Log.i("UserService", "getDeviceScanned$list")
        scannedDevices.postValue(list)
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        bluetoothAdapter.bluetoothLeScanner?.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun connectDevice(context: Context, address: String) {
        try {
            Log.i("UserService", "connectDevice")
            //如果有旧的连接，先断开
            connectedDevice.value?.mAddress.let {
                if (it != null) {
                    disconnectDevice()
                }
            }
            val device = bluetoothAdapter.getRemoteDevice(address)
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
            isScanning.postValue(false)
        } catch (exception: IllegalArgumentException) {
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice() {
        bluetoothGatt?.disconnect()
        Log.i("UserService", "disconnectDevice")
        bluetoothGatt?.close()
        connectedDevice.postValue(connectedDevice.value.let {
            it?.mDeviceState = DeviceStatus.DISCONNECTED
            it
        })
    }
}