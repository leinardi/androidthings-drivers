/*
 * Copyright 2017 Roberto Leinardi.
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

package com.leinardi.androidthings.driver.lsm9ds1;

import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.annotation.IntDef;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_119HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_14_9HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_238HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_476HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_59_5HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_952HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_POWER_DOWN;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerDecimation.ACCEL_DEC_0_SAMPLES;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerDecimation.ACCEL_DEC_2_SAMPLES;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerDecimation.ACCEL_DEC_4_SAMPLES;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerDecimation.ACCEL_DEC_8_SAMPLES;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerRange.ACCEL_RANGE_16G;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerRange.ACCEL_RANGE_2G;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerRange.ACCEL_RANGE_4G;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerRange.ACCEL_RANGE_8G;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.GyroscopeScale.GYRO_SCALE_2000DPS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.GyroscopeScale.GYRO_SCALE_245DPS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.GyroscopeScale.GYRO_SCALE_500DPS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagOutputDataRate.ODR_M_0_625HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagOutputDataRate.ODR_M_10HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagOutputDataRate.ODR_M_1_25HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagOutputDataRate.ODR_M_20HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagOutputDataRate.ODR_M_2_5HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagOutputDataRate.ODR_M_40HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagOutputDataRate.ODR_M_6HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagOutputDataRate.ODR_M_80HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerGain.MAG_GAIN_12GAUSS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerGain.MAG_GAIN_16GAUSS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerGain.MAG_GAIN_4GAUSS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerGain.MAG_GAIN_8GAUSS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerSystemOperatingMode.MAG_CONTINUOUS_CONVERSION;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerSystemOperatingMode.MAG_POWER_DOWN;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerSystemOperatingMode.MAG_SINGLE_CONVERSION;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerXYOperatingMode.MAG_XY_OM_HIGH_PERFORMANCE;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerXYOperatingMode.MAG_XY_OM_LOW_POWER;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerXYOperatingMode.MAG_XY_OM_MEDIUM_PERFORMANCE;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerXYOperatingMode.MAG_XY_OM_ULTRA_HIGH_PERFORMANCE;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerZOperatingMode.MAG_Z_OM_HIGH_PERFORMANCE;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerZOperatingMode.MAG_Z_OM_LOW_POWER;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerZOperatingMode.MAG_Z_OM_MEDIUM_PERFORMANCE;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerZOperatingMode.MAG_Z_OM_ULTRA_HIGH_PERFORMANCE;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.SensorType.SENSOR_MAG;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.SensorType.SENSOR_XG;

/**
 * Driver for the LSM9DS1 3D accelerometer, 3D gyroscope, 3D magnetometer and temperature sensor.
 */
public class Lsm9ds1 implements Closeable {
    private static final String TAG = Lsm9ds1.class.getSimpleName();
    // Accelerometer/Gyroscope registers
    private static final int REGISTER_WHO_AM_I_XG = 0x0F;
    private static final int REGISTER_CTRL_REG1_G = 0x10;
    private static final int REGISTER_CTRL_REG2_G = 0x11;
    private static final int REGISTER_CTRL_REG3_G = 0x12;
    private static final int REGISTER_ORIENT_CFG_G = 0x13;
    private static final int REGISTER_TEMP_OUT_L = 0x15;
    private static final int REGISTER_TEMP_OUT_H = 0x16;
    private static final int REGISTER_STATUS_REG = 0x17;
    private static final int REGISTER_OUT_X_L_G = 0x18;
    private static final int REGISTER_OUT_X_H_G = 0x19;
    private static final int REGISTER_OUT_Y_L_G = 0x1A;
    private static final int REGISTER_OUT_Y_H_G = 0x1B;
    private static final int REGISTER_OUT_Z_L_G = 0x1C;
    private static final int REGISTER_OUT_Z_H_G = 0x1D;
    private static final int REGISTER_CTRL_REG4 = 0x1E;
    private static final int REGISTER_CTRL_REG5_XL = 0x1F;
    private static final int REGISTER_CTRL_REG6_XL = 0x20;
    private static final int REGISTER_CTRL_REG7_XL = 0x21;
    private static final int REGISTER_CTRL_REG8 = 0x22;
    private static final int REGISTER_CTRL_REG9 = 0x23;
    private static final int REGISTER_CTRL_REG10 = 0x24;
    private static final int REGISTER_OUT_X_L_XL = 0x28;
    private static final int REGISTER_OUT_X_H_XL = 0x29;
    private static final int REGISTER_OUT_Y_L_XL = 0x2A;
    private static final int REGISTER_OUT_Y_H_XL = 0x2B;
    private static final int REGISTER_OUT_Z_H_XL = 0x2D;
    private static final int REGISTER_OUT_Z_L_XL = 0x2C;
    private static final int REGISTER_FIFO_CTRL = 0x2E;
    private static final int REGISTER_FIFO_SRC = 0x2F;
    // Magnetometer registers
    private static final int REGISTER_WHO_AM_I_M = 0x0F;
    private static final int REGISTER_CTRL_REG1_M = 0x20;
    private static final int REGISTER_CTRL_REG2_M = 0x21;
    private static final int REGISTER_CTRL_REG3_M = 0x22;
    private static final int REGISTER_CTRL_REG4_M = 0x23;
    private static final int REGISTER_CTRL_REG5_M = 0x24;
    private static final int REGISTER_STATUS_REG_M = 0x27;
    private static final int REGISTER_OUT_X_L_M = 0x28;
    private static final int REGISTER_OUT_X_H_M = 0x29;
    private static final int REGISTER_OUT_Y_L_M = 0x2A;
    private static final int REGISTER_OUT_Y_H_M = 0x2B;
    private static final int REGISTER_OUT_Z_L_M = 0x2C;
    private static final int REGISTER_OUT_Z_H_M = 0x2D;
    private static final int REGISTER_CFG_M = 0x30;
    private static final int REGISTER_INT_SRC_M = 0x31;
    // Accelerometer data rate
    private static final int ACCELDATARATE_POWERDOWN = 0;
    private static final int ACCELDATARATE_3_125HZ = 1 << 4;
    private static final int ACCELDATARATE_6_25HZ = 2 << 4;
    private static final int ACCELDATARATE_12_5HZ = 3 << 4;
    private static final int ACCELDATARATE_25HZ = 4 << 4;
    private static final int ACCELDATARATE_50HZ = 5 << 4;
    private static final int ACCELDATARATE_100HZ = 6 << 4;
    private static final int ACCELDATARATE_200HZ = 7 << 4;
    private static final int ACCELDATARATE_400HZ = 8 << 4;
    private static final int ACCELDATARATE_800HZ = 9 << 4;
    private static final int ACCELDATARATE_1600HZ = 10 << 4;
    // Magnetometer data rate
    private static final int MAGDATARATE_3_125HZ = 0;
    private static final int MAGDATARATE_6_25HZ = 1 << 2;
    private static final int MAGDATARATE_12_5HZ = 2 << 2;
    private static final int MAGDATARATE_25HZ = 3 << 2;
    private static final int MAGDATARATE_50HZ = 4 << 2;
    private static final int MAGDATARATE_100HZ = 5 << 2;
    // STATUS_REG
    private static final byte STATUS_REG_IG_XL = 0b01000000;
    private static final byte STATUS_REG_IG_G = 0b00100000;
    private static final byte STATUS_REG_INACT = 0b00010000;
    private static final byte STATUS_REG_BOOT_STATUS = 0b00001000;
    private static final byte STATUS_REG_TDA = 0b00000100;
    private static final byte STATUS_REG_GDA = 0b00000010;
    private static final byte STATUS_REG_XLDA = 0b00000001;
    // STATUS_REG_M
    private static final byte STATUS_REG_M_ZYXOR = (byte) 0b10000000;
    private static final byte STATUS_REG_M_ZOR = 0b01000000;
    private static final byte STATUS_REG_M_YOR = 0b00100000;
    private static final byte STATUS_REG_M_XOR = 0b00010000;
    private static final byte STATUS_REG_M_ZYXDA = 0b00001000;
    private static final byte STATUS_REG_M_ZDA = 0b00000100;
    private static final byte STATUS_REG_M_YDA = 0b00000010;
    private static final byte STATUS_REG_M_XDA = 0b00000001;

    public static final byte CTRL_REG5_XL_ZEN_XL = 0b00100000;
    public static final byte CTRL_REG5_XL_YEN_XL = 0b00010000;
    public static final byte CTRL_REG5_XL_XEN_XL = 0b00001000;
    public static final byte ORIENT_CFG_G_SIGN_X_G = 0b00100000;
    public static final byte ORIENT_CFG_G_SIGN_Y_G = 0b00010000;
    public static final byte ORIENT_CFG_G_SIGN_Z_G = 0b00001000;
    private static final byte CTRL_REG7_XL_HR = (byte) 0b10000000;
    // CTRL_REG8
    private static final byte CTRL_REG8_BOOT = (byte) 0b10000000;
    private static final byte CTRL_REG8_BDU = 0b01000000;
    private static final byte CTRL_REG8_H_LACTIVE = 0b00100000;
    private static final byte CTRL_REG8_PP_OD = 0b00010000;
    private static final byte CTRL_REG8_SIM = 0b00001000;
    private static final byte CTRL_REG8_IF_ADD_INC = 0b00000100;
    private static final byte CTRL_REG8_BLE = 0b00000010;
    private static final byte CTRL_REG8_SW_RESET = 0b00000001;

    private static final byte CTRL_REG9_SLEEP_G = 0b01000000;
    private static final byte CTRL_REG9_FIFO_EN = 0b00000010;
    private static final byte CTRL_REG1_M_TEMP_COMP = (byte) 0b10000000;
    private static final byte CTRL_REG2_M_REBOOT = 0b00001000;
    private static final byte CTRL_REG2_M_SOFT_RST = 0b00000100;

    private static final float TEMP_LSB_DEGREE_CELSIUS = 16.0F;
    private static final float TEMP_BIAS = 27.5F; // This is an empirical estimation
    private static final int I2C_ADDRESS_ACCEL_GYRO = 0x6B;
    private static final int I2C_ADDRESS_MAG = 0x1E;
    private static final byte XG_ID = 0b01101000;
    private static final byte MAG_ID = 0b00111101;

    // Linear Acceleration: mg per LSB
    private static final float ACCEL_MG_LSB_2G = 0.061F;
    private static final float ACCEL_MG_LSB_4G = 0.122F;
    private static final float ACCEL_MG_LSB_8G = 0.244F;
    private static final float ACCEL_MG_LSB_16G = 0.732F;
    // Magnetic Field Strength: gauss range
    private static final float MAG_MGAUSS_4GAUSS = 0.14F;
    private static final float MAG_MGAUSS_8GAUSS = 0.29F;
    private static final float MAG_MGAUSS_12GAUSS = 0.43F;
    private static final float MAG_MGAUSS_16GAUSS = 0.58F;
    // Angular Rate: dps per LSB
    private static final float GYRO_DPS_DIGIT_245DPS = 0.00875F;
    private static final float GYRO_DPS_DIGIT_500DPS = 0.01750F;
    private static final float GYRO_DPS_DIGIT_2000DPS = 0.07000F;
    private static final int FIFO_MAX_THRESHOLD = 31;
    private float mAccelMgLsb;
    private float mMagMgaussLsb;
    private float mGyroDpsDigit;
    private float mGravity = SensorManager.GRAVITY_EARTH;
    private I2cDevice mAccelGyroDevice;
    private I2cDevice mMagDevice;

    /**
     * Create a new LSM9DS1 sensor driver connected on the given bus.
     *
     * @param bus I2C bus the sensor is connected to.
     * @throws IOException
     */
    public Lsm9ds1(String bus) throws IOException {
        this(bus, I2C_ADDRESS_ACCEL_GYRO, I2C_ADDRESS_MAG);
    }

    /**
     * Create a new LSM9DS1 sensor driver connected on the given bus and address.
     *
     * @param bus              I2C bus the sensor is connected to.
     * @param accelGyroAddress I2C address of the accelerometer/gyroscope sensor.
     * @param magAddress       I2C address of the magnetometer sensor.
     * @throws IOException
     */
    public Lsm9ds1(String bus, int accelGyroAddress, int magAddress) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice accelGyroDevice = pioService.openI2cDevice(bus, accelGyroAddress);
        I2cDevice magDevice = pioService.openI2cDevice(bus, magAddress);
        try {
            connect(accelGyroDevice, magDevice);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    private void connect(I2cDevice accelGyroDevice, I2cDevice magDevice) throws IOException {
        mAccelGyroDevice = accelGyroDevice;
        mMagDevice = magDevice;

        resetAndReboot(SENSOR_XG, false);
        resetAndReboot(SENSOR_MAG, true);

        byte idXg = readRegByte(SENSOR_XG, REGISTER_WHO_AM_I_XG);
        byte idMag = readRegByte(SENSOR_MAG, REGISTER_WHO_AM_I_M);
        if (idXg != XG_ID || idMag != MAG_ID) {
            throw new IllegalStateException("Could not find LSM9DS1, check wiring!");
        }

        setFifoMemoryEnabled(false);

        // Accelerometer configuration
        setAccelerometerOdr(ODR_952HZ);
        setAccelerometerEnabledAxes(CTRL_REG5_XL_ZEN_XL | CTRL_REG5_XL_YEN_XL | CTRL_REG5_XL_XEN_XL);
        setAccelerometerDecimation(ACCEL_DEC_0_SAMPLES);
        setAccelerometerHighResolution(true);
        setAccelerometerRange(ACCEL_RANGE_2G);

        // Gyroscope configuration
        setGyroscopeOdr(ODR_952HZ);
        setGyroscopeScale(GYRO_SCALE_245DPS);

        // Magnetometer configuration
        setMagnetometerTemperatureCompensation(false);
        setMagnetometerXYOperatingMode(MAG_XY_OM_LOW_POWER);
        setMagnetometerZOperatingMode(MAG_Z_OM_LOW_POWER);
        setMagnetometerSystemOperatingMode(MAG_CONTINUOUS_CONVERSION);
        setMagnetometerGain(MAG_GAIN_4GAUSS);
    }

    private void resetAndReboot(@SensorType int type, boolean waitForReboot) throws IOException {
        switch (type) {
            case SENSOR_XG:
                writeRegByte(type, REGISTER_CTRL_REG8, (byte) (CTRL_REG8_BOOT | CTRL_REG8_IF_ADD_INC | CTRL_REG8_SW_RESET));
                break;
            case SENSOR_MAG:
                writeRegByte(type, REGISTER_CTRL_REG2_M, (byte) (CTRL_REG2_M_REBOOT | CTRL_REG2_M_SOFT_RST));
                break;
        }
        if (waitForReboot) {
            SystemClock.sleep(10);
        }
    }

    private byte getStatusRegister(@SensorType int type) throws IOException {
        int reg = type == SENSOR_XG ? REGISTER_STATUS_REG : REGISTER_STATUS_REG_M;
        return readRegByte(type, reg);
    }

    /**
     * Polls the temperature status register to check if new data is available.
     *
     * @return false if a new data is not yet available; true if a new data is available
     * @throws IOException
     */
    public boolean isTemperatureNewDataAvailable() throws IOException {
        return (getStatusRegister(SENSOR_XG) & STATUS_REG_TDA) == STATUS_REG_TDA;
    }

    /**
     * Polls the accelerometer status register to check if new data is available.
     *
     * @return false if a new set of data is not yet available; true if a new set of data is available
     * @throws IOException
     */
    public boolean isAccelerometerNewDataAvailable() throws IOException {
        return (getStatusRegister(SENSOR_XG) & STATUS_REG_XLDA) == STATUS_REG_XLDA;
    }

    /**
     * Polls the gyroscope status register to check if new data is available.
     *
     * @return false if a new set of data is not yet available; true if a new set of data is available
     * @throws IOException
     */
    public boolean isGyroscopeNewDataAvailable() throws IOException {
        return (getStatusRegister(SENSOR_XG) & STATUS_REG_GDA) == STATUS_REG_GDA;
    }

    /**
     * Polls the accelerometer status register to check if new data is available for all axes.
     *
     * @return false if a new set of data is not yet available; true if a new set of data is available
     * @throws IOException
     */
    public boolean isMagnetometerXYZAxesNewDataAvailable() throws IOException {
        return (getStatusRegister(SENSOR_MAG) & STATUS_REG_M_ZYXDA) == STATUS_REG_M_ZYXDA;
    }

    /**
     * Polls the accelerometer status register to check if new data is available for X-axis.
     *
     * @return false if a new data for the X-axis is not yet available; true if a new data for the X-axis is available
     * @throws IOException
     */
    public boolean isMagnetometerXAxisNewDataAvailable() throws IOException {
        return (getStatusRegister(SENSOR_MAG) & STATUS_REG_M_XDA) == STATUS_REG_M_XDA;
    }

    /**
     * Polls the accelerometer status register to check if new data is available for Y-axis.
     *
     * @return false if a new data for the Y-axis is not yet available; true if a new data for the Y-axis is available
     * @throws IOException
     */
    public boolean isMagnetometerYAxisNewDataAvailable() throws IOException {
        return (getStatusRegister(SENSOR_MAG) & STATUS_REG_M_YDA) == STATUS_REG_M_YDA;
    }

    /**
     * Polls the accelerometer status register to check if new data is available for Z-axis.
     *
     * @return false if a new data for the Z-axis is not yet available; true if a new data for the Z-axis is available
     * @throws IOException
     */
    public boolean isMagnetometerZAxisNewDataAvailable() throws IOException {
        return (getStatusRegister(SENSOR_MAG) & STATUS_REG_M_ZDA) == STATUS_REG_M_ZDA;
    }

    /**
     * Get the accelerometer range.
     *
     * @throws IOException
     */
    public int getAccelerometerRange() throws IOException {
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL);
        return (reg & 0b00011000) & 0xFF;
    }

    /**
     * Set the accelerometer range.
     * Must be one of the {@link AccelerometerRange} values.
     *
     * @throws IOException
     */
    public void setAccelerometerRange(@AccelerometerRange int range) throws IOException {
        // We need to preserve the other bytes in CTRL_REG6_XL. So, first read it:
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL);
        // Then mask out the accel range bits:
        reg &= ~(0b00011000);
        // Then mask in our new range bits:
        reg |= range;
        // And write the new register value back into CTRL_REG6_XL:
        writeRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL, reg);

        switch (range) {
            case ACCEL_RANGE_2G:
                mAccelMgLsb = ACCEL_MG_LSB_2G;
                break;
            case ACCEL_RANGE_4G:
                mAccelMgLsb = ACCEL_MG_LSB_4G;
                break;
            case ACCEL_RANGE_8G:
                mAccelMgLsb = ACCEL_MG_LSB_8G;
                break;
            case ACCEL_RANGE_16G:
                mAccelMgLsb = ACCEL_MG_LSB_16G;
                break;
        }
    }

    /**
     * Get the accelerometer decimation data on OUT REG and FIFO.
     *
     * @throws IOException
     */
    public int getAccelerometerDecimation() throws IOException {
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL);
        return (reg & 0b11000000) & 0xFF;
    }

    /**
     * Set the accelerometer decimation data on OUT REG and FIFO.
     * Must be one of the {@link AccelerometerDecimation} values.
     *
     * @throws IOException
     */
    public void setAccelerometerDecimation(@AccelerometerDecimation int decimation) throws IOException {
        // We need to preserve the other bytes in CTRL_REG6_XL. So, first read it:
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL);
        // Then mask out the accel decimation bits:
        reg &= ~(0b11000000);
        // Then mask in our new decimation bits:
        reg |= decimation;
        // And write the new register value back into CTRL_REG6_XL:
        writeRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL, reg);
    }

    /**
     * Get the accelerometer output data rate.
     * See {@link AccelGyroOutputDataRate}.
     *
     * @throws IOException
     */
    public int getAccelerometerOdr() throws IOException {
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL);
        return (reg & 0b11100000) & 0xFF;
    }

    /**
     * Set the accelerometer output data rate.
     * Must be one of the {@link AccelGyroOutputDataRate} values.
     *
     * @throws IOException
     */
    public void setAccelerometerOdr(@AccelGyroOutputDataRate int odr) throws IOException {
        // We need to preserve the other bytes in CTRL_REG6_XL. So, first read it:
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL);
        // Then mask out the accel scale bits:
        reg &= ~(0b11100000);
        // Then mask in our new scale bits:
        reg |= odr;
        // And write the new register value back into CTRL_REG6_XL:
        writeRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL, reg);
    }

    /**
     * Get the accelerometer highResolution.
     *
     * @throws IOException
     */
    public boolean getAccelerometerHighResolution() throws IOException {
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG6_XL);
        return (byte) ((reg & 0b10000000) & 0xFF) == CTRL_REG7_XL_HR;
    }

    /**
     * Set the accelerometer highResolution.
     *
     * @throws IOException
     */
    public void setAccelerometerHighResolution(boolean enable) throws IOException {
        // We need to preserve the other bytes in CTRL_REG7_XL. So, first read it:
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG7_XL);
        // Then mask out the accel high resolution bits:
        reg &= ~(CTRL_REG7_XL_HR);
        if (enable) {
            // Then mask in our new high resolution bits:
            reg |= CTRL_REG7_XL_HR;
        }
        // And write the new register value back into CTRL_REG7_XL:
        writeRegByte(SENSOR_XG, REGISTER_CTRL_REG7_XL, reg);
    }

    /**
     * Get the accelerometer enabled axes.
     *
     * @return bit mask made with {@link #CTRL_REG5_XL_YEN_XL},
     * {@link #CTRL_REG5_XL_YEN_XL} or {@link #CTRL_REG5_XL_ZEN_XL}.
     * @throws IOException
     */
    public int getAccelerometerEnabledAxes() throws IOException {
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG5_XL);
        return (reg & 0b00111000) & 0xFF;
    }

    /**
     * Set the accelerometer enabled axes.
     *
     * @param axesFlag bit mask made with {@link #CTRL_REG5_XL_YEN_XL},
     *                 {@link #CTRL_REG5_XL_YEN_XL} or {@link #CTRL_REG5_XL_ZEN_XL}.
     * @throws IOException
     */
    private void setAccelerometerEnabledAxes(int axesFlag) throws IOException {
        // We need to preserve the other bytes in CTRL_REG5_XL. So, first read it:
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG5_XL);
        // Then mask out the gyro axes bits:
        reg &= ~(0b00111000);
        // Then mask in our new axes bits:
        reg |= axesFlag;
        // And write the new register value back into CTRL_REG5_XL:
        writeRegByte(SENSOR_XG, REGISTER_CTRL_REG5_XL, reg);
    }

    /**
     * Get the gyroscope scale.
     * See {@link GyroscopeScale}.
     *
     * @throws IOException
     */
    public int getGyroscopeScale() throws IOException {
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG1_G);
        return (reg & 0b00011000) & 0xFF;
    }

    /**
     * Set the gyroscope scale.
     * Must be one of the {@link GyroscopeScale} values.
     *
     * @throws IOException
     */
    private void setGyroscopeScale(@GyroscopeScale int scale) throws IOException {
        // We need to preserve the other bytes in CTRL_REG1_G. So, first read it:
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG1_G);
        // Then mask out the gyro scale bits:
        reg &= ~(0b00011000);
        // Then mask in our new scale bits:
        reg |= scale;
        // And write the new register value back into CTRL_REG1_G:
        writeRegByte(SENSOR_XG, REGISTER_CTRL_REG1_G, reg);

        switch (scale) {
            case GYRO_SCALE_245DPS:
                mGyroDpsDigit = GYRO_DPS_DIGIT_245DPS;
                break;
            case GYRO_SCALE_500DPS:
                mGyroDpsDigit = GYRO_DPS_DIGIT_500DPS;
                break;
            case GYRO_SCALE_2000DPS:
                mGyroDpsDigit = GYRO_DPS_DIGIT_2000DPS;
                break;
        }
    }

    /**
     * Get the gyroscope output data rate.
     *
     * @throws IOException
     */
    public int getGyroscopeOdr() throws IOException {
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG1_G);
        return (reg & 0b11100000) & 0xFF;
    }

    /**
     * Set the gyroscope output data rate.
     * Must be one of the {@link AccelGyroOutputDataRate} values.
     *
     * @throws IOException
     */
    private void setGyroscopeOdr(@AccelGyroOutputDataRate int odr) throws IOException {
        // We need to preserve the other bytes in CTRL_REG1_G. So, first read it:
        byte reg = readRegByte(SENSOR_XG, REGISTER_CTRL_REG1_G);
        // Then mask out the gyro odr bits:
        reg &= ~(0b11100000);
        // Then mask in our new odr bits:
        reg |= odr;
        // And write the new register value back into CTRL_REG1_G:
        writeRegByte(SENSOR_XG, REGISTER_CTRL_REG1_G, reg);
    }

    /**
     * Get the gyroscope angular rate negative sign axes.
     *
     * @return bit mask made with {@link #ORIENT_CFG_G_SIGN_X_G},
     * {@link #ORIENT_CFG_G_SIGN_Y_G} or {@link #ORIENT_CFG_G_SIGN_Z_G}.
     * @throws IOException
     */
    public int getGyroscopeAxesAngularRateNegativeSign() throws IOException {
        byte reg = readRegByte(SENSOR_XG, REGISTER_ORIENT_CFG_G);
        return (reg & 0b00111000) & 0xFF;
    }

    /**
     * Set the gyroscope angular rate negative sign axes.
     *
     * @param axesFlag bit mask made with {@link #ORIENT_CFG_G_SIGN_X_G},
     *                 {@link #ORIENT_CFG_G_SIGN_Y_G} or {@link #ORIENT_CFG_G_SIGN_Z_G}.
     * @throws IOException
     */
    private void setGyroscopeAxesAngularRateNegativeSign(int axesFlag) throws IOException {
        // We need to preserve the other bytes in ORIENT_CFG_G. So, first read it:
        byte reg = readRegByte(SENSOR_XG, REGISTER_ORIENT_CFG_G);
        // Then mask out the gyro axes bits:
        reg &= ~(0b00111000);
        // Then mask in our new axes bits:
        reg |= axesFlag;
        // And write the new register value back into ORIENT_CFG_G:
        writeRegByte(SENSOR_XG, REGISTER_ORIENT_CFG_G, reg);
    }

    /**
     * Get the magnetometer gain.
     *
     * @throws IOException
     */
    public int getMagnetometerGain() throws IOException {
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG2_M);
        return (reg & 0b01100000) & 0xFF;
    }

    /**
     * Set the magnetometer gain.
     * Must be one of the {@link MagnetometerGain} values.
     *
     * @throws IOException
     */
    private void setMagnetometerGain(@MagnetometerGain int gain) throws IOException {
        // We need to preserve the other bytes in CTRL_REG2_M. So, first read it:
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG2_M);
        // Then mask out the mag gain bits:
        reg &= ~(0b01100000);
        // Then mask in our new gain bits:
        reg |= gain;
        // And write the new register value back into CTRL_REG2_M:
        writeRegByte(SENSOR_MAG, REGISTER_CTRL_REG2_M, reg);

        switch (gain) {
            case MAG_GAIN_4GAUSS:
                mMagMgaussLsb = MAG_MGAUSS_4GAUSS;
                break;
            case MAG_GAIN_8GAUSS:
                mMagMgaussLsb = MAG_MGAUSS_8GAUSS;
                break;
            case MAG_GAIN_12GAUSS:
                mMagMgaussLsb = MAG_MGAUSS_12GAUSS;
                break;
            case MAG_GAIN_16GAUSS:
                mMagMgaussLsb = MAG_MGAUSS_16GAUSS;
                break;
        }
    }

    /**
     * Get the magnetometer system operating mode.
     *
     * @throws IOException
     */
    public int getMagnetometerSystemOperatingMode() throws IOException {
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG3_M);
        return (reg & 0b00000011) & 0xFF;
    }

    /**
     * Set the magnetometer operating mode.
     * Must be one of the {@link MagnetometerSystemOperatingMode} values.
     *
     * @throws IOException
     */
    private void setMagnetometerSystemOperatingMode(@MagnetometerSystemOperatingMode int mode) throws IOException {
        // We need to preserve the other bytes in CTRL_REG3_M. So, first read it:
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG3_M);
        // Then mask out the mag operatingMode bits:
        reg &= ~(0b00000011);
        // Then mask in our new operatingMode bits:
        reg |= mode;
        // And write the new register value back into CTRL_REG3_M:
        writeRegByte(SENSOR_MAG, REGISTER_CTRL_REG3_M, reg);
    }

    /**
     * Get the magnetometer XY operating mode.
     *
     * @throws IOException
     */
    public int getMagnetometerXYOperatingMode() throws IOException {
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG1_M);
        return (reg & 0b01100000) & 0xFF;
    }

    /**
     * Set the magnetometer operating mode.
     * Must be one of the {@link MagnetometerXYOperatingMode} values.
     *
     * @throws IOException
     */
    private void setMagnetometerXYOperatingMode(@MagnetometerXYOperatingMode int mode) throws IOException {
        // We need to preserve the other bytes in CTRL_REG1_M. So, first read it:
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG1_M);
        // Then mask out the mag operatingMode bits:
        reg &= ~(0b01100000);
        // Then mask in our new operatingMode bits:
        reg |= mode;
        // And write the new register value back into CTRL_REG1_M:
        writeRegByte(SENSOR_MAG, REGISTER_CTRL_REG1_M, reg);
    }

    /**
     * Get the magnetometer Z operating mode.
     *
     * @throws IOException
     */
    public int getMagnetometerZOperatingMode() throws IOException {
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG4_M);
        return (reg & 0b00001100) & 0xFF;
    }

    /**
     * Set the magnetometer operating mode.
     * Must be one of the {@link MagnetometerZOperatingMode} values.
     *
     * @throws IOException
     */
    private void setMagnetometerZOperatingMode(@MagnetometerZOperatingMode int mode) throws IOException {
        // We need to preserve the other bytes in CTRL_REG4_M. So, first read it:
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG4_M);
        // Then mask out the mag operatingMode bits:
        reg &= ~(0b00001100);
        // Then mask in our new operatingMode bits:
        reg |= mode;
        // And write the new register value back into CTRL_REG4_M:
        writeRegByte(SENSOR_MAG, REGISTER_CTRL_REG4_M, reg);
    }

    /**
     * Get the magnetometer temperatureCompensation.
     *
     * @throws IOException
     */
    public boolean getMagnetometerTemperatureCompensation() throws IOException {
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG1_M);
        return (byte) ((reg & CTRL_REG1_M_TEMP_COMP) & 0xFF) == CTRL_REG1_M_TEMP_COMP;
    }

    /**
     * Set the magnetometer temperatureCompensation.
     *
     * @throws IOException
     */
    private void setMagnetometerTemperatureCompensation(boolean enable) throws IOException {
        // We need to preserve the other bytes in CTRL_REG1_M. So, first read it:
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG1_M);
        // Then mask out the mag temperature compensation bits:
        reg &= ~(CTRL_REG1_M_TEMP_COMP);
        if (enable) {
            // Then mask in our new temperature compensation bits:
            reg |= CTRL_REG1_M_TEMP_COMP;
        }
        // And write the new register value back into CTRL_REG1_M:
        writeRegByte(SENSOR_MAG, REGISTER_CTRL_REG1_M, reg);
    }

    /**
     * Get the magnetometer output data rate.
     * See {@link MagOutputDataRate}.
     *
     * @throws IOException
     */
    public int getMagnetometerOdr() throws IOException {
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG1_M);
        return (reg & 0b00011100) & 0xFF;
    }

    /**
     * Set the magnetometer output data rate.
     * Must be one of the {@link MagOutputDataRate} values.
     *
     * @throws IOException
     */
    public void setMagnetometerOdr(@MagOutputDataRate int odr) throws IOException {
        // We need to preserve the other bytes in CTRL_REG1_M. So, first read it:
        byte reg = readRegByte(SENSOR_MAG, REGISTER_CTRL_REG1_M);
        // Then mask out the accel scale bits:
        reg &= ~(0b00011100);
        // Then mask in our new scale bits:
        reg |= odr;
        // And write the new register value back into CTRL_REG1_M:
        writeRegByte(SENSOR_MAG, REGISTER_CTRL_REG1_M, reg);
    }

    /**
     * Read the raw accelerometer sensor values.
     * <p>
     * If you want the acceleration in SI units (m/s^2) use the {@link #getAcceleration()}.
     *
     * @return an integer array containing X, Y, Z axis raw values.
     * @throws IOException
     */
    public int[] getRawAccelerometerData() throws IOException {
        byte[] buffer = new byte[6];
        int[] result = new int[3];
        readRegBuffer(SENSOR_XG, REGISTER_OUT_X_L_XL, buffer, buffer.length);
        result[0] = (buffer[1] << 8) | buffer[0]; // Store x-axis values
        result[1] = (buffer[3] << 8) | buffer[2]; // Store y-axis values
        result[2] = (buffer[5] << 8) | buffer[4]; // Store z-axis values
        return result;
    }

    /**
     * Get the acceleration on the X, Y, Z axis in SI units (m/s^2).
     *
     * @return a float array containing X, Y, Z axis values in SI units (m/s^2).
     * @throws IOException
     */
    public float[] getAcceleration() throws IOException {
        int[] rawAccelerometerData = getRawAccelerometerData();
        float[] result = new float[3];
        for (int i = 0; i < rawAccelerometerData.length; i++) {
            result[i] = rawAccelerometerData[i] * mAccelMgLsb / 1000f * mGravity;
        }
        return result;
    }

    /**
     * Read the raw magnetometer sensor values.
     * <p>
     * If you want the magnetic induction in SI units (Gs) use the {@link #getMagneticInduction()}.
     *
     * @return an integer array containing X, Y, Z axis raw values.
     * @throws IOException
     */
    public int[] getRawMagnetometerData() throws IOException {
        byte[] buffer = new byte[6];
        int[] result = new int[3];
        readRegBuffer(SENSOR_MAG, REGISTER_OUT_X_L_M, buffer, buffer.length);
        result[0] = (buffer[1] << 8) | buffer[0]; // Store x-axis values
        result[1] = (buffer[3] << 8) | buffer[2]; // Store y-axis values
        result[2] = (buffer[5] << 8) | buffer[4]; // Store z-axis values
        return result;
    }

    /**
     * Get the magnetic induction on the X, Y, Z axis in SI units (Gs).
     *
     * @return a float array containing X, Y, Z axis values in SI units (Gs).
     * @throws IOException
     */
    public float[] getMagneticInduction() throws IOException {
        int[] rawMagnetometerData = getRawMagnetometerData();
        float[] result = new float[3];
        for (int i = 0; i < rawMagnetometerData.length; i++) {
            result[i] = rawMagnetometerData[i] * mMagMgaussLsb / 1000f;
        }
        return result;
    }

    /**
     * Read the raw gyroscope sensor values.
     * <p>
     * If you want the angular velocity in SI units (deg/s) use the {@link #getAngularVelocity()}.
     *
     * @return an integer array containing X, Y, Z axis raw values.
     * @throws IOException
     */
    public int[] getRawGyroscopeData() throws IOException {
        byte[] buffer = new byte[6];
        int[] result = new int[3];
        readRegBuffer(SENSOR_XG, REGISTER_OUT_X_L_G, buffer, buffer.length);
        result[0] = (buffer[1] << 8) | buffer[0]; // Store x-axis values
        result[1] = (buffer[3] << 8) | buffer[2]; // Store y-axis values
        result[2] = (buffer[5] << 8) | buffer[4]; // Store z-axis values
        return result;
    }

    /**
     * Get the angular velocity on the X, Y, Z axis in SI units (deg/s).
     *
     * @return a float array containing X, Y, Z axis values in SI units (deg/s).
     * @throws IOException
     */
    public float[] getAngularVelocity() throws IOException {
        int[] rawGyroscopeData = getRawGyroscopeData();
        float[] result = new float[3];
        for (int i = 0; i < rawGyroscopeData.length; i++) {
            result[i] = rawGyroscopeData[i] * mGyroDpsDigit;
        }
        return result;
    }

    /**
     * Read the Temperature data output register.
     * {@link #REGISTER_TEMP_OUT_L} and {@link #REGISTER_TEMP_OUT_H} registers together
     * express a 16-bit word in two's complement right-justified quoted at 16 LSB/⁰C.
     *
     * @return raw data temperature.
     * @throws IOException
     */
    public int getRawTemperature() throws IOException {
        byte[] buffer = new byte[2];
        readRegBuffer(SENSOR_XG, REGISTER_TEMP_OUT_L, buffer, buffer.length);
        return ((int) buffer[1] << 8) | buffer[0];
    }

    /**
     * Get the temperature of the sensor in degrees Celsius.
     * <p>
     * The intent of the temperature sensor is to keep track of the (gyro) die
     * temperature and compensate if necessary. It is not intended as an
     * environmental sensor.
     *
     * @return the temperature in degrees Celsius.
     * @throws IOException
     */
    public float getTemperature() throws IOException {
        return getRawTemperature() / TEMP_LSB_DEGREE_CELSIUS + TEMP_BIAS;
    }

    /**
     * Get the current value used for gravity in SI units (m/s^2).
     */
    public float getGravity() {
        return mGravity;
    }

    /**
     * Set the current value used for gravity in SI units (m/s^2).
     */
    public void setGravity(float gravity) {
        mGravity = gravity;
    }

    /**
     * Enable/disable Gyroscope sleep mode.
     *
     * @param enable True to enable Gyroscope sleep mode; false to Gyroscope sleep mode.
     * @throws IOException
     */
    public void setSleepGyroscopeEnabled(boolean enable) throws IOException {
        byte temp = readRegByte(SENSOR_XG, REGISTER_CTRL_REG9);
        if (enable) {
            temp |= CTRL_REG9_SLEEP_G;
        } else {
            temp &= ~CTRL_REG9_SLEEP_G;
        }
        writeRegByte(SENSOR_XG, REGISTER_CTRL_REG9, temp);
    }

    /**
     * Enable/disable FIFO memory.
     *
     * @param enable True to enable FIFO memory; false to disable FIFO memory.
     * @throws IOException
     */
    public void setFifoMemoryEnabled(boolean enable) throws IOException {
        byte temp = readRegByte(SENSOR_XG, REGISTER_CTRL_REG9);
        if (enable) {
            temp |= CTRL_REG9_FIFO_EN;
        } else {
            temp &= ~CTRL_REG9_FIFO_EN;
        }
        writeRegByte(SENSOR_XG, REGISTER_CTRL_REG9, temp);
    }

    /**
     * Configure FIFO mode and Threshold.
     *
     * @param mode      Set FIFO mode to off, FIFO (stop when full), continuous, bypass. See {@link FifoMode}.
     * @param threshold FIFO threshold level setting (0-31).
     * @throws IOException
     */
    public void setFifoModeAndTreshold(@FifoMode int mode, int threshold) throws IOException {
        if (threshold > FIFO_MAX_THRESHOLD) {
            threshold = FIFO_MAX_THRESHOLD;
        }
        writeRegByte(SENSOR_XG, REGISTER_FIFO_CTRL, (byte) (mode | (threshold & 0b0011111)));
    }

    /**
     * Number of unread samples stored into FIFO (0-32).
     *
     * @return The number of unread samples.
     * @throws IOException
     */
    public int getFifoSamplesCount() throws IOException {
        return readRegByte(SENSOR_XG, REGISTER_FIFO_SRC) & 0b00111111;
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (mAccelGyroDevice != null) {
            try {
                mAccelGyroDevice.close();
            } finally {
                mAccelGyroDevice = null;
            }
        }
        if (mMagDevice != null) {
            try {
                mMagDevice.close();
            } finally {
                mMagDevice = null;
            }
        }
    }

    /**
     * Read a byte from a given register.
     *
     * @param sensorType The sensor to read from (see {@link SensorType}).
     * @param reg        The register to read from (0x00-0xFF).
     * @return The value read from the device.
     * @throws IOException
     */
    private byte readRegByte(@SensorType int sensorType, int reg) throws IOException {
        if (sensorType == SENSOR_MAG) {
            if (mMagDevice == null) {
                throw new IllegalStateException("I2C device not open");
            }
            return mMagDevice.readRegByte(reg);
        } else {
            if (mAccelGyroDevice == null) {
                throw new IllegalStateException("I2C device not open");
            }
            return mAccelGyroDevice.readRegByte(reg);
        }
    }

    /**
     * Read multiple bytes from a given register.
     *
     * @param sensorType The sensor to read from (see {@link SensorType})
     * @param buffer     Buffer to read data into.
     * @param length     Number of bytes to read, may not be larger than the buffer size.
     * @return The value read from the device.
     * @throws IOException
     */
    private void readRegBuffer(@SensorType int sensorType, int reg, byte[] buffer, int length) throws IOException {
        if (sensorType == SENSOR_MAG) {
            if (mMagDevice == null) {
                throw new IllegalStateException("I2C device not open");
            }
            mMagDevice.readRegBuffer(reg, buffer, length);
        } else {
            if (mAccelGyroDevice == null) {
                throw new IllegalStateException("I2C device not open");
            }
            mAccelGyroDevice.readRegBuffer(reg, buffer, length);
        }
    }

    /**
     * Write a byte to a given register.
     *
     * @param sensorType The sensor to write to (see {@link SensorType})
     * @param reg        The register to write to (0x00-0xFF).
     * @throws IOException
     */
    private void writeRegByte(@SensorType int sensorType, int reg, byte data) throws IOException {
        if (sensorType == SENSOR_MAG) {
            if (mMagDevice == null) {
                throw new IllegalStateException("I2C device not open");
            }
            mMagDevice.writeRegByte(reg, data);
        } else {
            if (mAccelGyroDevice == null) {
                throw new IllegalStateException("I2C device not open");
            }
            mAccelGyroDevice.writeRegByte(reg, data);
        }

    }

    /**
     * Accelerometer range
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACCEL_RANGE_2G,
            ACCEL_RANGE_4G,
            ACCEL_RANGE_8G,
            ACCEL_RANGE_16G
    })
    public @interface AccelerometerRange {
        int ACCEL_RANGE_2G = 0;
        int ACCEL_RANGE_16G = 1 << 3;
        int ACCEL_RANGE_4G = 2 << 3;
        int ACCEL_RANGE_8G = 3 << 3;
    }

    /**
     * Accelerometer Decimation values
     * Decimation of acceleration data on OUT REG and FIFO.
     * <ul>
     * <li>{@link #ACCEL_DEC_0_SAMPLES}: no decimation
     * <li>{@link #ACCEL_DEC_2_SAMPLES}: update every 2 samples
     * <li>{@link #ACCEL_DEC_4_SAMPLES}: update every 4 samples
     * <li>{@link #ACCEL_DEC_8_SAMPLES}: update every 8 samples
     * </ul><p>
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACCEL_DEC_0_SAMPLES,
            ACCEL_DEC_2_SAMPLES,
            ACCEL_DEC_4_SAMPLES,
            ACCEL_DEC_8_SAMPLES
    })
    public @interface AccelerometerDecimation {
        int ACCEL_DEC_0_SAMPLES = 0b00000000;
        int ACCEL_DEC_2_SAMPLES = 0b01000000;
        int ACCEL_DEC_4_SAMPLES = (byte) 0b10000000;
        int ACCEL_DEC_8_SAMPLES = (byte) 0b11000000;
    }

    /**
     * Gyroscope scale
     * <ul>
     * <li>{@link #GYRO_SCALE_245DPS}: +/- 245 degrees per second rotation
     * <li>{@link #GYRO_SCALE_500DPS}: +/- 500 degrees per second rotation
     * <li>{@link #GYRO_SCALE_2000DPS}: +/- 2000 degrees per second rotation
     * </ul><p>
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GYRO_SCALE_245DPS,
            GYRO_SCALE_500DPS,
            GYRO_SCALE_2000DPS
    })
    public @interface GyroscopeScale {
        int GYRO_SCALE_245DPS = 0;
        int GYRO_SCALE_500DPS = 1 << 4;
        int GYRO_SCALE_2000DPS = 3 << 4;
    }

    /**
     * All possible data rate/bandwidth combos of the Accelerometer/Gyroscope
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ODR_POWER_DOWN,
            ODR_14_9HZ,
            ODR_59_5HZ,
            ODR_119HZ,
            ODR_238HZ,
            ODR_476HZ,
            ODR_952HZ
    })
    public @interface AccelGyroOutputDataRate {
        int ODR_POWER_DOWN = 0; // Power down
        int ODR_14_9HZ = 1 << 5;  // 14.9 Hz
        int ODR_59_5HZ = 2 << 5;  // 59.5 Hz
        int ODR_119HZ = 3 << 5;   // 119 Hz
        int ODR_238HZ = 4 << 5;   // 238 Hz
        int ODR_476HZ = 5 << 5;   // 476 Hz
        int ODR_952HZ = 6 << 5;   // 952 Hz
    }

    /**
     * Magnetometer gain
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MAG_GAIN_4GAUSS,
            MAG_GAIN_8GAUSS,
            MAG_GAIN_12GAUSS,
            MAG_GAIN_16GAUSS
    })
    public @interface MagnetometerGain {
        int MAG_GAIN_4GAUSS = 0;       // +/- 4 gauss
        int MAG_GAIN_8GAUSS = 1 << 5;  // +/- 8 gauss
        int MAG_GAIN_12GAUSS = 2 << 5; // +/- 12 gauss
        int MAG_GAIN_16GAUSS = 3 << 5; // +/- 16 gauss
    }

    /**
     * Magnetometer system operating mode
     * <ul>
     * <li>{@link #MAG_CONTINUOUS_CONVERSION}: Continuous-conversion mode
     * <li>{@link #MAG_SINGLE_CONVERSION}: Single-conversion mode
     * <li>{@link #MAG_POWER_DOWN}: Power-down mode
     * </ul><p>
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MAG_CONTINUOUS_CONVERSION,
            MAG_SINGLE_CONVERSION,
            MAG_POWER_DOWN
    })
    public @interface MagnetometerSystemOperatingMode {
        int MAG_CONTINUOUS_CONVERSION = 0;
        int MAG_SINGLE_CONVERSION = 1;
        int MAG_POWER_DOWN = 2;
    }

    /**
     * Magnetometer XY operating mode
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MAG_XY_OM_LOW_POWER,
            MAG_XY_OM_MEDIUM_PERFORMANCE,
            MAG_XY_OM_HIGH_PERFORMANCE,
            MAG_XY_OM_ULTRA_HIGH_PERFORMANCE
    })
    public @interface MagnetometerXYOperatingMode {
        int MAG_XY_OM_LOW_POWER = 0;
        int MAG_XY_OM_MEDIUM_PERFORMANCE = 1 << 5;
        int MAG_XY_OM_HIGH_PERFORMANCE = 2 << 5;
        int MAG_XY_OM_ULTRA_HIGH_PERFORMANCE = 3 << 5;
    }

    /**
     * Magnetometer Z operating mode
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MAG_Z_OM_LOW_POWER,
            MAG_Z_OM_MEDIUM_PERFORMANCE,
            MAG_Z_OM_HIGH_PERFORMANCE,
            MAG_Z_OM_ULTRA_HIGH_PERFORMANCE
    })
    public @interface MagnetometerZOperatingMode {
        int MAG_Z_OM_LOW_POWER = 0;
        int MAG_Z_OM_MEDIUM_PERFORMANCE = 1 << 2;
        int MAG_Z_OM_HIGH_PERFORMANCE = 2 << 2;
        int MAG_Z_OM_ULTRA_HIGH_PERFORMANCE = 3 << 2;
    }

    /**
     * All possible data rate/bandwidth combos of the Accelerometer/Gyroscope
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ODR_M_0_625HZ,
            ODR_M_1_25HZ,
            ODR_M_2_5HZ,
            ODR_M_6HZ,
            ODR_M_10HZ,
            ODR_M_20HZ,
            ODR_M_40HZ,
            ODR_M_80HZ
    })
    public @interface MagOutputDataRate {
        int ODR_M_0_625HZ = 0;
        int ODR_M_1_25HZ = 1 << 2;
        int ODR_M_2_5HZ = 2 << 2;
        int ODR_M_6HZ = 3 << 2;
        int ODR_M_10HZ = 4 << 2;
        int ODR_M_20HZ = 5 << 2;
        int ODR_M_40HZ = 6 << 2;
        int ODR_M_80HZ = 7 << 2;
    }

    /**
     * FIFO mode
     * <ul>
     * <li>{@link #FIFO_OFF}: Bypass mode. FIFO turned off
     * <li>{@link #FIFO_THS}: FIFO mode. Stops collecting data when FIFO is full
     * <li>{@link #FIFO_CONT_TRIGGER}: Continuous mode until trigger is deasserted, then FIFO mode
     * <li>{@link #FIFO_OFF_TRIGGER}:Bypass mode until trigger is deasserted, then Continuous mode
     * <li>{@link #FIFO_CONT}: Continuous mode. If the FIFO is full, the new sample overwrites the older sample
     * </ul><p>
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FifoMode.FIFO_OFF,
            FifoMode.FIFO_THS,
            FifoMode.FIFO_CONT_TRIGGER,
            FifoMode.FIFO_OFF_TRIGGER,
            FifoMode.FIFO_CONT
    })
    public @interface FifoMode {
        int FIFO_OFF = 0;
        int FIFO_THS = 1 << 5;
        int FIFO_CONT_TRIGGER = 3 << 5;
        int FIFO_OFF_TRIGGER = 4 << 5;
        int FIFO_CONT = 5 << 5;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SENSOR_MAG, SENSOR_XG})
    @interface SensorType {
        int SENSOR_MAG = 0;
        int SENSOR_XG = 1;
    }
}
