# HD44780 LCD driver for Android Things

[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-hd44780/maven-metadata.xml.svg?style=plastic)](https://jcenter.bintray.com/com/leinardi/android/things/driver-hd44780/maven-metadata.xml)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-drivers/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-drivers)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/blob/master/LICENSE)

This driver supports LCD peripherals built on the HD44780 chip and controlled with the PCF8574 chip.

NOTE: these drivers are not production-ready. They are offered as sample
implementations of Android Things user space drivers for common peripherals
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.

This driver is based on the [lcd-pcf8574-androidthings driver from Nilhcem](https://github.com/Nilhcem/lcd-pcf8574-androidthings)
and the [hd44780-i2c driver from gorskima](https://github.com/gorskima/hd44780-i2c) 

## How to use the driver

### Gradle dependency

To use the `driver-hd44780` driver, simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the driver available on [jcenter][jcenter].

```
dependencies {
    implementation 'com.leinardi.android.things:driver-hd44780:<version>'
}
```

### Sample usage

```java
import com.leinardi.android.things.driver.hd44780.Hd44780;

// Access the LCD:
Hd44780 mLcd;

try {
    mLcd = new Hd44780(BoardDefaults.getI2CPort(), Hd44780.I2cAddress.PCF8574AT, Hd44780.Geometry.LCD_20X4);
} catch (IOException e) {
    // couldn't configure the LCD...
}

// Draw on the screen:

try {
     mLcd.setBacklight(true);
     mLcd.cursorHome();
     mLcd.clearDisplay();
     mLcd.setText("Hello LCD");
     int[] heart = {0b00000, 0b01010, 0b11111, 0b11111, 0b11111, 0b01110, 0b00100, 0b00000};
     mLcd.createCustomChar(heart, 0);
     mLcd.setCursor(10, 0);
     mLcd.writeCustomChar(0);
} catch (IOException e) {
    // error setting LCD
}

// Close the LCD when finished:

try {
    mLcd.close();
} catch (IOException e) {
    // error closing LCD
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

[jcenter]: https://bintray.com/leinardi/androidthings/driver-hd44780/_latestVersion
