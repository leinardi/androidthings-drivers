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

package com.leinardi.android.things.driver.ds3231.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.things.device.TimeManager;
import com.leinardi.android.things.driver.ds3231.Ds3231;

import java.io.IOException;

/*
 * This class is inspired by the RtcReceiver.java from jpuderer: https://goo.gl/byY8dJ
 */
public class Ds3231Receiver extends BroadcastReceiver {
    private static final String TAG = Ds3231Receiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            handleBootComplete(context);
        } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
            handleTimeChanged(context);
        } else {
            Log.e(TAG, "Unexpected broadcast action: " + action);
        }
    }

    private void handleBootComplete(Context context) {
        final String i2cPort = getI2cName(context);
        if (!TextUtils.isEmpty(i2cPort)) {
            try {
                Ds3231 ds3231 = new Ds3231(i2cPort);
                long ds3231Timestamp = ds3231.getTimeInMillis();
                long systemTimeStamp = System.currentTimeMillis();
                // If the DS3231 has a sane timestamp, set the system clock using the DS3231.
                // Otherwise, set the DS3231 using the system time if the system time appears sane
                if (isSaneTimestamp(ds3231Timestamp)) {
                    Log.i(TAG, "Setting system clock using DS3231");
                    TimeManager timeManager = TimeManager.getInstance();
                    timeManager.setTime(ds3231Timestamp);

                    // Re-enable NTP updates.  The call to setTime() disables them automatically,
                    // but that's what we use to update our DS3231.
                    timeManager.setAutoTimeEnabled(true);
                } else if (isSaneTimestamp(systemTimeStamp)) {
                    Log.i(TAG, "Setting DS3231 time using system clock");
                    ds3231.setTime(systemTimeStamp);
                }
                ds3231.close();
            } catch (IOException e) {
                Log.e(TAG, "Error accessing DS3231", e);
            }
        }
    }

    private void handleTimeChanged(Context context) {
        final String i2cPort = getI2cName(context);
        if (!TextUtils.isEmpty(i2cPort)) {
            try {
                Ds3231 ds3231 = new Ds3231(i2cPort);
                long timestamp = System.currentTimeMillis();
                if (isSaneTimestamp(timestamp)) {
                    Log.i(TAG, "Time changed. Setting DS3231 time using system clock");
                    ds3231.setTime(timestamp);
                } else {
                    Log.w(TAG, "Time changed. Ignoring non-sane timestamp");
                }
                ds3231.close();
            } catch (IOException e) {
                Log.e(TAG, "Error accessing DS3231", e);
            }
        }
    }

    // Assume timestamp is not sane if the timestamp predates the build time
    // of this image.  Borrowed this logic from AlarmManagerService
    private boolean isSaneTimestamp(long timestamp) {
        final long systemBuildTime = Environment.getRootDirectory().lastModified();
        return timestamp >= systemBuildTime;
    }

    private String getI2cName(Context context) {
        String i2cPort = null;
        try {
            ApplicationInfo info =
                    context.getPackageManager().getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = info.metaData;
            i2cPort = bundle.getString("ds3231_i2c_port");
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.w(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        if (i2cPort == null) {
            i2cPort = BoardDefaults.getI2CPort();
        }
        if (i2cPort == null) {
            Log.e(TAG, "Unable to get I2C port name.");
        }
        Log.d(TAG, "i2cPort = " + i2cPort);
        return i2cPort;
    }
}
