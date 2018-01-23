# TSL256x sensor driver for Android Things

[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-tsl256x/maven-metadata.xml.svg?style=plastic)](https://jcenter.bintray.com/com/leinardi/android/things/driver-tsl256x/maven-metadata.xml)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-drivers/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-drivers)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/blob/master/LICENSE)

This driver supports sensor peripherals built on the TSL256x chip.

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
// TBD
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
