# DS3231 automatic system wall clock time backup and restore for Android Things

[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-ds3231-receiver/maven-metadata.xml.svg?style=plastic)](https://jcenter.bintray.com/com/leinardi/android/things/driver-ds3231-receiver/maven-metadata.xml)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-drivers/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-drivers)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/blob/master/LICENSE)

This library automatically persist on reboot/poweroff the system wall clock time,
using the DS3231 driver, without the need of any code change.

NOTE: these drivers are not production-ready. They are offered as sample
implementations of Android Things user space drivers for common peripherals
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.

This driver is based on the [jpuderer-things-drivers driver from jpuderer](https://github.com/jpuderer/jpuderer-things-drivers)

## How to use the driver

### Gradle dependency
If you are just interested in persisting the system wall clock time when you reboot or
power off your device, simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the driver available on [jcenter][jcenter].
No additional code changes are required.
```
dependencies {
    implementation 'com.leinardi.android.things:driver-ds3231-receiver:<version>'
}
  ```

### Specify the I2C port name
The I2C port name is automatically detected for any officially supported Android Things board.
You can manually specify the port name by adding the following meta-data tag in your 
application manifest, where the value corresponds to the I2C bus the RTC module is attached to:

```xml
    <meta-data android:name="ds3231_i2c_port" android:value="I2C1" />
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

[jcenter]: https://bintray.com/leinardi/androidthings/driver-ds3231-receiver/_latestVersion
