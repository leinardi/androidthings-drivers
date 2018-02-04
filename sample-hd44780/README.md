## OLED Screen sample for Android Things

This sample demonstrates how to control the HD44780 LCD using PCF8574's I2C with
Android Things.

## Pre-requisites

- Android Things compatible board
- Android Studio 3.1+
- 1 [HD44780 LCD](https://www.sparkfun.com/datasheets/LCD/HD44780.pdf)
- 1 [PCF8574 I2C 8-bit expander](https://www.ti.com/lit/ds/symlink/pcf8574.pdf)
- jumper wires
- 1 breadboard


# Build and install

On Android Studio, click on the "Run" button.

If you prefer to run on the command line, from this repository's root directory, type

```bash
./gradlew sample-hd44780:installDebug
adb shell am start com.leinardi.android.things.sample.hd44780/.LcdActivity
```

If you have everything set up correctly, you will some example text on the LCD showing
different features of the driver (print text, custom characters, turning the backlight
and display on and of, etc).

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
