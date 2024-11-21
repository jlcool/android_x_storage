package dev.yacine.android_x_storage

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Environment
import android.os.storage.StorageManager

import androidx.core.content.ContextCompat

import java.io.File
import java.util.HashMap

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class AndroidXStoragePlugin : FlutterPlugin, MethodCallHandler {
  private lateinit var channel: MethodChannel
  private lateinit var applicationContext: Context

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "android_x_storage")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success(android.os.Build.VERSION.SDK_INT)
      }
      "getExternalStorageDirectory" -> {
        result.success(Environment.getExternalStorageDirectory().toString())
      }
      "getMusicDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString())
      }
      "getAlarmsDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS).toString())
      }
      "getNotificationsDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS).toString())
      }
      "getPicturesDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString())
      }
      "getDCIMDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString())
      }
      "getMoviesDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString())
      }
      "getRingtonesDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).toString())
      }
      "getPodcastsDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).toString())
      }
      "getDownloadsDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString())
      }
      "getDocumentsDirectory" -> {
        result.success(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString())
      }
      "getSDCardStorageDirectory" -> {
        val sdCardPath = getSDCardPath()
        if (sdCardPath != null) {
          result.success(sdCardPath)
        } else {
          result.error("SDCardNotFound", "No SD card available", null)
        }
      }
      "getUSBStorageDirectories" -> {
        val usbPaths = getUSBPaths()
        result.success(usbPaths)
      }
      else -> {
        result.notImplemented()
      }
    }
  }
  @SuppressLint("PrivateApi")
  fun getStoragePath(context: Context, targetType: String): List<String> {
    val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    val paths = mutableListOf<String>()
    try {
      // 获取 StorageManager 的 getVolumes 方法
      val getVolumesMethod = StorageManager::class.java.getMethod("getVolumes")
      val volumeInfoList = getVolumesMethod.invoke(storageManager) as List<*>

      val volumeInfoClass = Class.forName("android.os.storage.VolumeInfo")
      val getTypeMethod = volumeInfoClass.getMethod("getType")
      val getDiskMethod = volumeInfoClass.getMethod("getDisk")
      val getPathMethod = volumeInfoClass.getMethod("getPath")

      // DiskInfo 相关
      val diskInfoClass = Class.forName("android.os.storage.DiskInfo")
      val isSdMethod = diskInfoClass.getMethod("isSd")
      val isUsbMethod = diskInfoClass.getMethod("isUsb")

      for (volume in volumeInfoList) {
        if (volume != null) {
          val type = getTypeMethod.invoke(volume) as Int
          // 只处理私有或可移除存储
          if (type == 0 || type == 1) {
            val disk = getDiskMethod.invoke(volume)
            if (disk != null) {
              val isSd = isSdMethod.invoke(disk) as Boolean
              val isUsb = isUsbMethod.invoke(disk) as Boolean
              val pathFile = getPathMethod.invoke(volume) as? File
              val path = pathFile?.absolutePath // 获取路径字符串

              if (targetType.equals("sd", true) && isSd && path!=null) {
                paths.add(path)
              }
              if (targetType.equals("usb", true) && isUsb && path!=null) {
                paths.add(path)
              }
            }
          }
        }
      }
      return paths;
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return paths // 未找到匹配的路径时返回 null
  }
  private fun getSDCardPath(): String? {
    val paths= getStoragePath(applicationContext,"sd")
    if(paths.isNotEmpty()){
      return paths[0]
    }
    return null
  }

  private fun getUSBPaths(): List<String> {
    return getStoragePath(applicationContext,"usb")
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
