# Android Things user-space drivers 

[![GitHub tag](https://img.shields.io/github/tag/leinardi/android.things-drivers.svg?style=plastic)](https://github.com/leinardi/android.things-drivers/releases)
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
[driver-lsm9ds1](driver-lsm9ds1) | 3D accelerometer, 3D gyroscope, 3D magnetometer and temperature sensor | `implementation 'com.leinardi.android.things:driver-lsm9ds1:0.2'` | [![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-lsm9ds1/maven-metadata.xml.svg)](https://jcenter.bintray.com/com/leinardi/android/things/driver-lsm9ds1/maven-metadata.xml) [changelog](driver-lsm9ds1/CHANGELOG.md) [sample](sample-lsm9ds1)
[driver-hd44780](driver-hd44780) | alphanumeric dot matrix LCD | `implementation 'com.leinardi.android.things:driver-hd44780:0.2'` | [![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-hd44780/maven-metadata.xml.svg)](https://jcenter.bintray.com/com/leinardi/android/things/driver-hd44780/maven-metadata.xml) [changelog](driver-hd44780/CHANGELOG.md) [sample](sample-hd44780)
[driver-sh1106](driver-sh1106) | OLED display | `implementation 'com.leinardi.android.things:driver-sh1106:0.2'` | [![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-sh1106/maven-metadata.xml.svg)](https://jcenter.bintray.com/com/leinardi/android/things/driver-sh1106/maven-metadata.xml) [changelog](driver-sh1106/CHANGELOG.md) [sample](sample-sh1106)
<!-- DRIVER_LIST_END -->

## License

Copyright 2017 Roberto Leinardi.

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
