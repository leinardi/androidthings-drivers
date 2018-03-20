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

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

@SuppressWarnings("WeakerAccess")
public abstract class Epd implements Closeable {
    private static final String TAG = Epd.class.getSimpleName();
    private static final String EPD_RESET_PIN_RPI = "BCM17";
    private static final String EPD_DATA_COMMAND_PIN_RPI = "BCM25";
    //    private static final String EPD_CS_PIN_RPI = "BCM8";
    private static final String EPD_BUSY_PIN_RPI = "BCM24";
    private static final String EPD_SPI_DEVICE_RPI = "SPI0.0";

    // Device SPI Configuration constants
    private static final int SPI_BPW = 8; // Bits per word
    private static final int SPI_FREQUENCY = 2000000;
    // Clock idle low, data is clocked in on rising edge, output data (change) on falling edge
    private static final int SPI_MODE = SpiDevice.MODE0;
    private static final int MAX_WRITE_BUFFER_LENGTH = 1024;
    private SpiDevice mSpiDevice;
    /**
     * Reset signal input. The Reset is active Low.
     */
    private Gpio mResetPin;
    /**
     * Data/Command control pin connecting to the MCU. When the pin is pulled High, the data will be interpreted as
     * data. When the pin is pulled Low, the data will be interpreted as command.
     */
    private Gpio mDataCommandPin;
    /**
     * Busy state output pin. When Busy the operation of chip should not be interrupted and any commands
     * should not be issued to the module.
     */
    private Gpio mBusyPin;

    public Epd() throws IOException {
        this(EPD_SPI_DEVICE_RPI, EPD_RESET_PIN_RPI, EPD_DATA_COMMAND_PIN_RPI, EPD_BUSY_PIN_RPI);
    }

    public Epd(String spiBusPort, String resetPin, String dataCommandPin, String busyPin) throws IOException {
        PeripheralManager pioService = PeripheralManager.getInstance();
        mSpiDevice = pioService.openSpiDevice(spiBusPort);
        mResetPin = pioService.openGpio(resetPin);
        mDataCommandPin = pioService.openGpio(dataCommandPin);
        mBusyPin = pioService.openGpio(busyPin);
        try {
            configure();
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    protected void configure() throws IOException {
        // Note: You may need to set bit justification for your board.
        // mSpiDevice.setBitJustification(SPI_BITJUST);
        mSpiDevice.setFrequency(SPI_FREQUENCY);
        mSpiDevice.setMode(SPI_MODE);
        mSpiDevice.setBitsPerWord(SPI_BPW);
        mResetPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mDataCommandPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mBusyPin.setDirection(Gpio.DIRECTION_IN);
    }

    protected void sendCommand(int command) throws IOException {
        if (mSpiDevice == null) {
            throw new IllegalStateException("SPI device not open");
        }
        mDataCommandPin.setValue(false);
        byte[] buffer = new byte[]{(byte) (command & 0xFF)};
        mSpiDevice.write(buffer, buffer.length);
    }

    protected void sendData(byte data) throws IOException {
        byte[] buffer = new byte[]{data};
        sendData(buffer);
    }

    protected void sendData(byte[] data) throws IOException {
        if (mSpiDevice == null) {
            throw new IllegalStateException("SPI device not open");
        }
        mDataCommandPin.setValue(true);
        if (data.length > MAX_WRITE_BUFFER_LENGTH) {
            for (int i = 0; i < data.length - MAX_WRITE_BUFFER_LENGTH + 1; i += MAX_WRITE_BUFFER_LENGTH) {
                mSpiDevice.write(Arrays.copyOfRange(data, i, i + MAX_WRITE_BUFFER_LENGTH), MAX_WRITE_BUFFER_LENGTH);
            }

            if (data.length % MAX_WRITE_BUFFER_LENGTH != 0) {
                mSpiDevice.write(
                        Arrays.copyOfRange(data, data.length - data.length % MAX_WRITE_BUFFER_LENGTH, data.length),
                        data.length % MAX_WRITE_BUFFER_LENGTH);
            }
        } else {
            mSpiDevice.write(data, data.length);
        }
    }

    public void reset() throws IOException {
        mResetPin.setValue(false);
        delay(50);
        mResetPin.setValue(true);
        delay(50);
    }

    /**
     * Get the width of the display
     */
    public abstract int getDisplayWidth();

    /**
     * Get the height of the display
     */
    public abstract int getDisplayHeight();

    /**
     * Clears all pixel data in the display buffer. This will be rendered the next time
     * {@link #show()} is called.
     */
    public abstract void clearPixels();

    /**
     * Sets a specific pixel in the display buffer to on or off. This will be rendered the next time
     * {@link #show()} is called.
     *
     * @param x  The horizontal coordinate.
     * @param y  The vertical coordinate.
     * @param on Set to true to enable the pixel; false to disable the pixel.
     */
    public abstract void setPixel(int x, int y, boolean on) throws IllegalArgumentException;

    /**
     * Invert the display color without rewriting the contents of the display data RAM..
     *
     * @param invert Set to true to invert the display color; set to false to set the display back to normal.
     * @throws IOException
     * @throws IllegalStateException
     */
    public abstract void setInvertDisplay(boolean invert) throws IOException, IllegalStateException;

    /**
     * Renders the current pixel data to the screen.
     *
     * @throws IOException
     * @throws IllegalStateException
     */
    public abstract void show() throws IOException;

    protected abstract void wakeUp() throws IOException;

    protected abstract void sleep() throws IOException;

    protected abstract boolean isBusy() throws IOException;

    protected boolean getBusyPinValue() throws IOException {
        return mBusyPin.getValue();
    }

    protected void waitUntilIdle() throws IOException {
        while (isBusy()) {
            delay(100);
        }
    }

    protected void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Releases the SPI interface and related resources.
     */
    @Override
    public void close() throws IOException {
        if (mSpiDevice != null) {
            try {
                mSpiDevice.close();
            } finally {
                mSpiDevice = null;
            }
        }
        if (mResetPin != null) {
            try {
                mResetPin.close();
            } finally {
                mResetPin = null;
            }
        }
        if (mDataCommandPin != null) {
            try {
                mDataCommandPin.close();
            } finally {
                mDataCommandPin = null;
            }
        }
        if (mBusyPin != null) {
            try {
                mBusyPin.close();
            } finally {
                mBusyPin = null;
            }
        }
    }
}
