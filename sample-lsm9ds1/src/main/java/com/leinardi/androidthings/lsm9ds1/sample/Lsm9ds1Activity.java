/*
 * Copyright 2017 Roberto Leinardi.
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

package com.leinardi.androidthings.lsm9ds1.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1;

import java.io.IOException;

/**
 * Activity that tests the Lsm9ds1 sensor.
 */
public class Lsm9ds1Activity extends Activity {
    private static final String TAG = Lsm9ds1Activity.class.getSimpleName();

    private Lsm9ds1 mLsm9ds1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mLsm9ds1 = new Lsm9ds1(BoardDefaults.getI2CPort());
        } catch (IOException e) {
            Log.e(TAG, "Error while opening screen", e);
            throw new RuntimeException(e);
        }
        Log.d(TAG, "Lsm9ds1 activity created");

        try {
            mLsm9ds1.getAccelerometerRange();
            mLsm9ds1.getMagnetometerGain();
            mLsm9ds1.getGyroscopeScale();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close the device.
        try {
            mLsm9ds1.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing LSM9DS1", e);
        } finally {
            mLsm9ds1 = null;
        }
    }
}
