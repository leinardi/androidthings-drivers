# DS3231 RTC driver for Android Things

[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-ds3231/maven-metadata.xml.svg?style=plastic)](https://jcenter.bintray.com/com/leinardi/android/things/driver-ds3231/maven-metadata.xml)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-drivers/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-drivers)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/blob/master/LICENSE)

This driver supports real-time clock (RTC) peripherals built on the DS3231 chip.

NOTE: these drivers are not production-ready. They are offered as sample
implementations of Android Things user space drivers for common peripherals
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.

This driver is based on the [DS3232RTC driver from JChristensen](https://github.com/JChristensen/DS3232RTC/blob/master/DS3232RTC.cpp),
the [Arduino-DS3231 driver from jarzebski](https://github.com/jarzebski/Arduino-DS3231),
the [ds3231 driver from rodan](https://github.com/rodan/ds3231),
and the [jpuderer-things-drivers driver from jpuderer](https://github.com/jpuderer/jpuderer-things-drivers)

### Driver's Supported features
 * Real-Time Clock Counts Seconds, Minutes, Hours, Date of the Month, Month, 
 Day of the Week, and Year, with Leap-Year Compensation Valid Up to 2100
 * Digital Temp Sensor
 * Register for Aging Trim
 * Two Time-of-Day Alarms
 * Programmable Square-Wave Output Signal

## How to use the driver

### Gradle dependency
#### Persist on reboot/poweroff system wall clock time
If you are just interested in persisting the system wall clock time when you reboot or
power off your device, simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the driver available on [jcenter][jcenter].  

_No additional code changes are required_.
```
dependencies {
    implementation 'com.leinardi.android.things:driver-ds3231-receiver:<version>'
}
  ```

#### Driver without receiver

If you are not interested in the automatic backup and restore feature, you can add just 
the `ds3231` driver:
```
dependencies {
    implementation 'com.leinardi.android.things:driver-ds3231:<version>'
}
```

### Sample usage

```java
public class ClockActivity extends Activity {
    private static final String TAG = ClockActivity.class.getSimpleName();
    private Ds3231 mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Date date;
        try {
            mDevice = new Ds3231(BoardDefaults.getI2CPort());
            Log.d(TAG, "isTimekeepingDataValid = " + mDevice.isTimekeepingDataValid());
            Log.d(TAG, "isOscillatorEnabled = " + mDevice.isOscillatorEnabled());

            Calendar calendar = Calendar.getInstance();
            calendar.set(1982, Calendar.DECEMBER, 22);

            date = calendar.getTime();

            Log.d(TAG, "DateTime = " + date.toString());
            mDevice.setTime(date);
            Log.d(TAG, "getTime = " + mDevice.getTime().toString());
            mDevice.setTime(date.getTime());
            Log.d(TAG, "getTime = " + mDevice.getTime().toString());

            date = new Date(System.currentTimeMillis());
            Log.d(TAG, "DateTime = " + date.toString());
            mDevice.setTime(date);
            Log.d(TAG, "getTime = " + mDevice.getTime().toString());
            mDevice.setTime(date.getTime());
            Log.d(TAG, "getTime = " + mDevice.getTime().toString());
            // Close the device.
            mDevice.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while opening screen", e);
            throw new RuntimeException(e);
        } finally {
            mDevice = null;
        }
    }
}
```

## License

Copyright 2018 Roberto Leinardi

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.

[jcenter]: https://bintray.com/leinardi/androidthings/driver-ds3231/_latestVersion
