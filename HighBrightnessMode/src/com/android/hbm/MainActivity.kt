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

package com.android.hbm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.android.hbm.service.HbmService
import com.android.hbm.utils.HbmManager

class MainActivity : Activity() {
    private lateinit var hbmManager: HbmManager
    private lateinit var modeGroup: RadioGroup
    private lateinit var autoRadio: RadioButton
    private lateinit var manualRadio: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hbmManager = HbmManager.getInstance(this)
        
        modeGroup = findViewById(R.id.mode_group)
        autoRadio = findViewById(R.id.radio_auto)
        manualRadio = findViewById(R.id.radio_manual)

        // Load current mode
        val currentMode = Settings.System.getInt(
            contentResolver,
            HbmManager.SETTING_HBM_MODE,
            HbmManager.MODE_MANUAL
        )

        when (currentMode) {
            HbmManager.MODE_AUTO -> autoRadio.isChecked = true
            HbmManager.MODE_MANUAL -> manualRadio.isChecked = true
        }

        modeGroup.setOnCheckedChangeListener { _, checkedId ->
            val newMode = when (checkedId) {
                R.id.radio_auto -> HbmManager.MODE_AUTO
                R.id.radio_manual -> HbmManager.MODE_MANUAL
                else -> HbmManager.MODE_MANUAL
            }

            Settings.System.putInt(
                contentResolver,
                HbmManager.SETTING_HBM_MODE,
                newMode
            )

            if (newMode == HbmManager.MODE_AUTO) {
                // Start service for auto mode
                val intent = Intent(this, HbmService::class.java)
                startForegroundService(intent)
                Toast.makeText(this, "Auto HBM enabled", Toast.LENGTH_SHORT).show()
            } else {
                // Stop service for manual mode
                val intent = Intent(this, HbmService::class.java)
                stopService(intent)
                Toast.makeText(this, "Manual HBM mode", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
