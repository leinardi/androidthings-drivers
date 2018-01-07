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

package com.leinardi.android.things.driver.ds3231;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Driver for controlling the DS3231 real-time clock (RTC).
 */
public class Ds3231 implements Closeable {
    public static final int TIMEKEEPING_INVALID = -1;
    /**
     * I2C address for this peripheral
     */
    public static final int I2C_ADDRESS = 0x68;
    public static final float TEMPERATURE_RESOLUTION = 0.25f;
    private static final String TAG = Ds3231.class.getSimpleName();
    private static final int RTC_SECONDS_REG = 0x00;
    private static final int RTC_MINUTES_REG = 0x01;
    private static final int RTC_HOURS_REG = 0x02;
    private static final int RTC_DAY_REG = 0x03;
    private static final int RTC_DATE_REG = 0x04;
    private static final int RTC_MONTH_REG = 0x05;
    private static final int RTC_YEAR_REG = 0x06;
    private static final int ALM1_SECONDS_REG = 0x07;
    private static final int ALM1_MINUTES_REG = 0x08;
    private static final int ALM1_HOURS_REG = 0x09;
    private static final int ALM1_DAYDATE_REG = 0x0A;
    private static final int ALM2_MINUTES_REG = 0x0B;
    private static final int ALM2_HOURS_REG = 0x0C;
    private static final int ALM2_DAYDATE_REG = 0x0D;
    private static final int RTC_CONTROL_REG = 0x0E;
    private static final int RTC_STATUS_REG = 0x0F;
    private static final int RTC_AGING_REG = 0x10;
    private static final int RTC_TEMP_MSB_REG = 0x11;
    private static final int RTC_TEMP_LSB_REG = 0x12;
    private static final int RTC_MONTH_REG_CENTURY = 0b1000_0000;
    /**
     * When high, the 12-hour mode is selected
     */
    private static final int INDICATOR_12_HOURS = 0b0100_0000;
    /**
     * In the 12-hour mode, is the AM/PM bit with logic-high being PM.
     */
    private static final int INDICATOR_PM = 0b0010_0000;
    /**
     * When set to logic 0, the oscillator is started. When set to logic 1, the oscillator is stopped when the DS3231
     * switches to V BAT. This bit is clear (logic 0) when power is first applied. When the DS3231 is powered by V
     * CC , the oscillator is always on regardless of the status of the EOSC bit. When EOSC is disabled, all register
     * data is static.
     */
    private static final int RTC_CONTROL_REG_ENABLE_OSCILLATOR = 0b1000_0000;
    /**
     * When set to logic 1 with INTCN = 0 and V CC < V PF, this bit enables the square wave. When BBSQW is logic 0,
     * the INT/SQW pin goes high impedance when V CC < V PF. This bit is disabled (logic 0) when power is first
     * applied.
     */
    private static final int RTC_CONTROL_REG_BATTERY_BACKED_SQUARE_WAVE_ENABLE = 0b0100_0000;
    /**
     * Setting this bit to 1 forces the temperature sensor to convert the tempera- ture into digital code and execute
     * the TCXO algorithm to update the capacitance array to the oscillator. This can only happen when a conversion
     * is not already in prog- ress. The user should check the status bit BSY before forcing the controller to start
     * a new TCXO execution. A user-initiated temperature conversion does not affect the internal 64-second update
     * cycle. A user-initiated temperature conversion does not affect the BSY bit for approximately 2ms. The CONV bit
     * remains at a 1 from the time it is written until the conversion is finished, at which time both CONV and BSY
     * go to 0. The CONV bit should be used when monitoring the status of a user-initiated conversion.
     */
    private static final int RTC_CONTROL_REG_CONVERT_TEMPERATURE = 0b0010_0000;
    /**
     * See {@link #RTC_CONTROL_REG_RATE_SELECT1}.
     */
    private static final int RTC_CONTROL_REG_RATE_SELECT2 = 0b0001_0000;
    /**
     * {@link #RTC_CONTROL_REG_RATE_SELECT1} and {@link #RTC_CONTROL_REG_RATE_SELECT2} control the frequency of the
     * square-wave
     * output when the square wave has been enabled. The following table shows the square-wave frequencies that can
     * be selected with the RS bits. These bits are both set to logic 1 (8.192kHz) when power is first applied.
     */
    private static final int RTC_CONTROL_REG_RATE_SELECT1 = 0b0000_1000;
    /**
     * This bit controls the INT/SQW signal. When the INTCN bit is set to logic 0, a square wave is output on the
     * INT/SQW pin. When the INTCN bit is set to logic 1, then a match between the time- keeping registers and either
     * of the alarm registers acti- vates the INT/SQW output (if the alarm is also enabled). The corresponding alarm
     * flag is always set regardless of the state of the INTCN bit. The INTCN bit is set to logic 1 when power is
     * first applied.
     */
    private static final int RTC_CONTROL_REG_INTERRUPT_CONTROL = 0b0000_0100;
    /**
     * When set to logic 1, this bit permits the alarm 2 flag (A2F) bit in the status register to assert INT/SQW
     * (when INTCN = 1). When the A2IE bit is set to logic 0 or INTCN is set to logic 0, the A2F bit does not
     * initiate an interrupt signal. The A2IE bit is disabled (logic 0) when power is first applied.
     */
    private static final int RTC_CONTROL_REG_ALARM2_INTERRUPT_ENABLE = 0b0000_0010;
    /**
     * When set to logic 1, this bit permits the alarm 1 flag (A1F) bit in the status register to assert INT/SQW
     * (when INTCN = 1). When the A1IE bit is set to logic 0 or INTCN is set to logic 0, the A1F bit does not
     * initiate the INT/SQW signal. The A1IE bit is disabled (logic 0) when power is first applied.
     */
    private static final int RTC_CONTROL_REG_ALARM1_INTERRUPT_ENABLE = 0b0000_0001;
    /**
     * A logic 1 in this bit indicates that the oscillator either is stopped or was stopped for some period and may
     * be used to judge the validity of the timekeeping data. This bit is set to logic 1 any time that the oscillator
     * stops. The following are examples of conditions that can cause the OSF bit to be set:
     * 1) The first time power is applied.
     * 2) The voltages present on both V CC and V BAT are insufficient to support oscillation.
     * 3) The EOSC bit is turned off in battery-backed mode.
     * 4) External influences on the crystal (i.e., noise, leakage, etc.).
     * This bit remains at logic 1 until written to logic 0.
     */
    private static final int RTC_STATUS_REG_OSCILLATOR_STOP_FLAG = 0b1000_0000;
    /**
     * This bit con trols the status of the 32kHz pin. When set to logic 1, the 32kHz pin is enabled and outputs a
     * 32.768kHz square- wave signal. When set to logic 0, the 32kHz pin goes to a high-impedance state. The initial
     * power-up state of this bit is logic 1, and a 32.768kHz square-wave signal appears at the 32kHz pin after a
     * power source is applied to the DS3231 (if the oscillator is running).
     */
    private static final int RTC_STATUS_REG_ENABLE_32KHZ_OUTPUT = 0b0000_1000;
    /**
     * This bit indicates the device is busy executing TCXO functions. It goes to logic 1 when the conversion signal
     * to the temperature sensor is asserted and then is cleared when the device is in the 1-minute idle state.
     */
    private static final int RTC_STATUS_REG_BUSY = 0b0000_0100;
    /**
     * A logic 1 in the alarm 2 flag bit indicates that the time matched the alarm 2 registers. If the A2IE bit is
     * logic 1 and the INTCN bit is set to logic 1, the INT/SQW pin is also asserted. A2F is cleared when written to
     * logic 0. This bit can only be written to logic 0. Attempting to write to logic 1 leaves the value unchanged.
     */
    private static final int RTC_STATUS_REG_ALARM2_FLAG = 0b0000_0010;
    /**
     * A logic 1 in the alarm 1 flag bit indicates that the time matched the alarm 1 registers. If the A1IE bit is
     * logic 1 and the INTCN bit is set to logic 1, the INT/SQW pin is also asserted. A1F is cleared when written to
     * logic 0. This bit can only be written to logic 0. Attempting to write to logic 1 leaves the value unchanged.
     */
    private static final int RTC_STATUS_REG_ALARM1_FLAG = 0b0000_0001;
    private I2cDevice mI2cDevice;

    /**
     * Create a new Ds3231 driver connected to the named I2C bus
     *
     * @param i2cName I2C bus name the RTC is connected to
     * @throws IOException
     */
    public Ds3231(String i2cName) throws IOException {
        this(i2cName, I2C_ADDRESS);
    }

    /**
     * Create a new Ds3231 driver connected to the named I2C bus and address
     * with the given dimensions.
     *
     * @param i2cName    I2C bus name the RTC is connected to
     * @param i2cAddress I2C address of the RTC
     * @throws IOException
     */
    public Ds3231(String i2cName, int i2cAddress) throws IOException {
        I2cDevice device = new PeripheralManagerService().openI2cDevice(i2cName, i2cAddress);
        try {
            connect(device);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    private void connect(I2cDevice device) throws IOException {
        mI2cDevice = device;

        // Try to read a register
        isTimekeepingDataValid();

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

    @Nullable
    public Date getTime() throws IOException {
        if (isTimekeepingDataValid()) {
            byte[] buffer = new byte[7];
            readRegBuffer(RTC_SECONDS_REG, buffer, buffer.length);

            int second = packetBcdToDec(buffer[0]);
            int minute = packetBcdToDec(buffer[1]);
            int hour;
            if (((buffer[2] & 0xFF) & INDICATOR_12_HOURS) == INDICATOR_12_HOURS) {
                hour = packetBcdToDec((byte) ((buffer[2] & 0xFF) & ~(INDICATOR_12_HOURS | INDICATOR_PM)));
                if (((buffer[2] & 0xFF) & INDICATOR_PM) == INDICATOR_PM) {
                    hour += 12;
                }
            } else {
                hour = packetBcdToDec((byte) ((buffer[2] & 0xFF) & ~(INDICATOR_12_HOURS)));
            }
            int day = packetBcdToDec(buffer[4]);
            int month = packetBcdToDec((byte) (buffer[5] & 0b0001_1111));
            int century = ((buffer[5] & 0xFF) & RTC_MONTH_REG_CENTURY) == RTC_MONTH_REG_CENTURY ? 2000 : 1900;
            int year = century + packetBcdToDec(buffer[6]);

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
            calendar.set(year, month, day, hour, minute, second);

            return calendar.getTime();
        } else {
            return null;
        }
    }

    public void setTime(Date date) throws IOException {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        if (year < 1900 || year > 2099) {
            throw new IllegalArgumentException("Year must be between 1900 and 2099 included. Year: " + year);
        }
        byte[] buffer = new byte[7];

        buffer[0] = decToPacketBcd(calendar.get(Calendar.SECOND));
        buffer[1] = decToPacketBcd(calendar.get(Calendar.MINUTE));
        buffer[2] = decToPacketBcd(calendar.get(Calendar.HOUR_OF_DAY));
        buffer[2] &= ~(INDICATOR_12_HOURS);
        buffer[3] = decToPacketBcd(calendar.get(Calendar.DAY_OF_WEEK));
        buffer[4] = decToPacketBcd(calendar.get(Calendar.DAY_OF_MONTH));
        buffer[5] = decToPacketBcd(calendar.get(Calendar.MONTH));
        buffer[5] += year > 1999 ? RTC_MONTH_REG_CENTURY : 0;
        buffer[6] = decToPacketBcd(year % 100);

        setOscillator(true);
        setTimekeepingDataValid(true);
        writeRegBuffer(RTC_SECONDS_REG, buffer, buffer.length);
    }

    public void setTime(long timeInMillis) throws IOException {
        setTime(new Date(timeInMillis));
    }

    public long getTimeInMillis() throws IOException {
        Date date = getTime();
        if (date != null) {
            return date.getTime();
        } else {
            return TIMEKEEPING_INVALID;
        }
    }

    public boolean isTimekeepingDataValid() throws IOException {
        return ((readRegByte(RTC_STATUS_REG) & 0xFF) & RTC_STATUS_REG_OSCILLATOR_STOP_FLAG)
                != RTC_STATUS_REG_OSCILLATOR_STOP_FLAG;
    }

    private void setTimekeepingDataValid(boolean valid) throws IOException {
        byte reg = readRegByte(RTC_STATUS_REG);
        reg &= ~(RTC_STATUS_REG_OSCILLATOR_STOP_FLAG);
        if (!valid) {
            reg |= RTC_STATUS_REG_OSCILLATOR_STOP_FLAG;
        }
        writeRegByte(RTC_STATUS_REG, reg);
    }

    public boolean isOscillatorEnabled() throws IOException {
        return ((readRegByte(RTC_CONTROL_REG) & 0xFF) & RTC_CONTROL_REG_ENABLE_OSCILLATOR)
                == RTC_CONTROL_REG_ENABLE_OSCILLATOR;
    }

    public void setOscillator(boolean enabled) throws IOException {
        byte reg = readRegByte(RTC_CONTROL_REG);
        reg &= ~(RTC_CONTROL_REG_ENABLE_OSCILLATOR);
        if (enabled) {
            reg |= RTC_CONTROL_REG_ENABLE_OSCILLATOR;
        }
        writeRegByte(RTC_CONTROL_REG, reg);
    }

    public boolean is32khz() {
        return false;
    }

    public void set32khz(boolean enabled) {

    }

    private void forceTemperatureRefresh() throws IOException {
        byte reg = readRegByte(RTC_CONTROL_REG);
        reg |= RTC_CONTROL_REG_CONVERT_TEMPERATURE;
        writeRegByte(RTC_CONTROL_REG, reg);
    }

    private boolean isTemperatureRefreshing() throws IOException {
        int controlReg = readRegByte(RTC_CONTROL_REG) & 0xFF;
        int statusReg = readRegByte(RTC_STATUS_REG) & 0xFF;
        return (controlReg & RTC_CONTROL_REG_CONVERT_TEMPERATURE) != 0 || (statusReg & RTC_STATUS_REG_BUSY) != 0;
    }

    /**
     * Get the temperature of the sensor in degrees Celsius.
     * <p>
     * The intent of the temperature sensor is to keep track of the die temperature and compensate the crystal
     * oscillator if necessary. It is not intended as an environmental sensor.
     *
     * @return the temperature in degrees Celsius.
     * @throws IOException
     */
    public float readTemperature() throws IOException {
        boolean alreadyBusy = ((readRegByte(RTC_STATUS_REG) & 0xFF) & RTC_STATUS_REG_BUSY) == RTC_STATUS_REG_BUSY;
        if (!alreadyBusy) {
            forceTemperatureRefresh();
        }

        while (isTemperatureRefreshing()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep interrupted", e);
                Thread.currentThread().interrupt();
                break;
            }
        }

        byte[] buffer = new byte[2];
        readRegBuffer(RTC_TEMP_MSB_REG, buffer, buffer.length);
        return ((int) buffer[0]) + TEMPERATURE_RESOLUTION * ((buffer[1] >> 6) & 0xF);
    }

    /**
     * Read a byte from a given register.
     *
     * @param reg The register to read from (0x00-0xFF).
     * @return The value read from the device.
     * @throws IOException
     */
    private byte readRegByte(int reg) throws IOException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C device not open");
        }
        return mI2cDevice.readRegByte(reg);
    }

    /**
     * Read multiple bytes from a given register.
     *
     * @param reg    The register to read from (0x00-0xFF).
     * @param buffer Buffer to read data into.
     * @param length Number of bytes to read, may not be larger than the buffer size.
     * @throws IOException
     */
    private void readRegBuffer(int reg, byte[] buffer, int length) throws IOException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C device not open");
        }
        mI2cDevice.readRegBuffer(reg, buffer, length);
    }

    /**
     * Write a byte to a given register.
     *
     * @param reg The register to write to (0x00-0xFF).
     * @throws IOException
     */
    private void writeRegByte(int reg, byte data) throws IOException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C device not open");
        }
        mI2cDevice.writeRegByte(reg, data);

    }

    /**
     * Write a byte array to a given register.
     *
     * @param reg    The register to write to (0x00-0xFF).
     * @param buffer Data to write.
     * @param length Number of bytes to write, may not be larger than the buffer size.
     * @throws IOException
     */
    private void writeRegBuffer(int reg, byte[] buffer, int length) throws IOException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C device not open");
        }
        mI2cDevice.writeRegBuffer(reg, buffer, length);

    }

    private int packetBcdToDec(byte bcd) {
        return ((bcd >>> 4) * 10) + (bcd & 0xF);
    }

    private byte decToPacketBcd(int dec) {
        if (dec < 0 || dec > 99) {
            throw new IllegalArgumentException("dec must be between 0 and 99 included. dec: " + dec);
        }
        return (byte) (((dec / 10) << 4) + (dec % 10));
    }
}
