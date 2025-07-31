package com.example.ephmonitor.model

import android.bluetooth.BluetoothDevice
import com.example.ephmonitor.enums.DeviceStatus

data class BLEDeviceInfo(
    //设备名
    var mDeviceName: String?,
    //设备状态
    var mDeviceState: DeviceStatus?,
    //设备地址
    var mAddress: String?,
    //设备类型
    var mType: String?,
    //蓝牙设备
    var mBluetoothDevice: BluetoothDevice?,
    //电量
    var mElc: Double?,
    //上次接收数据时间
    var mReceiveTime: Long?,
    //上次更新电量时间
    var mLowElcTipTime: Long?,
    //连接时间
    var mConnectTime: Long?
)
