# HC-SR04 ultrasonic ranging module driver for Android Things

[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-hcsr04/maven-metadata.xml.svg?style=plastic)](https://jcenter.bintray.com/com/leinardi/android/things/driver-hcsr04/maven-metadata.xml)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-drivers/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-drivers)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/blob/master/LICENSE)

This driver supports the HC-SR04 ultrasonic ranging module.

NOTE: these drivers are not production-ready. They are offered as sample
implementations of Android Things user space drivers for common peripherals
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.

This driver is based on the [AndroidThings-UltrasonicSensor from Danesz](https://github.com/Danesz/AndroidThings-UltrasonicSensor).

## How to use the driver

### Gradle dependency

To use the `hcsr04` driver, simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the driver available on [jcenter][jcenter].

```
dependencies {
    implementation 'com.leinardi.android.things:driver-hcsr04:<version>'
}
```

### Sample usage

```java
public class DistanceActivity extends Activity implements SensorEventListener {
    private static final String TAG = DistanceActivity.class.getSimpleName();

    private Hcsr04SensorDriver mProximitySensorDriver;
    private SensorManager mSensorManager;

    private SensorManager.DynamicSensorCallback mDynamicSensorCallback = new SensorManager
            .DynamicSensorCallback() {
        @Override
        public void onDynamicSensorConnected(Sensor sensor) {
            if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
                mSensorManager.registerListener(DistanceActivity.this,
                        sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback);

        try {
            mProximitySensorDriver = new Hcsr04SensorDriver(trigPin, echoPin);
            mProximitySensorDriver.registerProximitySensor();
        } catch (IOException e) {
            // couldn't configure the device...
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProximitySensorDriver != null) {
            mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback);
            mSensorManager.unregisterListener(this);
            mProximitySensorDriver.unregisterProximitySensor();
            try {
                mProximitySensorDriver.close();
            } catch (IOException e) {
                // error closing sensor
            } finally {
                mProximitySensorDriver = null;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(TAG, String.format(Locale.getDefault(), "sensor changed: [%f]", event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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

[jcenter]: https://bintray.com/leinardi/androidthings/driver-hcsr04/_latestVersion
