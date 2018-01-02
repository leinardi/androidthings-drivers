/*
 * Copyright 2018 Roberto Leinardi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leinardi.android.things.sample.hd44780;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.leinardi.android.things.driver.hd44780.Hd44780;

import java.io.IOException;

/**
 * Activity that tests the HD44780 LCD.
 */
public class LcdActivity extends Activity {
    private static final String TAG = LcdActivity.class.getSimpleName();
    private static final int LCD_COLS = 20;
    private static final int LCD_ROWS = 4;
    private Hd44780 mLcd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mLcd = new Hd44780(BoardDefaults.getI2CPort(), Hd44780.I2cAddress.PCF8574AT, Hd44780.Geometry.LCD_20X4);
        } catch (IOException e) {
            Log.e(TAG, "Error while opening LCD", e);
            throw new RuntimeException(e);
        }
        Log.d(TAG, "LCD activity created");

        showText();
    }

    private void showText() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        mLcd.setBacklight(true);
                        mLcd.cursorHome();
                        mLcd.clearDisplay();
                        mLcd.setText("Hello LCD");
                        int[] heart = {0b00000, 0b01010, 0b11111, 0b11111, 0b11111, 0b01110, 0b00100, 0b00000};
                        mLcd.createCustomChar(heart, 0);
                        mLcd.setCursor(10, 0);
                        mLcd.writeCustomChar(0); // write :heart: custom character previously stored in location 0
                        delay(2);

                        mLcd.clearDisplay();
                        mLcd.setText("Backlight Off");
                        mLcd.setBacklight(false);
                        delay(2);
                        mLcd.clearDisplay();
                        mLcd.setText("Backlight On");
                        mLcd.setBacklight(true);
                        delay(2);

                        mLcd.clearDisplay();
                        mLcd.setText("Cursor On");
                        mLcd.setCursorOn(true);
                        delay(2);

                        mLcd.clearDisplay();
                        mLcd.setText("Cursor Blink");
                        mLcd.setBlinkOn(true);
                        delay(2);

                        mLcd.clearDisplay();
                        mLcd.setText("Cursor OFF");
                        mLcd.setBlinkOn(false);
                        mLcd.setCursorOn(false);
                        delay(2);

                        mLcd.clearDisplay();
                        mLcd.setText("Display Off");
                        mLcd.setDisplayOn(false);
                        delay(2);

                        mLcd.clearDisplay();
                        mLcd.setText("Display On");
                        mLcd.setDisplayOn(true);
                        delay(2);

                        mLcd.clearDisplay();
                        for (int i = 0; i < LCD_ROWS; i++) {
                            mLcd.setCursor(0, i);
                            mLcd.setText("-+* line " + i + " *+-");
                        }
                        delay(2);

                        mLcd.scrollDisplayLeft();
                        delay(2);

                        mLcd.scrollDisplayLeft();
                        delay(2);

                        mLcd.scrollDisplayLeft();
                        delay(2);

                        mLcd.scrollDisplayRight();
                        delay(2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void delay(long s) {
        SystemClock.sleep(s * 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close the device.
        try {
            mLcd.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing Hd44780", e);
        } finally {
            mLcd = null;
        }
    }
}
