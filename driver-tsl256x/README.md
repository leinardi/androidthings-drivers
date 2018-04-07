# TSL256x sensor driver for Android Things

[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-tsl256x/maven-metadata.xml.svg?style=plastic)](https://jcenter.bintray.com/com/leinardi/android/things/driver-tsl256x/maven-metadata.xml)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-drivers/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-drivers)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/blob/master/LICENSE)

This driver supports sensor peripherals built on the TSL256x chips (TSL2560, TSL2561, TSL2562 and TSL2563).

NOTE: these drivers are not production-ready. They are offered as sample
implementations of Android Things user space drivers for common peripherals
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.

This driver is based on the [Adafruit_TSL256x driver from adafruit](https://github.com/adafruit/Adafruit_TSL256x),
the [Adafruit_CircuitPython_TSL256x driver from adafruit](https://github.com/adafruit/Adafruit_CircuitPython_TSL256x)
and the [TSL256x_RaspberryPi_Library driver from akimach](https://github.com/akimach/TSL256x_RaspberryPi_Library)

## How to use the driver

### Gradle dependency

To use the `tsl256x` driver, simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the driver available on [jcenter][jcenter].

```
dependencies {
    implementation 'com.leinardi.android.things:driver-tsl256x:<version>'
}
```

### Sample usage

```java
public class LuxActivity extends Activity implements SensorEventListener {
    private static final String TAG = LuxActivity.class.getSimpleName();

    private Tsl256x mTsl256x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mTsl256x = new Tsl256x(BoardDefaults.getI2CPort(), 0x39);
            mTsl256x.setAutoGain(true);
            mTsl256x.setIntegrationTime(Tsl256x.IntegrationTime.INTEGRATION_TIME_402MS);
            int[] luminosities = mTsl256x.readLuminosity();
            Log.d(TAG, "Broadband luminosity = " + luminosities[0]);
            Log.d(TAG, "IR luminosity = " + luminosities[1]);
            Log.d(TAG, "Visible luminosity = " + luminosities[2]);
            float lux = mTsl256x.readLux();
            Log.d(TAG, "Lux = " + lux);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mTsl256x.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

To use it with the `SensorManager` check the [sample project](https://github.com/leinardi/androidthings-drivers/tree/tsl256x/sample-tsl256x).

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

[jcenter]: https://bintray.com/leinardi/androidthings/driver-tsl256x/_latestVersion
