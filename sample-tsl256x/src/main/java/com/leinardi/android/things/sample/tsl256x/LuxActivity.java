/*
 * Copyright 2018 Roberto Leinardi.
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
 */

package com.leinardi.android.things.sample.tsl256x;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.leinardi.android.things.driver.tsl256x.Tsl256xSensorDriver;

import java.io.IOException;
import java.util.Locale;

/**
 * LuxActivity is an example that use the driver for the TSL256x sensors.
 */
public class LuxActivity extends Activity implements SensorEventListener {
    private static final String TAG = LuxActivity.class.getSimpleName();

    private Tsl256xSensorDriver mLightSensorDriver;
    private SensorManager mSensorManager;

    private SensorManager.DynamicSensorCallback mDynamicSensorCallback = new SensorManager
            .DynamicSensorCallback() {
        @Override
        public void onDynamicSensorConnected(Sensor sensor) {
            if (sensor.getType() == Sensor.TYPE_LIGHT) {
                Log.i(TAG, "Light sensor connected");
                mSensorManager.registerListener(LuxActivity.this,
                        sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting LuxActivity");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback);

        try {
            mLightSensorDriver = new Tsl256xSensorDriver(BoardDefaults.getI2CPort());
            mLightSensorDriver.registerLightSensor();
        } catch (IOException e) {
            Log.e(TAG, "Error configuring sensor", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Closing sensor");
        if (mLightSensorDriver != null) {
            mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback);
            mSensorManager.unregisterListener(this);
            mLightSensorDriver.unregisterLightSensor();
            try {
                mLightSensorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing sensor", e);
            } finally {
                mLightSensorDriver = null;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(TAG, String.format(Locale.getDefault(), "sensor changed: [%f]", event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "sensor accuracy changed: " + accuracy);
    }
}
