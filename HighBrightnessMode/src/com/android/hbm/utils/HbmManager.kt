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

package com.android.hbm.utils

import android.content.Context
import android.provider.Settings
import android.util.Log
import java.io.File

class HbmManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "HbmManager"
        private const val HBM_NODE = "/sys/kernel/oplus_display/hbm"
        
        const val SETTING_HBM_MODE = "hbm_mode"
        const val SETTING_HBM_STATE = "hbm_state"
        
        const val MODE_AUTO = 0
        const val MODE_MANUAL = 1
        
        const val STATE_OFF = 0
        const val STATE_ON = 1
        const val STATE_AUTO = 2
        
        const val LUX_THRESHOLD = 10000
        
        @Volatile
        private var instance: HbmManager? = null
        
        fun getInstance(context: Context): HbmManager {
            return instance ?: synchronized(this) {
                instance ?: HbmManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    fun setHbmEnabled(enabled: Boolean): Boolean {
        return try {
            val value = if (enabled) "1" else "0"
            val file = File(HBM_NODE)
            
            if (!file.exists()) {
                Log.e(TAG, "HBM node does not exist: $HBM_NODE")
                return false
            }
            
            file.writeText(value)
            
            // Update state in settings
            val state = if (enabled) STATE_ON else STATE_OFF
            Settings.System.putInt(context.contentResolver, SETTING_HBM_STATE, state)
            
            Log.d(TAG, "HBM set to: $value")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting HBM", e)
            false
        }
    }
    
    fun getHbmEnabled(): Boolean {
        return try {
            val file = File(HBM_NODE)
            if (!file.exists()) {
                Log.e(TAG, "HBM node does not exist: $HBM_NODE")
                return false
            }
            file.readText().trim() == "1"
        } catch (e: Exception) {
            Log.e(TAG, "Error reading HBM", e)
            false
        }
    }
    
    fun getMode(): Int {
        return Settings.System.getInt(
            context.contentResolver,
            SETTING_HBM_MODE,
            MODE_MANUAL
        )
    }
    
    fun setMode(mode: Int) {
        Settings.System.putInt(context.contentResolver, SETTING_HBM_MODE, mode)
    }
    
    fun getState(): Int {
        return Settings.System.getInt(
            context.contentResolver,
            SETTING_HBM_STATE,
            STATE_OFF
        )
    }
    
    fun setState(state: Int) {
        Settings.System.putInt(context.contentResolver, SETTING_HBM_STATE, state)
    }
}
