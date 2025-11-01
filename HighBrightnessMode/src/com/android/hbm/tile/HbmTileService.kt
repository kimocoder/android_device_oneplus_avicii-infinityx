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

package com.android.hbm.tile

import android.content.Intent
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.android.hbm.R
import com.android.hbm.service.HbmService
import com.android.hbm.utils.HbmManager

class HbmTileService : TileService() {
    
    private lateinit var hbmManager: HbmManager
    
    companion object {
        private const val TAG = "HbmTileService"
    }
    
    override fun onCreate() {
        super.onCreate()
        hbmManager = HbmManager.getInstance(this)
    }
    
    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }
    
    override fun onClick() {
        super.onClick()
        
        val currentState = hbmManager.getState()
        val mode = hbmManager.getMode()
        
        // Cycle through states: OFF -> ON -> AUTO -> OFF
        val newState = when (currentState) {
            HbmManager.STATE_OFF -> HbmManager.STATE_AUTO
            HbmManager.STATE_AUTO -> HbmManager.STATE_ON
            HbmManager.STATE_ON -> HbmManager.STATE_OFF
            else -> HbmManager.STATE_OFF
        }
        
        when (newState) {
            HbmManager.STATE_OFF -> {
                hbmManager.setHbmEnabled(false)
                hbmManager.setState(HbmManager.STATE_OFF)
                hbmManager.setMode(HbmManager.MODE_MANUAL)
                // Stop service
                val intent = Intent(this, HbmService::class.java)
                stopService(intent)
            }
            HbmManager.STATE_ON -> {
                hbmManager.setHbmEnabled(true)
                hbmManager.setState(HbmManager.STATE_ON)
                hbmManager.setMode(HbmManager.MODE_MANUAL)
                // Stop service
                val intent = Intent(this, HbmService::class.java)
                stopService(intent)
            }
            HbmManager.STATE_AUTO -> {
                hbmManager.setState(HbmManager.STATE_AUTO)
                hbmManager.setMode(HbmManager.MODE_AUTO)
                // Start service
                val intent = Intent(this, HbmService::class.java)
                startForegroundService(intent)
            }
        }
        
        updateTile()
    }
    
    private fun updateTile() {
        val tile = qsTile ?: return
        val state = hbmManager.getState()
        
        when (state) {
            HbmManager.STATE_OFF -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "HBM"
                tile.subtitle = "Off"
                tile.icon = Icon.createWithResource(this, R.drawable.ic_hbm_off)
            }
            HbmManager.STATE_ON -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "HBM"
                tile.subtitle = "On"
                tile.icon = Icon.createWithResource(this, R.drawable.ic_hbm_on)
            }
            HbmManager.STATE_AUTO -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "HBM"
                tile.subtitle = "Auto"
                tile.icon = Icon.createWithResource(this, R.drawable.ic_hbm_auto)
            }
        }
        
        tile.updateTile()
    }
}
