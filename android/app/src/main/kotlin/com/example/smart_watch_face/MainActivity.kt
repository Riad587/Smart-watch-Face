package com.example.smart_watch_face

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.watchfacepush.WatchFacePushManagerFactory
import com.google.android.gms.wearable.Wearable
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.smart_watch/push"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                // 1. Logic to request turning on Bluetooth
                "requestBluetooth" -> {
                    // On Android 12+, BLUETOOTH_CONNECT is a runtime permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                            1002
                        )
                        result.success(false) // Permission not yet granted
                        return@setMethodCallHandler
                    }

                    val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    val adapter = bluetoothManager.adapter

                    if (adapter == null) {
                        result.error("NO_BT_HARDWARE", "Device does not support Bluetooth", null)
                    } else if (!adapter.isEnabled) {
                        // Open system dialog to turn on Bluetooth
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(intent, 1001)
                        result.success(false) // Not enabled yet, user needs to click 'Allow'
                    } else {
                        result.success(true) // Already enabled
                    }
                }

                // 2. Logic to check if a watch is actually connected
                "checkConnection" -> {
                    lifecycleScope.launch {
                        try {
                            val nodes = Wearable.getNodeClient(this@MainActivity).connectedNodes.await()
                            result.success(nodes.isNotEmpty())
                        } catch (e: Exception) {
                            result.error("CONN_CHECK_FAILED", e.message, null)
                        }
                    }
                }

                // 3. Your existing push logic
                "pushWatchFace" -> {
                    val path = call.argument<String>("path")
                    val pkg = call.argument<String>("packageName")
                    val token = call.argument<String>("token")

                    if (Build.VERSION.SDK_INT < 33) {
                        result.error(
                            "UNSUPPORTED_OS",
                            "Watch Face Push API requires Android 13 or higher.",
                            null
                        )
                    } else if (path != null && pkg != null && token != null) {
                        val file = File(path)
                        if (file.exists()) {
                            lifecycleScope.launch {
                                try {
                                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                                    val manager = WatchFacePushManagerFactory.createWatchFacePushManager(this@MainActivity)

                                    manager.addWatchFace(pfd, token)
                                    manager.setWatchFaceAsActive(pkg)

                                    result.success("Watch face applied successfully!")
                                } catch (e: Exception) {
                                    result.error("PUSH_FAILED", e.message, null)
                                }
                            }
                        } else {
                            result.error("FILE_NOT_FOUND", "APK file missing at: $path", null)
                        }
                    } else {
                        result.error("INVALID_ARGS", "Missing path, package, or token", null)
                    }
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}