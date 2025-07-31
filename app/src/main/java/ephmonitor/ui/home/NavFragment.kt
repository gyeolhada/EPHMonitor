package com.example.ephmonitor.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.util.Log
import android.net.ConnectivityManager
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.ephmonitor.databinding.FragmentNavBinding

class NavFragment : Fragment() {

    private var _binding: FragmentNavBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationManager: LocationManager
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavBinding.inflate(inflater, container, false)
        val webView = binding.webView

        // 配置 WebView
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.setSupportZoom(true)  // 允许缩放
        webSettings.useWideViewPort = true  // 启用响应式布局
        webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Mobile Safari/537.36"

        // 设置 WebViewClient 处理 URL 跳转
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                Log.d("WebView", "URL loading: ${request?.url}") // 日志打印，调试用
                return false
            }
        }

        // 获取当前位置后加载百度地图
        getLocation { latitude, longitude ->
            currentLatitude = latitude
            currentLongitude = longitude
            val mapUrl = "https://map.baidu.com/?lat=$latitude&lon=$longitude&zoom=15"
            webView.loadUrl(mapUrl)
            Log.d("WebView", "Map URL: $mapUrl") // 打印地图URL，便于调试
        }

        // 开始导航按钮
        binding.btStart.setOnClickListener {
            currentLatitude?.let { latitude ->
                currentLongitude?.let { longitude ->
                    // 假设目标位置为某固定点（可以根据需求设置）
                    val targetLatitude = 40.7128  // 目标纬度
                    val targetLongitude = -74.0060 // 目标经度
                    val navigationUrl = "https://map.baidu.com/?lat=$latitude&lon=$longitude&to=$targetLatitude,$targetLongitude&zoom=15"
                    webView.loadUrl(navigationUrl)  // 加载导航页面
                    Log.d("WebView", "Navigation URL: $navigationUrl") // 打印导航URL，便于调试
                }
            }
        }

        // 结束导航按钮
        binding.btEnd.setOnClickListener {
            // 结束导航，清除 WebView 或返回到默认地图页面
            webView.loadUrl("https://map.baidu.com/")  // 加载百度地图首页
        }

        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(callback: (Double, Double) -> Unit) {
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 检查权限
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // 请求权限
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }

        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) {
            callback(location.latitude, location.longitude)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    callback(location.latitude, location.longitude)
                    locationManager.removeUpdates(this) // 获取位置后移除监听，防止多次调用
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            })
        }
    }

    // 处理权限请求结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授权，获取位置
                getLocation { latitude, longitude ->
                    currentLatitude = latitude
                    currentLongitude = longitude
                    val mapUrl = "https://map.baidu.com/?lat=$latitude&lon=$longitude&zoom=15"
                    binding.webView.loadUrl(mapUrl)
                    Log.d("WebView", "Map URL: $mapUrl") // 打印地图URL，便于调试
                }
            } else {
                // 权限被拒绝，提示用户
                // 可以弹出提示框让用户去设置界面开启权限
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
