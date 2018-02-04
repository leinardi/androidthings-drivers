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

package com.leinardi.android.things.driver.epaperdriverhat;

import java.io.IOException;
import java.util.Arrays;

/**
 * Driver for controlling the GDEW075T8 E-Paper display.
 */
public class Gdew075t8Epd extends Epd {

    // Display resolution
    private static final int EPD_WIDTH = 640;
    private static final int EPD_HEIGHT = 384;
    // EPD7IN5 commands
    private static final int PANEL_SETTING = 0x00;
    private static final int POWER_SETTING = 0x01;
    private static final int POWER_OFF = 0x02;
    private static final int POWER_OFF_SEQUENCE_SETTING = 0x03;
    private static final int POWER_ON = 0x04;
    private static final int POWER_ON_MEASURE = 0x05;
    private static final int BOOSTER_SOFT_START = 0x06;
    private static final int DEEP_SLEEP = 0x07;
    private static final int DATA_START_TRANSMISSION_1 = 0x10;
    private static final int DATA_STOP = 0x11;
    private static final int DISPLAY_REFRESH = 0x12;
    private static final int IMAGE_PROCESS = 0x13;
    private static final int LUT_FOR_VCOM = 0x20;
    private static final int LUT_BLUE = 0x21;
    private static final int LUT_WHITE = 0x22;
    private static final int LUT_GRAY_1 = 0x23;
    private static final int LUT_GRAY_2 = 0x24;
    private static final int LUT_RED_0 = 0x25;
    private static final int LUT_RED_1 = 0x26;
    private static final int LUT_RED_2 = 0x27;
    private static final int LUT_RED_3 = 0x28;
    private static final int LUT_XON = 0x29;
    private static final int PLL_CONTROL = 0x30;
    private static final int TEMPERATURE_SENSOR_COMMAND = 0x40;
    private static final int TEMPERATURE_CALIBRATION = 0x41;
    private static final int TEMPERATURE_SENSOR_WRITE = 0x42;
    private static final int TEMPERATURE_SENSOR_READ = 0x43;
    private static final int VCOM_AND_DATA_INTERVAL_SETTING = 0x50;
    private static final int LOW_POWER_DETECTION = 0x51;
    private static final int TCON_SETTING = 0x60;
    private static final int TCON_RESOLUTION = 0x61;
    private static final int SPI_FLASH_CONTROL = 0x65;
    private static final int REVISION = 0x70;
    private static final int GET_STATUS = 0x71;
    private static final int AUTO_MEASUREMENT_VCOM = 0x80;
    private static final int READ_VCOM_VALUE = 0x81;
    private static final int VCM_DC_SETTING = 0x82;
    private static final int PARTIAL_WINDOW = 0x90; // Undocumented
    private static final int PARTIAL_IN = 0x91; // Undocumented
    private static final int PARTIAL_OUT = 0x92; // Undocumented
    private static final int FLASH_MODE = 0xE5; // Undocumented

    private static final int PANEL_SETTING_RES_640X480 = 0b0000_0000;
    private static final int PANEL_SETTING_RES_640X450 = 0b0100_0000;
    private static final int PANEL_SETTING_RES_640X448 = 0b1000_0000;
    private static final int PANEL_SETTING_RES_600X448 = 0b1100_0000;

    private static final int PANEL_SETTING_LUT_FROM_REGISTER = 0b0010_0000;
    // Source = width, gate = height
    /**
     * First line to last: G1→ ......→Gn
     */
    private static final int PANEL_SETTING_GATE_SCAN_UP = 0b0000_1000;
    /**
     * First data to last data: S1→......→Sn
     */
    private static final int PANEL_SETTING_SOURCE_SHIFT_RIGHT = 0b0000_0100;
    private static final int PANEL_SETTING_DC_DC_CONVERTER_ON = 0b0000_0010;
    private static final int PANEL_SETTING_NORMAL_OPERATION = 0b0000_0001;

    private static final int POWER_SETTING_VDNS_L = 0b0011_0000;
    private static final int POWER_SETTING_VSOURCE_LV_EN_INTERNAL_DC_DC = 0b0000_0100;
    private static final int POWER_SETTING_VSOURCE_INTERNAL_DC_DC = 0b0000_0010;
    private static final int POWER_SETTING_VGATE_INTERNAL_DC_DC = 0b0000_0001;
    private static final int POWER_SETTING_VGHL_PLUS_MINUS_20V = 0b0000_0000;

    private static final int BOOSTER_SOFT_START_PHASE_PERIOD_10MS = 0b0000_0000;
    private static final int BOOSTER_SOFT_START_PHASE_PERIOD_20MS = 0b0100_0000;
    private static final int BOOSTER_SOFT_START_PHASE_PERIOD_30MS = 0b1000_0000;
    private static final int BOOSTER_SOFT_START_PHASE_PERIOD_40MS = 0b1100_0000;
    private static final int BOOSTER_SOFT_START_DRIVING_STRENGTH_1 = 0b0000_0000;
    private static final int BOOSTER_SOFT_START_DRIVING_STRENGTH_2 = 0b0000_1000;
    private static final int BOOSTER_SOFT_START_DRIVING_STRENGTH_3 = 0b0001_0000;
    private static final int BOOSTER_SOFT_START_DRIVING_STRENGTH_4 = 0b0001_1000;
    private static final int BOOSTER_SOFT_START_DRIVING_STRENGTH_5 = 0b0010_0000;
    private static final int BOOSTER_SOFT_START_DRIVING_STRENGTH_6 = 0b0010_1000;
    private static final int BOOSTER_SOFT_START_DRIVING_STRENGTH_7 = 0b0011_0000;
    private static final int BOOSTER_SOFT_START_DRIVING_STRENGTH_8 = 0b0011_1000;
    private static final int BOOSTER_SOFT_START_MIN_OFF_TIME_0_26 = 0b0000_0000;
    private static final int BOOSTER_SOFT_START_MIN_OFF_TIME_0_31 = 0b0000_0001;
    private static final int BOOSTER_SOFT_START_MIN_OFF_TIME_0_36 = 0b0000_0010;
    private static final int BOOSTER_SOFT_START_MIN_OFF_TIME_0_52 = 0b0000_0011;
    private static final int BOOSTER_SOFT_START_MIN_OFF_TIME_0_77 = 0b0000_0100;
    private static final int BOOSTER_SOFT_START_MIN_OFF_TIME_1_61 = 0b0000_0101;
    private static final int BOOSTER_SOFT_START_MIN_OFF_TIME_3_43 = 0b0000_0110;
    private static final int BOOSTER_SOFT_START_MIN_OFF_TIME_6_77 = 0b0000_0111;
    private static final int TEMPERATURE_CALIBRATION_INTERNAL = 0b0000_0000;
    private static final int TEMPERATURE_CALIBRATION_EXTERNAL = 0b1000_0000;

    private static final int PLL_CONTROL_50HZ = 0b0011_1100; // See datasheet page 17

    private static final int VCOM_AND_DATA_INTERVAL_SETTING_DEFAULT_WHITE_BALCK = 0b0110_0111; // See datasheet page 19
    private static final int VCOM_AND_DATA_INTERVAL_SETTING_INVERT_COLORS = 0b0001_0000; // See datasheet page 19

    private static final int TCON_SETTING_DEFAULT_S2G_G2S = 0b0010_0010; // See datasheet page 20

    private static final int DEEP_SLEEP_CHECK_CODE = 0b1010_0101;

    private static final int SPI_FLASH_CONTROL_ENABLED = 0b0000_00001;
    private static final int SPI_FLASH_CONTROL_DISABLED = 0b0000_00000;

    private static final int PIXEL_ON = 0b011;
    private static final int PARTIAL_UPDATE_DELAY = 500;

    private byte[] mBuffer;
    private boolean mInvertColor;

    public Gdew075t8Epd() throws IOException {
    }

    public Gdew075t8Epd(String spiBusPort, String resetPin, String dataCommandPin, String busyPin) throws IOException {
        super(spiBusPort, resetPin, dataCommandPin, busyPin);
    }

    @Override
    protected void configure() throws IOException {
        super.configure();
        mBuffer = new byte[((getDisplayWidth() * getDisplayHeight()) / 2)];
    }

    @Override
    public void clearPixels() {
        Arrays.fill(mBuffer, 0, mBuffer.length, (byte) 0);
    }

    @Override
    public void setPixel(int x, int y, boolean on) throws IllegalArgumentException {
        if (x < 0 || y < 0 || x >= getDisplayWidth() || y >= getDisplayHeight()) {
            throw new IllegalArgumentException("pixel out of bound:" + x + "," + y);
        }
        if (on) {
            mBuffer[x / 2 + (y * getDisplayWidth() / 2)] |= (x % 2 == 0 ? (PIXEL_ON << 4) : PIXEL_ON);
        } else {
            mBuffer[x / 2 + (y * getDisplayWidth() / 2)] &= ~(x % 2 == 0 ? (PIXEL_ON << 4) : PIXEL_ON);
        }
    }

    @Override
    public void setInvertDisplay(boolean invert) throws IOException, IllegalStateException {
        mInvertColor = invert;
    }

    @Override
    public int getDisplayWidth() {
        return EPD_WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return EPD_HEIGHT;
    }

    @Override
    public void show() throws IOException {
        wakeUp();
        sendCommand(DATA_START_TRANSMISSION_1);
        sendData(mBuffer);
        sendCommand(DISPLAY_REFRESH);
        waitUntilIdle();
        sleep();
    }

    /**
     * On this display, partial update to display RAM works, but refresh is full screen
     */
    private void show(int left, int top, int right, int bottom) throws IOException {
        int width = right - left;
        int height = bottom - top;

        byte[] buffer = new byte[((width * height) + 1) / 2];
        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                byte b = mBuffer[x / 2 + (y * getDisplayWidth() / 2)];
                if (left == right - 1 && left % 2 == 0) {
                    b &= ~(0xF);
                }
                buffer[((x - left) / 2 + (((y - top) * width) + 1) / 2)] |= (x % 2 == 0 ? (b << 4) : b);
            }
        }

        left &= ~(0b0000_0111); // byte boundary
        right = (right - 1) | 0b0000_0111; // byte boundary - 1
        sendCommand(PARTIAL_IN);
        sendCommand(PARTIAL_WINDOW);
        sendData((byte) (left / 256));
        sendData((byte) (left % 256));
        sendData((byte) (right / 256));
        sendData((byte) (right % 256));
        sendData((byte) (top / 256));
        sendData((byte) (top % 256));
        sendData((byte) (bottom / 256));
        sendData((byte) (bottom % 256));
        //sendData(0x01); // Gates scan both inside and outside of the partial window. (default)
        sendData((byte) 0x00); // Gates scan only inside of the partial window

        sendCommand(DATA_START_TRANSMISSION_1);
        sendData(buffer);
        sendCommand(DISPLAY_REFRESH);
        sendCommand(PARTIAL_OUT);
    }

    @Override
    protected void wakeUp() throws IOException {
        reset();

        sendCommand(SPI_FLASH_CONTROL);
        sendData((byte) SPI_FLASH_CONTROL_ENABLED);

        sendCommand(0xAB);

        sendCommand(SPI_FLASH_CONTROL);
        sendData((byte) SPI_FLASH_CONTROL_DISABLED);

        sendCommand(POWER_SETTING);
        sendData((byte) (POWER_SETTING_VDNS_L
                | POWER_SETTING_VSOURCE_LV_EN_INTERNAL_DC_DC
                | POWER_SETTING_VSOURCE_INTERNAL_DC_DC
                | POWER_SETTING_VGATE_INTERNAL_DC_DC));     //0b0011_0111
        sendData((byte) POWER_SETTING_VGHL_PLUS_MINUS_20V);

        sendCommand(PANEL_SETTING);
        sendData((byte) (PANEL_SETTING_RES_600X448
                | PANEL_SETTING_GATE_SCAN_UP
                | PANEL_SETTING_SOURCE_SHIFT_RIGHT
                | PANEL_SETTING_DC_DC_CONVERTER_ON
                | PANEL_SETTING_NORMAL_OPERATION));         // 0b1100_1111
        sendData((byte) 0b0000_1000);                       // What's the purpose of this?

        sendCommand(BOOSTER_SOFT_START);
        sendData((byte) (BOOSTER_SOFT_START_PHASE_PERIOD_40MS
                | BOOSTER_SOFT_START_DRIVING_STRENGTH_1
                | BOOSTER_SOFT_START_MIN_OFF_TIME_6_77));   // 0b1100_0111
        sendData((byte) (BOOSTER_SOFT_START_PHASE_PERIOD_40MS
                | BOOSTER_SOFT_START_DRIVING_STRENGTH_2
                | BOOSTER_SOFT_START_MIN_OFF_TIME_0_77));   // 0b1100_1100
        sendData((byte) (BOOSTER_SOFT_START_DRIVING_STRENGTH_6
                | BOOSTER_SOFT_START_MIN_OFF_TIME_0_26));   // 0b0010_1000

        sendData((byte) PLL_CONTROL_50HZ);

        sendCommand(TEMPERATURE_CALIBRATION);
        sendData((byte) TEMPERATURE_CALIBRATION_INTERNAL);

        sendCommand(VCOM_AND_DATA_INTERVAL_SETTING);
        int vcomAndDataIntervalSetting = VCOM_AND_DATA_INTERVAL_SETTING_DEFAULT_WHITE_BALCK;
        if (mInvertColor) {
            vcomAndDataIntervalSetting |= VCOM_AND_DATA_INTERVAL_SETTING_INVERT_COLORS;
        }
        sendData((byte) vcomAndDataIntervalSetting);

        sendCommand(TCON_SETTING);
        sendData((byte) TCON_SETTING_DEFAULT_S2G_G2S);

        sendCommand(TCON_RESOLUTION);
        sendData((byte) ((EPD_WIDTH >> 8) & 0xFF));     // 0b0000_0010
        sendData((byte) (EPD_WIDTH & 0xFF));            // 0b1000_0000
        sendData((byte) ((EPD_HEIGHT >> 8) & 0xFF));    // 0b0000_0001;
        sendData((byte) (EPD_HEIGHT & 0xFF));           // 0b1000_0000;

        sendCommand(VCM_DC_SETTING);
        sendData((byte) 0b0001_1110); // decide by LUT file;

        sendCommand(FLASH_MODE);
        sendData((byte) 0x03);

        sendCommand(POWER_ON);
        waitUntilIdle();
    }

    @Override
    protected void sleep() throws IOException {
        sendCommand(SPI_FLASH_CONTROL);
        sendData((byte) SPI_FLASH_CONTROL_ENABLED);

        sendCommand(0xB9);

        sendCommand(SPI_FLASH_CONTROL);
        sendData((byte) SPI_FLASH_CONTROL_DISABLED);

        sendCommand(POWER_OFF);
        waitUntilIdle();
        sendCommand(DEEP_SLEEP);
        sendData((byte) DEEP_SLEEP_CHECK_CODE);
    }

    @Override
    protected boolean isBusy() throws IOException {
        return !getBusyPinValue();
    }
}
