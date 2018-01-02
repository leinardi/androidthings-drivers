# LSM9DS1 sensor driver for Android Things

[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-lsm9ds1/maven-metadata.xml.svg?style=plastic)](https://jcenter.bintray.com/com/leinardi/android/things/driver-lsm9ds1/maven-metadata.xml)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-drivers/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-drivers)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/blob/master/LICENSE)

This driver supports sensor peripherals built on the LSM9DS1 chip.

NOTE: these drivers are not production-ready. They are offered as sample
implementations of Android Things user space drivers for common peripherals
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.

This driver is based on the [Adafruit_LSM9DS1 driver from adafruit](https://github.com/adafruit/Adafruit_LSM9DS1),
the [Adafruit_CircuitPython_LSM9DS1 driver from adafruit](https://github.com/adafruit/Adafruit_CircuitPython_LSM9DS1)
and the [LSM9DS1_RaspberryPi_Library driver from akimach](https://github.com/akimach/LSM9DS1_RaspberryPi_Library)

## How to use the driver

### Gradle dependency

To use the `lsm9ds1` driver, simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the driver available on [jcenter][jcenter].

```
dependencies {
    implementation 'com.leinardi.android.things:driver-lsm9ds1:<version>'
}
```

### Sample usage

```java
import com.leinardi.android.things.driver.lsm9ds1.Lsm9ds1;

// Access the sensor:

Lsm9ds1 mLsm9ds1;

try {
    mLsm9ds1 = new Lsm9ds1.Builder(bus) // All the following setters are optional
        .setI2cAddressAccelGyro(Lsm9ds1.I2C_ADDRESS_ACCEL_GYRO)
        .setI2cAddressMag(Lsm9ds1.I2C_ADDRESS_MAG)
        .setAccelerometerDecimation(Lsm9ds1.AccelerometerDecimation.ACCEL_DEC_0_SAMPLES)
        .setAccelerometerEnabledAxes(Lsm9ds1.ACCEL_AXIS_X | Lsm9ds1.ACCEL_AXIS_Y | Lsm9ds1.ACCEL_AXIS_Z)
        .setAccelerometerHighResolution(true)
        .setAccelerometerOdr(Lsm9ds1.AccelGyroOutputDataRate.ODR_952HZ)
        .setAccelerometerRange(Lsm9ds1.AccelerometerRange.ACCEL_RANGE_2G)
        .setFifoMemoryEnabled(false)
        .setFifoModeAndTreshold(Lsm9ds1.FifoMode.FIFO_OFF, Lsm9ds1.FIFO_MAX_THRESHOLD)
        .setGyroscopeOdr(Lsm9ds1.AccelGyroOutputDataRate.ODR_952HZ)
        .setGyroscopeScale(Lsm9ds1.GyroscopeScale.GYRO_SCALE_245DPS)
        .setMagnetometerGain(Lsm9ds1.MagnetometerGain.MAG_GAIN_4GAUSS)
        .setMagnetometerSystemOperatingMode(MAG_CONTINUOUS_CONVERSION)
        .setMagnetometerTemperatureCompensation(true)
        .setMagnetometerXYOperatingMode(Lsm9ds1.MagnetometerXYOperatingMode.MAG_XY_OM_ULTRA_HIGH_PERFORMANCE)
        .setMagnetometerZOperatingMode(Lsm9ds1.MagnetometerZOperatingMode.MAG_Z_OM_ULTRA_HIGH_PERFORMANCE)
        .build();
} catch (IOException e) {
    // couldn't configure the device...
}

// Read the current data:

try {
    float[] acceleration = mLsm9ds1.readAcceleration();
    float[] angularVelocity = mLsm9ds1.readAngularVelocity();
    float[] magneticInduction = mLsm9ds1.readMagneticInduction();
    float temperature = mLsm9ds1.readTemperature();
} catch (IOException e) {
    // error reading data
}

// Close the sensor when finished:

try {
    mLsm9ds1.close();
} catch (IOException e) {
    // error closing sensor
}
```

To use it with the `SensorManager` check the [sample project](https://github.com/leinardi/androidthings-drivers/tree/lsm9ds1/sample-lsm9ds1).

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

[jcenter]: https://bintray.com/leinardi/androidthings/driver-lsm9ds1/_latestVersion
