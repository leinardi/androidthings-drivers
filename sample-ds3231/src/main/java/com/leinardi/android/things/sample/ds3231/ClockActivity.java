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

package com.leinardi.android.things.sample.ds3231;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.leinardi.android.things.driver.ds3231.Ds3231;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Activity that tests the Ds3231 real-time clock (RTC).
 */
public class ClockActivity extends Activity {
    private static final String TAG = ClockActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Date date;
        Ds3231 device;
        try {
            device = new Ds3231(BoardDefaults.getI2CPort());
            Log.d(TAG, "isTimekeepingDataValid = " + device.isTimekeepingDataValid());
            Log.d(TAG, "isOscillatorEnabled = " + device.isOscillatorEnabled());

            Calendar calendar = Calendar.getInstance();
            calendar.set(1982, Calendar.DECEMBER, 22);

            date = calendar.getTime();

            device.setTime(date);
            Log.d(TAG, "DateTime = " + date.toString());
            Log.d(TAG, "getTime = " + device.getTime().toString());
            device.setTime(date.getTime());
            Log.d(TAG, "getTime = " + device.getTime().toString());

            date = new Date(System.currentTimeMillis());
            device.setTime(date);
            Log.d(TAG, "DateTime = " + date.toString());
            Log.d(TAG, "getTime = " + device.getTime().toString());
            device.setTime(date.getTime());
            Log.d(TAG, "getTime = " + device.getTime().toString());

            Log.d(TAG, "readTemperature = " + device.readTemperature());

            // Close the device.
            device.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while opening screen", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
