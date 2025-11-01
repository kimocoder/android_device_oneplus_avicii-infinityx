/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Sreeshankar K <sreeshankar0910@gmail.com>
 */

package com.android.hbm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import com.android.hbm.R
import com.android.hbm.utils.HbmManager

class HbmService : Service(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var hbmManager: HbmManager
    private var isHbmCurrentlyEnabled = false
    
    companion object {
        private const val TAG = "HbmService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "hbm_service_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        hbmManager = HbmManager.getInstance(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        if (lightSensor != null) {
            sensorManager.registerListener(
                this,
                lightSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d(TAG, "Light sensor registered")
        } else {
            Log.e(TAG, "Light sensor not available")
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        // Turn off HBM when service stops
        if (isHbmCurrentlyEnabled) {
            hbmManager.setHbmEnabled(false)
        }
        Log.d(TAG, "Service destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            Log.d(TAG, "Current lux: $lux")
            
            // Check if mode is still auto
            val mode = hbmManager.getMode()
            if (mode != HbmManager.MODE_AUTO) {
                stopSelf()
                return
            }
            
            // Enable HBM if lux >= 10000, disable if below
            if (lux >= HbmManager.LUX_THRESHOLD && !isHbmCurrentlyEnabled) {
                hbmManager.setHbmEnabled(true)
                isHbmCurrentlyEnabled = true
                Log.d(TAG, "Auto HBM enabled at $lux lux")
            } else if (lux < HbmManager.LUX_THRESHOLD && isHbmCurrentlyEnabled) {
                hbmManager.setHbmEnabled(false)
                isHbmCurrentlyEnabled = false
                Log.d(TAG, "Auto HBM disabled at $lux lux")
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "HBM Auto Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Running to monitor ambient light for auto HBM"
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Auto HBM Active")
            .setContentText("Monitoring ambient light")
            .setSmallIcon(R.drawable.ic_hbm_auto)
            .build()
    }
}
