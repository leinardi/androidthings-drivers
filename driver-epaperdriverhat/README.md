# E-Paper Driver HAT display driver for Android Things

[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/driver-epaperdriverhat/maven-metadata.xml.svg?style=plastic)](https://jcenter.bintray.com/com/leinardi/android/things/driver-epaperdriverhat/maven-metadata.xml)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-drivers/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-drivers)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-drivers.svg?style=plastic)](https://github.com/leinardi/androidthings-drivers/blob/master/LICENSE)

This driver supports E-Paper screen peripherals connected with an E-Paper Driver HAT.

## Supported displays

<!-- DISPLAY_LIST_START -->
Part Number | Class | Size | Color | Resolution | Interface
:---:|:---:| --- | --- | --- | --- 
[GDEW075T8](https://www.waveshare.com/wiki/7.5inch_e-Paper_HAT) | `Gdew075t8Epd` | 7.5" | Black, White | 640x384 | SPI
<!-- DISPLAY_LIST_END -->

NOTE: these drivers are not production-ready. They are offered as sample
implementations of Android Things user space drivers for common peripherals
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.

This driver is based on the [GxEPD driver from ZinggJM](https://github.com/ZinggJM/GxEPD) 

## How to use the driver

### Gradle dependency

To use the `epaperdriverhat` driver, simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the driver available on [jcenter][jcenter].

```
dependencies {
    implementation 'com.leinardi.android.things:driver-epaperdriverhat:<version>'
}
```

### Sample usage

```java
public class EpdScreenActivity extends Activity {
    private static final String TAG = EpdScreenActivity.class.getSimpleName();
    Gdew075t8Epd mEpd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mEpd = new Gdew075t8Epd();
            // Show checkerboard
            for (int i = 0; i < mEpd.getDisplayWidth(); i++) {
                for (int j = 0; j < mEpd.getDisplayHeight(); j++) {
                    mEpd.setPixel(i, j, (i % 2) == (j % 2));
                }
            }
            mEpd.show();
        } catch (IOException e) {
            Log.e(TAG, "Error initializing EPD", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mEpd.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception closing EPD", e);
        } finally {
            mEpd = null;
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

[jcenter]: https://bintray.com/leinardi/androidthings/driver-epaperdriverhat/_latestVersion
