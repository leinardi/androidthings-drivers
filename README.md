# Android Things user-space drivers 

[![GitHub tag](https://img.shields.io/github/tag/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/releases)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-drivers/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-drivers)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/blob/master/LICENSE)


Sample peripheral drivers for Android Things.

NOTE: these drivers are not production-ready. They are offered as sample
implementations of Android Things user space drivers for common peripherals
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.


# How to use a driver

For your convenience, drivers in this repository are also published to JCenter
as Maven artifacts. Look at their artifact and group ID in their build.gradle
and add them as dependencies to your own project.

For example, to use the `sh1106` driver, version `0.2`, simply add the line
below to your project's `build.gradle`:


```
dependencies {
    compile 'com.leinardi.android.things:driver-sh1106:0.2'
}
```


## Current contrib drivers

<!-- DRIVER_LIST_START -->
Driver | Type | Usage (add to your gradle dependencies) | Note
:---:|:---:| --- | ---
[driver-hcsr04](driver-hcsr04) | ultrasonic ranging module | `implementation 'com.leinardi.android.things:driver-hcsr04:0.1'` | [![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-hcsr04/maven-metadata.xml.svg)](https://jcenter.bintray.com/com/leinardi/android/things/driver-hcsr04/maven-metadata.xml) [changelog](driver-hcsr04/CHANGELOG.md) [sample](sample-hcsr04)
[driver-hd44780](driver-hd44780) | alphanumeric dot matrix LCD | `implementation 'com.leinardi.android.things:driver-hd44780:0.2'` | [![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-hd44780/maven-metadata.xml.svg)](https://jcenter.bintray.com/com/leinardi/android/things/driver-hd44780/maven-metadata.xml) [changelog](driver-hd44780/CHANGELOG.md) [sample](sample-hd44780)
[driver-lsm9ds1](driver-lsm9ds1) | 3D accelerometer, 3D gyroscope, 3D magnetometer and temperature sensor | `implementation 'com.leinardi.android.things:driver-lsm9ds1:0.3'` | [![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-lsm9ds1/maven-metadata.xml.svg)](https://jcenter.bintray.com/com/leinardi/android/things/driver-lsm9ds1/maven-metadata.xml) [changelog](driver-lsm9ds1/CHANGELOG.md) [sample](sample-lsm9ds1)
[driver-sh1106](driver-sh1106) | OLED display | `implementation 'com.leinardi.android.things:driver-sh1106:0.2'` | [![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-sh1106/maven-metadata.xml.svg)](https://jcenter.bintray.com/com/leinardi/android/things/driver-sh1106/maven-metadata.xml) [changelog](driver-sh1106/CHANGELOG.md) [sample](sample-sh1106)
<!-- DRIVER_LIST_END -->

## Change of group id and package name
If you are using the version `0.1` of `driver-lsm9ds1`, `driver-pcf8574-hd44780` or `driver-sh1106` please be aware that
the group ID and package name of these drives have been changed from `com.leinardi.androidthings` to `com.leinardi.android.things`.
If you want to use a version later than `0.1` you need to update your `build.gradle` to use the new group id and the imports in your Java/Kotlin files to use the new package name.
In addition to this, also the artifact ID of `driver-pcf8574-hd44780` has been changed to `driver-hd44780`.

## License

Copyright 2018 Roberto Leinardi.

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
