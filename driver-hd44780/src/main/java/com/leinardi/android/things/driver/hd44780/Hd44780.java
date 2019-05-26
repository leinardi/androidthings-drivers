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

package com.leinardi.android.things.driver.hd44780;

import android.util.Log;

import androidx.annotation.IntDef;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

/**
 * Driver for controlling the hd44780 LCD via the PCF8574's I2C.
 */
@SuppressWarnings("WeakerAccess")
public class Hd44780 implements AutoCloseable {
    private static final String TAG = Hd44780.class.getSimpleName();
    private static final int NANOS_PER_MILLI = (int) TimeUnit.MILLISECONDS.toNanos(1);
    private static final int COL_INDEX = 0;
    private static final int ROW_INDEX = 1;
    private static final int[][] GEOMETRIES = new int[][]{{8, 1}, {16, 2}, {20, 2}, {20, 4}};
    // commands
    private static final int HD44780_CLEAR_DISPLAY = 0x01;
    private static final int HD44780_RETURN_HOME = 0x02;
    private static final int HD44780_ENTRY_MODE_SET = 0x04;
    private static final int HD44780_DISPLAY_CTRL = 0x08;
    private static final int HD44780_CURSOR_SHIFT = 0x10;
    private static final int HD44780_FUNCTION_SET = 0x20;
    private static final int HD44780_CGRAM_ADDRESS = 0x40;
    private static final int HD44780_DDRAM_ADDRESS = 0x80;
    // flags for function set
    private static final int HD44780_DL_8BITS = 0x10;
    private static final int HD44780_DL_4BITS = 0x00;
    private static final int HD44780_N_2LINES = 0x08;
    private static final int HD44780_N_1LINE = 0x00;
    private static final int HD44780_5X10DOTS = 0x04;
    private static final int HD44780_5X8DOTS = 0x00;
    // flags for display on/off control
    private static final int HD44780_D_DISPLAY_ON = 0x04;
    private static final int HD44780_D_DISPLAY_OFF = 0x00;
    private static final int HD44780_C_CURSOR_ON = 0x02;
    private static final int HD44780_C_CURSOR_OFF = 0x00;
    private static final int HD44780_B_BLINK_ON = 0x01;
    private static final int HD44780_B_BLINK_OFF = 0x00;
    // flags for display entry mode
    private static final int HD44780_ENTRY_RIGHT = 0x00;
    private static final int HD44780_ENTRY_LEFT = 0x02;
    private static final int HD44780_ENTRY_SHIFT_INCREMENT = 0x01;
    private static final int HD44780_ENTRY_SHIFT_DECREMENT = 0x00;
    // flags for display/cursor shift
    private static final int HD44780_DISPLAY_MOVE = 0x08;
    private static final int HD44780_CURSOR_MOVE = 0x00;
    private static final int HD44780_MOVE_RIGHT = 0x04;
    private static final int HD44780_MOVE_LEFT = 0x00;
    private static final int LOW = 0x0;
    // These are Bit-Masks for the special signals and background light
    private static final int PCF_RS = 0x01;
    private static final int PCF_RW = 0x02;
    private static final int PCF_EN = 0x04;
    private static final int PCF_BACKLIGHT = 0x08;
    // Definitions on how the PCF8574 is connected to the LCD
    // These are Bit-Masks for the special signals and Background
    private static final int RSMODE_CMD = 0;
    private static final int RSMODE_DATA = 1;
    private static final int MAX_CGRAM_LOCATIONS = 8;
    private static final int[] ROW_OFFSETS = {0x00, 0x40, 0x14, 0x54};
    private final int[] mLcdGeometry;
    private boolean mBacklight;
    private byte mDisplayControl; // cursor, display, blink flags
    private byte mDisplayMode; // left2right, autoscroll
    private I2cDevice mI2cDevice;
    private int mCurrentCol;
    private int mCurrentRow;

    /**
     * Create a new Hd44780 driver connected to the named I2C bus and address
     * with the given geometry.
     *
     * @param i2cName    I2C bus name the display is connected to
     * @param i2cAddress I2C address of the display
     * @param geometry   geometry of the LCD. See {@link Geometry}.
     */
    public Hd44780(String i2cName, int i2cAddress, @Geometry int geometry) throws IOException {
        this(i2cName, i2cAddress, geometry, false);
    }

    /**
     * Create a new Hd44780 driver connected to the named I2C bus and address
     * with the given geometry.
     *
     * @param i2cName     I2C bus name the display is connected to
     * @param i2cAddress  I2C address of the display
     * @param geometry    geometry of the LCD. See {@link Geometry}.
     * @param use5x10Dots True to use a 10 pixel high font, false for the 8 pixel (default). It only works
     *                    for some 1 line displays.
     * @throws IOException
     */
    public Hd44780(String i2cName, int i2cAddress, @Geometry int geometry, boolean use5x10Dots) throws IOException {
        mLcdGeometry = GEOMETRIES[geometry];
        I2cDevice device = PeripheralManager.getInstance().openI2cDevice(i2cName, i2cAddress);
        try {
            init(device, use5x10Dots);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    /**
     * Recommended start sequence for initializing the communications with the LCD.
     * WARNING: If you change this code, power cycle your display before testing.
     *
     * @throws IOException
     */
    private void init(I2cDevice device, boolean use5x10Dots) throws IOException {
        mI2cDevice = device;

        byte displayFunction = 0; // lines and dots mode

        if (mLcdGeometry[ROW_INDEX] > 1) {
            displayFunction |= HD44780_N_2LINES;
        } else {
            displayFunction |= HD44780_N_1LINE;
        }

        if ((use5x10Dots) && (mLcdGeometry[ROW_INDEX] == 1)) {
            displayFunction |= HD44780_5X10DOTS;
        } else {
            displayFunction |= HD44780_5X8DOTS;
        }

        // initializing the display
        write2Wire((byte) 0x00, LOW, false);
        // SEE PAGE 45/46 FOR INITIALIZATION SPECIFICATION!
        // according to datasheet, we need at least 40ms after power rises above 2.7V
        // before sending commands.
        delayMicroseconds(50000);

        // Put the LCD into 4 bit mode according to the Hitachi HD44780 datasheet figure 26, pg 46
        commandHighNibble(HD44780_FUNCTION_SET | HD44780_DL_8BITS);
        delayMicroseconds(5000);
        commandHighNibble(HD44780_FUNCTION_SET | HD44780_DL_8BITS);
        delayMicroseconds(150);
        commandHighNibble(HD44780_FUNCTION_SET | HD44780_DL_8BITS);
        commandHighNibble(HD44780_FUNCTION_SET | HD44780_DL_4BITS);

        // finally, set # lines, font size, etc.
        command(HD44780_FUNCTION_SET | displayFunction);

        // turn the display on with no cursor or blinking default
        mDisplayControl = HD44780_D_DISPLAY_ON | HD44780_C_CURSOR_OFF | HD44780_B_BLINK_OFF;
        setDisplayOn(true);

        // clear it off
        clearDisplay();

        // Initialize to default text direction (for romance languages)
        mDisplayMode = HD44780_ENTRY_LEFT | HD44780_ENTRY_SHIFT_DECREMENT;
        // set the entry mode
        command(HD44780_ENTRY_MODE_SET | mDisplayMode);
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
     * Clears display and returns cursor to the home position (address 0).
     *
     * @throws IOException
     */
    public void clearDisplay() throws IOException {
        command(HD44780_CLEAR_DISPLAY);
        delayMicroseconds(2000); // this command takes a long time!

        // CLEAR_DISPLAY instruction also returns cursor to home,
        // so we need to update it locally.
        mCurrentCol = 0;
        mCurrentRow = 0;
    }

    /**
     * Clear the specified line and return the cursor to the beginning of the current row.
     *
     * @param row the row of the line to clear
     * @throws IOException
     */
    public void clearLine(int row) throws IOException {
        if (row < 0 || row > mLcdGeometry[ROW_INDEX]) {
            throw new IndexOutOfBoundsException("Row: " + row);
        }
        setCursor(0, row);
        for (int col = 0; col < mLcdGeometry[COL_INDEX]; col++) {
            write(' ');
        }
        setCursor(0, row);

    }

    private void handleNewLine() throws IOException {
        clearLine((mCurrentRow + 1) % mLcdGeometry[ROW_INDEX]);
    }

    private void handleCarriageReturn() throws IOException {
        setCursor(0, mCurrentRow);
    }

    /**
     * Returns cursor to home position.
     * Also returns display being shifted to the original position.DDRAM content remains unchanged.
     *
     * @throws IOException
     */
    public void cursorHome() throws IOException {
        command(HD44780_RETURN_HOME);
        delayMicroseconds(2000); // this command takes a long time!
    }

    /**
     * Set the cursor to a new position.
     *
     * @param col Desired column of the cursor
     * @param row Desired row of the cursor
     * @throws IOException
     */
    public void setCursor(int col, int row) throws IOException {
        if (col < 0 || col > mLcdGeometry[COL_INDEX]) {
            throw new IndexOutOfBoundsException("Col: " + col);
        }
        if (row >= mLcdGeometry[ROW_INDEX]) {
            row = (row + 1) % mLcdGeometry[ROW_INDEX];
        }

        command(HD44780_DDRAM_ADDRESS | (col + ROW_OFFSETS[row]));
        mCurrentCol = col;
        mCurrentRow = row;
    }

    /**
     * Turns the display on and off.
     *
     * @param on Set to true to enable the display; set to false to disable the display.
     * @throws IOException
     * @throws IllegalStateException
     */
    public void setDisplayOn(boolean on) throws IOException {
        if (on) {
            mDisplayControl |= HD44780_D_DISPLAY_ON;
            command(HD44780_DISPLAY_CTRL | mDisplayControl);
        } else {
            mDisplayControl &= ~HD44780_D_DISPLAY_ON;
            command(HD44780_DISPLAY_CTRL | mDisplayControl);
        }
    }

    /**
     * Turns the blinking cursor on and off.
     *
     * @param on Set to true to enable the blinking cursor; set to false to disable the blinking cursor.
     * @throws IOException
     * @throws IllegalStateException
     */
    public void setBlinkOn(boolean on) throws IOException {
        if (on) {
            mDisplayControl |= HD44780_B_BLINK_ON;
            command(HD44780_DISPLAY_CTRL | mDisplayControl);
        } else {
            mDisplayControl &= ~HD44780_B_BLINK_ON;
            command(HD44780_DISPLAY_CTRL | mDisplayControl);
        }
    }

    /**
     * Turns the underline cursor on and off.
     *
     * @param on Set to true to enable the underline cursor; set to false to disable the underline cursor.
     * @throws IOException
     * @throws IllegalStateException
     */
    public void setCursorOn(boolean on) throws IOException {
        if (on) {
            mDisplayControl |= HD44780_C_CURSOR_ON;
            command(HD44780_DISPLAY_CTRL | mDisplayControl);
        } else {
            mDisplayControl &= ~HD44780_C_CURSOR_ON;
            command(HD44780_DISPLAY_CTRL | mDisplayControl);
        }
    }

    // These commands scroll the display without changing the RAM
    public void scrollDisplayLeft() throws IOException {
        command(HD44780_CURSOR_SHIFT | HD44780_DISPLAY_MOVE | HD44780_MOVE_LEFT);
    }

    public void scrollDisplayRight() throws IOException {
        command(HD44780_CURSOR_SHIFT | HD44780_DISPLAY_MOVE | HD44780_MOVE_RIGHT);
    }

    /**
     * Set for text that flows Left to Right
     *
     * @throws IOException
     */
    public void setLeftToRight() throws IOException {
        mDisplayMode |= HD44780_ENTRY_LEFT;
        command(HD44780_ENTRY_MODE_SET | mDisplayMode);
    }

    /**
     * Set for text that flows Right to Left
     *
     * @throws IOException
     */
    public void setRightToLeft() throws IOException {
        mDisplayMode &= ~HD44780_ENTRY_LEFT;
        command(HD44780_ENTRY_MODE_SET | mDisplayMode);
    }

    public void setShiftIncrement(boolean increment) throws IOException {
        if (increment) {
            mDisplayMode |= HD44780_ENTRY_SHIFT_INCREMENT;
            command(HD44780_ENTRY_MODE_SET | mDisplayMode);
        } else {
            mDisplayMode &= ~HD44780_ENTRY_SHIFT_INCREMENT;
            command(HD44780_ENTRY_MODE_SET | mDisplayMode);
        }

    }

    /**
     * Switches the backlight on and off.
     *
     * @param enable Set to true to enable the backlight; set to false to disable the backlight.
     * @throws IOException
     */
    public void setBacklight(boolean enable) throws IOException {
        // The current brightness is stored in the private backlight variable to have it available for further data
        // transfers.
        mBacklight = enable;
        // send no data but set the background-pin right;
        write2Wire((byte) 0x00, RSMODE_DATA, false);
    }

    /**
     * Allows us to fill the first 8 CGRAM locations with custom characters
     *
     * @param charmap  an int[8] array containing the custom character
     * @param location the location where to store the custom character [0-8)
     * @throws IOException
     */
    public void createCustomChar(int[] charmap, int location) throws IOException {
        if (location < 0 || location > MAX_CGRAM_LOCATIONS) {
            throw new IndexOutOfBoundsException("Location must be between 0 and 7. Location: " + location);
        }
        location &= 0x7; // we only have 8 locations 0-7
        command(HD44780_CGRAM_ADDRESS | (location << 3));
        for (int i = 0; i < 8; i++) {
            write(charmap[i]);
        }
    }

    /**
     * Print the custom character stored in the CGRAM location to the current cursor position
     * <p>
     * See also {@link #createCustomChar(int[], int)}
     *
     * @param location the CGRAM location containing the custom character
     * @throws IOException
     */
    public void writeCustomChar(int location) throws IOException {
        if (location < 0 || location > MAX_CGRAM_LOCATIONS) {
            throw new IndexOutOfBoundsException("Location must be between 0 and 7. Location: " + location);
        }
        writeChar((char) location);
    }

    private void writeChar(char c) throws IOException {
        write(c);
        mCurrentCol++;
        if (mCurrentCol >= mLcdGeometry[COL_INDEX]) {
            clearLine((mCurrentRow + 1) % mLcdGeometry[ROW_INDEX]);
        }
    }

    private void write(int value) throws IOException {
        send(value, RSMODE_DATA);
    }

    /**
     * Sets the text to the begin of the specified line
     *
     * @param text The text to set
     * @param line Line number where to insert the text
     * @throws IOException
     */
    public void setText(String text, int line) throws IOException {
        if (line < 0 || line > mLcdGeometry[ROW_INDEX]) {
            throw new IndexOutOfBoundsException("Line:" + line);
        }
        clearLine(line);
        setText(text);
    }

    /**
     * Sets the text to the current cursor position
     *
     * @param text The text to set
     * @throws IOException
     */
    public void setText(String text) throws IOException {
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\r':
                    handleCarriageReturn();
                    break;
                case '\n':
                    handleNewLine();
                    break;
                default:
                    writeChar(c);
            }
        }
    }

    private void commandHighNibble(int value) throws IOException {
        sendNibble((byte) (value >> 4 & 0x0F), RSMODE_CMD);
    }

    private void command(int value) throws IOException {
        send(value, RSMODE_CMD);
    }

    /**
     * Writes either command or data
     *
     * @param value the value to send
     * @param mode  {@link #RSMODE_CMD} for commands, {@link #RSMODE_DATA} for data
     * @throws IOException
     */
    private void send(int value, int mode) throws IOException {
        // separate the 4 value-nibbles
        byte valueLo = (byte) (value & 0x0F);
        byte valueHi = (byte) (value >> 4 & 0x0F);

        sendNibble(valueHi, mode);
        sendNibble(valueLo, mode);
    }

    /**
     * Writes a nibble / halfByte with handshake
     *
     * @throws IOException
     */
    private void sendNibble(byte halfByte, int mode) throws IOException {
        write2Wire(halfByte, mode, true);
        delayMicroseconds(1);    // enable pulse must be >450ns
        write2Wire(halfByte, mode, false);
        delayMicroseconds(37); // commands need > 37us to settle
    }

    /**
     * Changes the PCF8674 pins to the given value
     *
     * @throws IOException
     * @throws IllegalStateException
     */
    private void write2Wire(byte halfByte, int mode, boolean enable) throws IOException, IllegalStateException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        // map the given values to the hardware of the I2C schema
        byte i2cData = (byte) (halfByte << 4);
        if (mode > 0) {
            i2cData |= PCF_RS;
        }
        // PCF_RW is never used.
        if (enable) {
            i2cData |= PCF_EN;
        }
        if (mBacklight) {
            i2cData |= PCF_BACKLIGHT;
        }
        mI2cDevice.write(new byte[]{i2cData}, 1);
    }

    private void delayMicroseconds(long micros) {
        long nanos = micros * 1000;
        long millis = nanos / NANOS_PER_MILLI;
        nanos %= NANOS_PER_MILLI;
        try {
            Thread.sleep(millis, (int) nanos);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException", e);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Geometry.LCD_8X1, Geometry.LCD_16X2, Geometry.LCD_20X2, Geometry.LCD_20X4})
    public @interface Geometry {
        int LCD_8X1 = 0;
        int LCD_16X2 = 1;
        int LCD_20X2 = 2;
        int LCD_20X4 = 3;
    }

    /**
     * I2C addresses for this peripheral
     */
    public static class I2cAddress {
        public static final int PCF8574AT = 0x3F;
        public static final int PCF8574AT_A0 = 0x3E;
        public static final int PCF8574AT_A1 = 0x3D;
        public static final int PCF8574AT_A0_A1 = 0x3C;
        public static final int PCF8574AT_A2 = 0x3B;
        public static final int PCF8574AT_A0_A2 = 0x3A;
        public static final int PCF8574AT_A1_A2 = 0x39;
        public static final int PCF8574AT_A0_A1_A2 = 0x38;
        public static final int PCF8574T = 0x27;
        public static final int PCF8574T_A0 = 0x26;
        public static final int PCF8574T_A1 = 0x25;
        public static final int PCF8574T_A0_A1 = 0x24;
        public static final int PCF8574T_A2 = 0x23;
        public static final int PCF8574T_A0_A2 = 0x22;
        public static final int PCF8574T_A1_A2 = 0x21;
        public static final int PCF8574T_A0_A1_A2 = 0x20;
    }
}
