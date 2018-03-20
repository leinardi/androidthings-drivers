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

package com.leinardi.android.things.driver.sh1106;

import android.graphics.Bitmap;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

/**
 * Driver for controlling the SH1106 OLED display.
 */
@SuppressWarnings("WeakerAccess")
public class Sh1106 implements Closeable {
    private static final String TAG = Sh1106.class.getSimpleName();
    private I2cDevice mI2cDevice;

    // Screen configuration constants.
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 64;

    /**
     * I2C address for this peripheral
     */
    public static final int I2C_ADDRESS = 0x3C;
    public static final int I2C_ADDRESS_ALT = 0x3D;

    private static final int PAGES = 8;
    private static final int VERTICAL_PIXEL_PER_PAGE = 8;

    // Protocol constants
    private static final int DATA_OFFSET = 1;
    private static final int COMMAND_DISPLAY_ON = 0xAF;
    private static final int COMMAND_DISPLAY_OFF = 0xAE;
    private static final int COMMAND_MEMORY_ADDRESSING_MODE = 0x20;
    private static final int COMMAND_HIGH_COLUMN = 0x10;
    private static final int COMMAND_PAGE = 0xB0;
    private static final int COMMAND_COMMON_OUTPUT_SCAN_DIRECTION = 0xC8;
    private static final int COMMAND_LOW_COLUMN = 0x02;
    private static final int COMMAND_DISPLAY_START_LINE = 0x40;
    private static final int COMMAND_SEGMENT_REMAP = 0xA1;
    private static final int COMMAND_NORMAL_DISPLAY = 0xA6;
    private static final int COMMAND_INVERTED_DISPLAY = 0xA7;
    private static final int COMMAND_DISPLAY_OFFSET = 0xD3;
    private static final int COMMAND_DISPLAY_OFFSET_VALUE = 0x00;
    private static final int COMMAND_DISPLAY_CLOCK_DIV = 0xD5;
    private static final int COMMAND_DISPLAY_CLOCK_DIV_VALUE = 0xF0;
    private static final int COMMAND_PRE_CHARGE = 0xD9;
    private static final int COMMAND_PRE_CHARGE_VALUE = 0x22;
    private static final int COMMAND_COMMON_PADS_HARDWARE_CONFIGURATION = 0xDA;
    private static final int COMMAND_COMMON_PADS_HARDWARE_CONFIGURATION_VALUE = 0x12;
    private static final int COMMAND_VCOM_DESELECT_LEVEL = 0xDB;
    private static final int COMMAND_VCOM_DESELECT_LEVEL_VALUE = 0x20;
    private static final int COMMAND_CHARGE_PUMP = 0x8D;
    private static final int COMMAND_CONTRAST_LEVEL = 0x81;

    private static final byte[] INIT_PAYLOAD = new byte[]{
            0, (byte) COMMAND_DISPLAY_OFF,
            0, (byte) COMMAND_MEMORY_ADDRESSING_MODE,
            0, (byte) COMMAND_HIGH_COLUMN,
            0, (byte) COMMAND_PAGE,
            0, (byte) COMMAND_COMMON_OUTPUT_SCAN_DIRECTION,
            0, (byte) COMMAND_LOW_COLUMN,
            0, (byte) COMMAND_SEGMENT_REMAP,
            0, (byte) COMMAND_INVERTED_DISPLAY,
            0, (byte) COMMAND_DISPLAY_START_LINE,
            0, (byte) COMMAND_DISPLAY_OFFSET,
            0, (byte) COMMAND_DISPLAY_OFFSET_VALUE,
            0, (byte) COMMAND_DISPLAY_CLOCK_DIV,
            0, (byte) COMMAND_DISPLAY_CLOCK_DIV_VALUE,
            0, (byte) COMMAND_PRE_CHARGE,
            0, (byte) COMMAND_PRE_CHARGE_VALUE,
            0, (byte) COMMAND_COMMON_PADS_HARDWARE_CONFIGURATION,
            0, (byte) COMMAND_COMMON_PADS_HARDWARE_CONFIGURATION_VALUE,
            0, (byte) COMMAND_VCOM_DESELECT_LEVEL,
            0, (byte) COMMAND_VCOM_DESELECT_LEVEL_VALUE,
            0, (byte) COMMAND_DISPLAY_ON,
            0, (byte) COMMAND_CHARGE_PUMP,

    };

    // Screen dimension.
    private int mWidth;
    private int mHeight;

    // Holds the i2c payloads.
    private byte[][] mBuffer;

    /**
     * Create a new Sh1106 driver connected to the named I2C bus
     *
     * @param i2cName I2C bus name the display is connected to
     * @throws IOException
     */
    public Sh1106(String i2cName) throws IOException {
        this(i2cName, I2C_ADDRESS);
    }

    /**
     * Create a new Sh1106 driver connected to the named I2C bus
     * with the given dimensions.
     *
     * @param i2cName I2C bus name the display is connected to
     * @param width   display width in pixels.
     * @param height  display height in pixels.
     * @throws IOException
     */
    public Sh1106(String i2cName, int width, int height) throws IOException {
        this(i2cName, I2C_ADDRESS, width, height);
    }

    /**
     * Create a new Sh1106 driver connected to the named I2C bus and address
     *
     * @param i2cName    I2C bus name the display is connected to
     * @param i2cAddress I2C address of the display
     * @throws IOException
     */
    public Sh1106(String i2cName, int i2cAddress) throws IOException {
        this(i2cName, i2cAddress, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Create a new Sh1106 driver connected to the named I2C bus and address
     * with the given dimensions.
     *
     * @param i2cName    I2C bus name the display is connected to
     * @param i2cAddress I2C address of the display
     * @param width      display width in pixels.
     * @param height     display height in pixels.
     * @throws IOException
     */
    public Sh1106(String i2cName, int i2cAddress, int width, int height) throws IOException {
        I2cDevice device = PeripheralManager.getInstance().openI2cDevice(i2cName, i2cAddress);
        try {
            init(device, width, height);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    /**
     * Create a new Sh1106 driver connected to the given device
     *
     * @param device I2C device of the display
     * @throws IOException
     */
    /*package*/ Sh1106(I2cDevice device) throws IOException {
        init(device, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Recommended start sequence for initializing the communications with the OLED display.
     * WARNING: If you change this code, power cycle your display before testing.
     *
     * @throws IOException
     */
    private void init(I2cDevice device, int width, int height) throws IOException {
        mI2cDevice = device;
        mWidth = width;
        mHeight = height;
        mBuffer = new byte[PAGES][(((mWidth * mHeight) / VERTICAL_PIXEL_PER_PAGE) / PAGES) + DATA_OFFSET];
        BitmapHelper.bmpToBytes(
                mBuffer,
                DATA_OFFSET,
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888),
                false);
        for (byte[] page : mBuffer) {
            page[0] = (byte) COMMAND_DISPLAY_START_LINE;
        }
        mI2cDevice.write(INIT_PAYLOAD, INIT_PAYLOAD.length);
    }

    @Override
    public void close() throws IOException {
        if (mI2cDevice != null) {
            try {
                mI2cDevice.close();
            } finally {
                mI2cDevice = null;
            }
        }
    }

    /**
     * Return the width of the display
     *
     * @return the width of the display
     */
    public int getLcdWidth() {
        return mWidth;
    }

    /**
     * Return the height of the display
     *
     * @return the height of the display
     */
    public int getLcdHeight() {
        return mHeight;
    }

    /**
     * Clears all pixel data in the display buffer. This will be rendered the next time
     * {@link #show()} is called.
     */
    public void clearPixels() {
        for (byte[] row : mBuffer) {
            Arrays.fill(row, DATA_OFFSET, row.length, (byte) 0);
        }
    }

    /**
     * Sets a specific pixel in the display buffer to on or off. This will be rendered the next time
     * {@link #show()} is called.
     *
     * @param x  The horizontal coordinate.
     * @param y  The vertical coordinate.
     * @param on Set to true to enable the pixel; false to disable the pixel.
     */
    public void setPixel(int x, int y, boolean on) throws IllegalArgumentException {
        if (x < 0 || y < 0 || x >= mWidth || y >= mHeight) {
            throw new IllegalArgumentException("Pixel out of bound:" + x + "," + y);
        }
        if (on) {
            mBuffer[(y / PAGES)][DATA_OFFSET + x] |= (1 << y % VERTICAL_PIXEL_PER_PAGE);
        } else {
            mBuffer[(y / PAGES)][DATA_OFFSET + x] &= ~(1 << y % VERTICAL_PIXEL_PER_PAGE);
        }
    }

    /**
     * Sets the contrast for the display.
     *
     * @param level The contrast level (0-255).
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public void setContrast(int level) throws IOException, IllegalArgumentException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        if (level < 0 || level > 255) {
            throw new IllegalArgumentException("Invalid contrast " + String.valueOf(level) +
                    ", level must be between 0 and 255");
        }
        mI2cDevice.writeRegByte(0, (byte) COMMAND_CONTRAST_LEVEL);
        mI2cDevice.writeRegByte(0, (byte) level);
    }

    /**
     * Turns the display on and off.
     *
     * @param on Set to true to enable the display; set to false to disable the display.
     * @throws IOException
     * @throws IllegalStateException
     */
    public void setDisplayOn(boolean on) throws IOException, IllegalStateException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        if (on) {
            mI2cDevice.writeRegByte(0, (byte) COMMAND_DISPLAY_ON);
        } else {
            mI2cDevice.writeRegByte(0, (byte) COMMAND_DISPLAY_OFF);
        }
    }

    /**
     * Invert the display color without rewriting the contents of the display data RAM..
     *
     * @param invert Set to true to invert the display color; set to false to set the display back to normal.
     * @throws IOException
     * @throws IllegalStateException
     */
    public void setInvertDisplay(boolean invert) throws IOException, IllegalStateException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        if (invert) {
            mI2cDevice.writeRegByte(0, (byte) COMMAND_INVERTED_DISPLAY);
        } else {
            mI2cDevice.writeRegByte(0, (byte) COMMAND_NORMAL_DISPLAY);
        }
    }

    /**
     * Renders the current pixel data to the screen.
     *
     * @throws IOException
     * @throws IllegalStateException
     */
    public void show() throws IOException, IllegalStateException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        for (int page = 0; page < PAGES; page++) {
            mI2cDevice.writeRegByte(0, (byte) (COMMAND_PAGE + page));
            mI2cDevice.writeRegByte(0, (byte) COMMAND_HIGH_COLUMN);
            mI2cDevice.writeRegByte(0, (byte) COMMAND_LOW_COLUMN);
            mI2cDevice.write(mBuffer[page], mBuffer[page].length);
        }
    }
}
