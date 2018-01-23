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

package com.leinardi.android.things.driver.tsl256x;

import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.leinardi.android.things.driver.tsl256x.Tsl256x.Gain.GAIN_16X;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.Gain.GAIN_1X;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.IntegrationTime.INTEGRATIONTIME_101MS;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.IntegrationTime.INTEGRATIONTIME_13_7MS;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.IntegrationTime.INTEGRATIONTIME_402MS;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.Mode.MODE_ACTIVE;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.Mode.MODE_STANDBY;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.PackageType.CS;
import static com.leinardi.android.things.driver.tsl256x.Tsl256x.PackageType.T_FN_CL;
import static java.lang.Math.pow;

/**
 * Driver for the TSL256x light-to-digital converter.
 */
@SuppressWarnings("WeakerAccess")
public class Tsl256x implements Closeable {
    private static final String TAG = Tsl256x.class.getSimpleName();

    private static final int COMMAND_BIT = 0b1000_0000;        // Must be 1
    private static final int CLEAR_BIT = 0b0100_0000;          // Clears any pending interrupt (write 1 to clear)
    private static final int WORD_BIT = 0b0010_0000;           // 1 = read/write word (rather than byte)
    private static final int BLOCK_BIT = 0b0001_0000;          // 1 = using block read/write

    private static final int REGISTER_CONTROL = 0x00;          // Control/power register
    private static final int REGISTER_TIMING = 0x01;           // Set integration time register
    private static final int REGISTER_THRESHHOLDL_LOW = 0x02;  // Interrupt low threshold low-byte
    private static final int REGISTER_THRESHHOLDL_HIGH = 0x03; // Interrupt low threshold high-byte
    private static final int REGISTER_THRESHHOLDH_LOW = 0x04;  // Interrupt high threshold low-byte
    private static final int REGISTER_THRESHHOLDH_HIGH = 0x05; // Interrupt high threshold high-byte
    private static final int REGISTER_INTERRUPT = 0x06;        // Interrupt settings
    private static final int REGISTER_CRC = 0x08;              // Factory use only
    private static final int REGISTER_ID = 0x0A;               // TSL2561 identification setting
    private static final int REGISTER_CHAN0_LOW = 0x0C;        // Light data channel 0, low byte
    private static final int REGISTER_CHAN0_HIGH = 0x0D;       // Light data channel 0, high byte
    private static final int REGISTER_CHAN1_LOW = 0x0E;        // Light data channel 1, low byte
    private static final int REGISTER_CHAN1_HIGH = 0x0F;       // Light data channel 1, high byte

    private static final int ID_PART_NUMBER = 0b1111_0000;
    private static final int ID_REVISION_NUMBER = 0b0000_1111;
    private static final int TSL2560_ID = 0b0000_0000;
    private static final int TSL2561_ID = 0b0001_0000;
    private static final int TSL2562_ID = 0b0010_0000;
    private static final int TSL2563_ID = 0b0011_0000;
    private static final int TSL2560T_FN_CL_ID = 0b0100_0000;
    private static final int TSL2561T_FN_CL_ID = 0b0101_0000;

    private static final int LUX_LUXSCALE = 14;      // Scale by 2^14
    private static final int LUX_RATIOSCALE = 9;       // Scale ratio by 2^9
    private static final int LUX_CHSCALE = 10;      // Scale channel values by 2^10
    private static final int LUX_CHSCALE_TINT0 = 0x7517;  // 322/11 * 2^LUX_CHSCALE
    private static final int LUX_CHSCALE_TINT1 = 0x0FE7;  // 322/81 * 2^LUX_CHSCALE

    // CS package values
    private static final int LUX_K1CS = 0x0043;  // 0.130 * 2^RATIO_SCALE
    private static final int LUX_B1CS = 0x0204;  // 0.0315 * 2^LUX_SCALE
    private static final int LUX_M1CS = 0x01ad;  // 0.0262 * 2^LUX_SCALE
    private static final int LUX_K2CS = 0x0085;  // 0.260 * 2^RATIO_SCALE
    private static final int LUX_B2CS = 0x0228;  // 0.0337 * 2^LUX_SCALE
    private static final int LUX_M2CS = 0x02c1;  // 0.0430 * 2^LUX_SCALE
    private static final int LUX_K3CS = 0x00c8;  // 0.390 * 2^RATIO_SCALE
    private static final int LUX_B3CS = 0x0253;  // 0.0363 * 2^LUX_SCALE
    private static final int LUX_M3CS = 0x0363;  // 0.0529 * 2^LUX_SCALE
    private static final int LUX_K4CS = 0x010a;  // 0.520 * 2^RATIO_SCALE
    private static final int LUX_B4CS = 0x0282;  // 0.0392 * 2^LUX_SCALE
    private static final int LUX_M4CS = 0x03df;  // 0.0605 * 2^LUX_SCALE
    private static final int LUX_K5CS = 0x014d;  // 0.65 * 2^RATIO_SCALE
    private static final int LUX_B5CS = 0x0177;  // 0.0229 * 2^LUX_SCALE
    private static final int LUX_M5CS = 0x01dd;  // 0.0291 * 2^LUX_SCALE
    private static final int LUX_K6CS = 0x019a;  // 0.80 * 2^RATIO_SCALE
    private static final int LUX_B6CS = 0x0101;  // 0.0157 * 2^LUX_SCALE
    private static final int LUX_M6CS = 0x0127;  // 0.0180 * 2^LUX_SCALE
    private static final int LUX_K7CS = 0x029a;  // 1.3 * 2^RATIO_SCALE
    private static final int LUX_B7CS = 0x0037;  // 0.00338 * 2^LUX_SCALE
    private static final int LUX_M7CS = 0x002b;  // 0.00260 * 2^LUX_SCALE
    private static final int LUX_K8CS = 0x029a;  // 1.3 * 2^RATIO_SCALE
    private static final int LUX_B8CS = 0x0000;  // 0.000 * 2^LUX_SCALE
    private static final int LUX_M8CS = 0x0000;  // 0.000 * 2^LUX_SCALE

    // T, FN and CL package values
    private static final int LUX_K1T = 0x0040;  // 0.125 * 2^RATIO_SCALE
    private static final int LUX_B1T = 0x01f2;  // 0.0304 * 2^LUX_SCALE
    private static final int LUX_M1T = 0x01be;  // 0.0272 * 2^LUX_SCALE
    private static final int LUX_K2T = 0x0080;  // 0.250 * 2^RATIO_SCALE
    private static final int LUX_B2T = 0x0214;  // 0.0325 * 2^LUX_SCALE
    private static final int LUX_M2T = 0x02d1;  // 0.0440 * 2^LUX_SCALE
    private static final int LUX_K3T = 0x00c0;  // 0.375 * 2^RATIO_SCALE
    private static final int LUX_B3T = 0x023f;  // 0.0351 * 2^LUX_SCALE
    private static final int LUX_M3T = 0x037b;  // 0.0544 * 2^LUX_SCALE
    private static final int LUX_K4T = 0x0100;  // 0.50 * 2^RATIO_SCALE
    private static final int LUX_B4T = 0x0270;  // 0.0381 * 2^LUX_SCALE
    private static final int LUX_M4T = 0x03fe;  // 0.0624 * 2^LUX_SCALE
    private static final int LUX_K5T = 0x0138;  // 0.61 * 2^RATIO_SCALE
    private static final int LUX_B5T = 0x016f;  // 0.0224 * 2^LUX_SCALE
    private static final int LUX_M5T = 0x01fc;  // 0.0310 * 2^LUX_SCALE
    private static final int LUX_K6T = 0x019a;  // 0.80 * 2^RATIO_SCALE
    private static final int LUX_B6T = 0x00d2;  // 0.0128 * 2^LUX_SCALE
    private static final int LUX_M6T = 0x00fb;  // 0.0153 * 2^LUX_SCALE
    private static final int LUX_K7T = 0x029a;  // 1.3 * 2^RATIO_SCALE
    private static final int LUX_B7T = 0x0018;  // 0.00146 * 2^LUX_SCALE
    private static final int LUX_M7T = 0x0012;  // 0.00112 * 2^LUX_SCALE
    private static final int LUX_K8T = 0x029a;  // 1.3 * 2^RATIO_SCALE
    private static final int LUX_B8T = 0x0000;  // 0.000 * 2^LUX_SCALE
    private static final int LUX_M8T = 0x0000;  // 0.000 * 2^LUX_SCALE

    // Auto-gain thresholds
    private static final int AGC_THI_13MS = 4850;    // Max value at Ti 13ms = 5047
    private static final int AGC_TLO_13MS = 100;     // Min value at Ti 13ms = 100
    private static final int AGC_THI_101MS = 36000;  // Max value at Ti 101ms = 37177
    private static final int AGC_TLO_101MS = 200;    // Min value at Ti 101ms = 200
    private static final int AGC_THI_402MS = 63000;  // Max value at Ti 402ms = 65535
    private static final int AGC_TLO_402MS = 500;    // Min value at Ti 402ms = 500

    private byte mChipId;
    private boolean mAutoGain;
    @IntegrationTime
    private int mIntegrationTime;
    @Gain
    private int mGain;
    @PackageType
    private int mPackageType;
    private I2cDevice mDevice;

    /**
     * Create a new TSL2561 driver connected to the given I2C bus.
     *
     * @param i2cName    I2C bus name the display is connected to
     * @param i2cAddress I2C address of the display
     * @throws IOException
     */
    public Tsl256x(String i2cName, int i2cAddress) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice device = pioService.openI2cDevice(i2cName, i2cAddress);
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
        if (mDevice != null) {
            throw new IllegalStateException("device already connected");
        }
        mDevice = device;

        @PackageType int packageType = CS;

        mChipId = readRegByte(REGISTER_ID);
        int partNumber = (mChipId & ID_PART_NUMBER) & 0xFF;
        switch (partNumber) {
            case TSL2560_ID:
                Log.d(TAG, "Found TSL2560");
                break;
            case TSL2561_ID:
                Log.d(TAG, "Found TSL2561");
                break;
            case TSL2562_ID:
                Log.d(TAG, "Found TSL2562");
                break;
            case TSL2563_ID:
                Log.d(TAG, "Found TSL2563");
                break;
            case TSL2560T_FN_CL_ID:
                Log.d(TAG, "Found TSL2560T/FN/CL");
                packageType = T_FN_CL;
                break;
            case TSL2561T_FN_CL_ID:
                Log.d(TAG, "Found TSL2561T/FN/CL");
                packageType = T_FN_CL;
                break;
            default:
                throw new IllegalStateException("Could not find a TSL256x, check wiring!");
        }

        setPackageType(packageType);
        setIntegrationTime(INTEGRATIONTIME_13_7MS);
        setGain(GAIN_1X);

        /* Note: by default, the device is in power down mode on bootup */
        setMode(MODE_STANDBY);
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    public byte getChipId() {
        return mChipId;
    }

    @PackageType
    public int getPackageType() {
        return mPackageType;
    }

    public void setPackageType(@PackageType int packageType) {
        mPackageType = packageType;
    }

    public boolean isAutoGainEnabled() {
        return mAutoGain;
    }

    public void setAutoGain(boolean autoGain) {
        mAutoGain = autoGain;
    }

    public void setIntegrationTime(@IntegrationTime int integrationTime) throws IOException {
        setMode(MODE_ACTIVE);
        writeRegByte(COMMAND_BIT | REGISTER_TIMING, (byte) ((integrationTime | mGain) & 0xFF));
        setMode(MODE_STANDBY);
        mIntegrationTime = integrationTime;
    }

    public void setGain(@Gain int gain) throws IOException {
        setMode(MODE_ACTIVE);
        writeRegByte(COMMAND_BIT | REGISTER_TIMING, (byte) ((mIntegrationTime | gain) & 0xFF));
        setMode(MODE_STANDBY);
        mGain = gain;
    }

    public int[] getLuminosity() throws IOException {
        setMode(MODE_ACTIVE);
        // Wait x ms for ADC to complete
        switch (mIntegrationTime) {
            case INTEGRATIONTIME_13_7MS:
                SystemClock.sleep(20);
                break;
            case INTEGRATIONTIME_101MS:
                SystemClock.sleep(110);
                break;
            case INTEGRATIONTIME_402MS:
                SystemClock.sleep(410);
                break;
        }
        int[] luminosities;
        if (isAutoGainEnabled()) {
            boolean check = false;
            boolean validRangeFound = false;

            do {
                int ch0;
                int tresholdHigh = 0;
                int tresholdLow = 0;
                @IntegrationTime
                int integrationTime = mIntegrationTime;

                // Get the hi/low threshold for the current integration time
                switch (integrationTime) {
                    case INTEGRATIONTIME_13_7MS:
                        tresholdHigh = AGC_THI_13MS;
                        tresholdLow = AGC_TLO_13MS;
                        break;
                    case INTEGRATIONTIME_101MS:
                        tresholdHigh = AGC_THI_101MS;
                        tresholdLow = AGC_TLO_101MS;
                        break;
                    case INTEGRATIONTIME_402MS:
                        tresholdHigh = AGC_THI_402MS;
                        tresholdLow = AGC_TLO_402MS;
                        break;
                }
                luminosities = getLuminosity();
                ch0 = luminosities[0];

                // Run an auto-gain check if we haven't already done so...
                if (!check) {
                    if ((ch0 < tresholdLow) && (mGain == GAIN_1X)) {
                        // Increase the gain and try again
                        setGain(GAIN_16X);
                        // Drop the previous conversion results
                        luminosities = getLuminosity();
                        // Set a flag to indicate we've adjusted the gain
                        check = true;
                    } else if ((ch0 > tresholdHigh) && (mGain == GAIN_16X)) {
                        // Drop gain to 1x and try again
                        setGain(GAIN_1X);
                        // Drop the previous conversion results
                        luminosities = getLuminosity();
                        // Set a flag to indicate we've adjusted the gain
                        check = true;
                    } else {
                        // Nothing to look at here, keep moving ....
                        // Reading is either valid, or we're already at the chips limits
                        validRangeFound = true;
                    }
                } else {
                    // If we've already adjusted the gain once, just return the new results.
                    // This avoids endless loops where a value is at one extreme pre-gain,
                    // and the the other extreme post-gain
                    validRangeFound = true;
                }
            } while (!validRangeFound);
        } else {
            luminosities = readLuminosityData();
        }
        setMode(MODE_STANDBY);
        return luminosities;
    }

    private int[] readLuminosityData() throws IOException {
        int[] luminosities = new int[3];
        luminosities[0] = readRegWord(COMMAND_BIT | WORD_BIT | REGISTER_CHAN0_LOW) & 0xFFFF;
        luminosities[1] = readRegWord(COMMAND_BIT | WORD_BIT | REGISTER_CHAN1_LOW) & 0xFFFF;
        luminosities[2] = luminosities[0] - luminosities[1];
        return luminosities;
    }

    public float getLuxF() throws IOException {
        int[] luminosities = getLuminosity();
        // Convert from unsigned integer to floating point
        float ch0 = luminosities[0];
        float ch1 = luminosities[1];

        // We will need the ratio for subsequent calculations
        float ratio = ch1 / ch0;

        float time = 0;
        switch (mIntegrationTime) {
            case INTEGRATIONTIME_13_7MS:
                time = 13.7f;
                break;
            case INTEGRATIONTIME_101MS:
                time = 101f;
                break;
            case INTEGRATIONTIME_402MS:
                time = 402f;
                break;
        }

        // Normalize for integration time
        ch0 *= (402.0 / time);
        ch1 *= (402.0 / time);

        // Normalize for gain
        if (mGain == GAIN_1X) {
            ch0 *= 16;
            ch1 *= 16;
        }

        // Determine lux per datasheet equations
        float lux = 0;
        if (mPackageType == CS) {
            if (ratio < 0.52) {
                lux = 0.0315f * ch0 - 0.0593f * ch0 * (float) pow(ratio, 1.4);
            } else if (ratio < 0.65) {
                lux = 0.0229f * ch0 - 0.0291f * ch1;
            } else if (ratio < 0.80) {
                lux = 0.0157f * ch0 - 0.0180f * ch1;
            } else if (ratio < 1.30) {
                lux = 0.00338f * ch0 - 0.00260f * ch1;
            }
        } else {
            if (ratio < 0.5) {
                lux = 0.0304f * ch0 - 0.062f * ch0 * (float) pow(ratio, 1.4);
            } else if (ratio < 0.61) {
                lux = 0.0224f * ch0 - 0.031f * ch1;
            } else if (ratio < 0.80) {
                lux = 0.0128f * ch0 - 0.0153f * ch1;
            } else if (ratio < 1.30) {
                lux = 0.00146f * ch0 - 0.00112f * ch1;
            }
        }
        return lux;
    }

    public long getLux() throws IOException {
        int[] luminosities = getLuminosity();
        int ch0 = luminosities[0];
        int ch1 = luminosities[1];
        long chScale = 0;
        long channel1;
        long channel0;

        switch (mIntegrationTime) {
            case INTEGRATIONTIME_13_7MS:
                chScale = LUX_CHSCALE_TINT0;
                break;
            case INTEGRATIONTIME_101MS:
                chScale = LUX_CHSCALE_TINT1;
                break;
            case INTEGRATIONTIME_402MS:
                chScale = (1 << LUX_CHSCALE);
                break;
        }

        // Scale for gain (1x or 16x)
        if (mGain == GAIN_1X) {
            chScale = chScale << 4;
        }

        // scale the channel values
        channel0 = (ch0 * chScale) >> LUX_CHSCALE;
        channel1 = (ch1 * chScale) >> LUX_CHSCALE;

        // find the ratio of the channel values (Channel1/Channel0)
        long ratio1 = 0;
        if (channel0 != 0) {
            ratio1 = (channel1 << (LUX_RATIOSCALE + 1)) / channel0;
        }

        // round the ratio value
        long ratio = (ratio1 + 1) >> 1;

        int b = 0;
        int m = 0;

        if (mPackageType == CS) {
            if ((ratio >= 0) && (ratio <= LUX_K1CS)) {
                b = LUX_B1CS;
                m = LUX_M1CS;
            } else if (ratio <= LUX_K2CS) {
                b = LUX_B2CS;
                m = LUX_M2CS;
            } else if (ratio <= LUX_K3CS) {
                b = LUX_B3CS;
                m = LUX_M3CS;
            } else if (ratio <= LUX_K4CS) {
                b = LUX_B4CS;
                m = LUX_M4CS;
            } else if (ratio <= LUX_K5CS) {
                b = LUX_B5CS;
                m = LUX_M5CS;
            } else if (ratio <= LUX_K6CS) {
                b = LUX_B6CS;
                m = LUX_M6CS;
            } else if (ratio <= LUX_K7CS) {
                b = LUX_B7CS;
                m = LUX_M7CS;
            } else if (ratio > LUX_K8CS) {
                b = LUX_B8CS;
                m = LUX_M8CS;
            }
        } else {
            if ((ratio >= 0) && (ratio <= LUX_K1T)) {
                b = LUX_B1T;
                m = LUX_M1T;
            } else if (ratio <= LUX_K2T) {
                b = LUX_B2T;
                m = LUX_M2T;
            } else if (ratio <= LUX_K3T) {
                b = LUX_B3T;
                m = LUX_M3T;
            } else if (ratio <= LUX_K4T) {
                b = LUX_B4T;
                m = LUX_M4T;
            } else if (ratio <= LUX_K5T) {
                b = LUX_B5T;
                m = LUX_M5T;
            } else if (ratio <= LUX_K6T) {
                b = LUX_B6T;
                m = LUX_M6T;
            } else if (ratio <= LUX_K7T) {
                b = LUX_B7T;
                m = LUX_M7T;
            } else if (ratio > LUX_K8T) {
                b = LUX_B8T;
                m = LUX_M8T;
            }
        }

        long temp;
        temp = ((channel0 * b) - (channel1 * m));

        // do not allow negative lux value
        if (temp < 0) {
            temp = 0;
        }

        // round lsb (2^(LUX_SCALE-1))
        temp += (1 << (LUX_LUXSCALE - 1));

        // strip off fractional portion
        return temp >> LUX_LUXSCALE;
    }

    /**
     * Set current power mode.
     *
     * @param mode
     * @throws IOException
     * @throws IllegalStateException
     */
    public void setMode(@Mode int mode) throws IOException, IllegalStateException {
        writeRegByte(COMMAND_BIT | REGISTER_CONTROL, (byte) mode);
    }

    /**
     * Get current power mode.
     *
     * @return
     * @throws IOException
     * @throws IllegalStateException
     */
    //    @SuppressWarnings("ResourceType")
    //    public @Mode
    //    int getMode() throws IOException, IllegalStateException {
    //
    //    }
    private byte readRegByte(int reg) throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        return mDevice.readRegByte(reg);
    }

    private void writeRegByte(int reg, byte data) throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        mDevice.writeRegByte(reg, data);
    }

    private short readRegWord(int reg) throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        return mDevice.readRegWord(reg);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INTEGRATIONTIME_13_7MS,
            INTEGRATIONTIME_101MS,
            INTEGRATIONTIME_402MS
    })
    public @interface IntegrationTime {
        int INTEGRATIONTIME_13_7MS = 0b0000_0000;  // 13.7ms
        int INTEGRATIONTIME_101MS = 0b0000_0001; // 101ms
        int INTEGRATIONTIME_402MS = 0b0000_0010; // 402ms
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GAIN_1X, GAIN_16X
    })
    public @interface Gain {
        int GAIN_1X = 0b0000_0000;     // No gain
        int GAIN_16X = 0b0001_0000;    // 16x gain
    }

    /**
     * Power mode.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_STANDBY, MODE_ACTIVE})
    public @interface Mode {
        int MODE_STANDBY = 0b00; // i2c on, output off, low power
        int MODE_ACTIVE = 0b11;  // i2c on, output on
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CS, T_FN_CL})
    public @interface PackageType {
        int CS = 0;
        int T_FN_CL = 3;
    }
}
