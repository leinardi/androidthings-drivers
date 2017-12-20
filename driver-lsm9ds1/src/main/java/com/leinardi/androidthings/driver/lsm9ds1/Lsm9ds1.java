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

import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerRange.LSM9DS1_ACCELRANGE_16G;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerRange.LSM9DS1_ACCELRANGE_2G;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerRange.LSM9DS1_ACCELRANGE_4G;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelerometerRange.LSM9DS1_ACCELRANGE_8G;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.GyroscopeScale.LSM9DS1_GYROSCALE_2000DPS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.GyroscopeScale.LSM9DS1_GYROSCALE_245DPS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.GyroscopeScale.LSM9DS1_GYROSCALE_500DPS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerGain.LSM9DS1_MAGGAIN_12GAUSS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerGain.LSM9DS1_MAGGAIN_16GAUSS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerGain.LSM9DS1_MAGGAIN_4GAUSS;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerGain.LSM9DS1_MAGGAIN_8GAUSS;

/**
 * Driver for the LSM9DS1 3D accelerometer, 3D gyroscope, 3D magnetometer and temperature sensor.
 */
public class Lsm9ds1 implements Closeable {
    private static final String TAG = Lsm9ds1.class.getSimpleName();
    // Accelerometer/Gyroscope registers
    private static final int LSM9DS1_REGISTER_WHO_AM_I_XG = 0x0F;
    private static final int LSM9DS1_REGISTER_CTRL_REG1_G = 0x10;
    private static final int LSM9DS1_REGISTER_CTRL_REG2_G = 0x11;
    private static final int LSM9DS1_REGISTER_CTRL_REG3_G = 0x12;
    private static final int LSM9DS1_REGISTER_TEMP_OUT_L = 0x15;
    private static final int LSM9DS1_REGISTER_TEMP_OUT_H = 0x16;
    private static final int LSM9DS1_REGISTER_STATUS_REG = 0x17;
    private static final int LSM9DS1_REGISTER_OUT_X_L_G = 0x18;
    private static final int LSM9DS1_REGISTER_OUT_X_H_G = 0x19;
    private static final int LSM9DS1_REGISTER_OUT_Y_L_G = 0x1A;
    private static final int LSM9DS1_REGISTER_OUT_Y_H_G = 0x1B;
    private static final int LSM9DS1_REGISTER_OUT_Z_L_G = 0x1C;
    private static final int LSM9DS1_REGISTER_OUT_Z_H_G = 0x1D;
    private static final int LSM9DS1_REGISTER_CTRL_REG4 = 0x1E;
    private static final int LSM9DS1_REGISTER_CTRL_REG5_XL = 0x1F;
    private static final int LSM9DS1_REGISTER_CTRL_REG6_XL = 0x20;
    private static final int LSM9DS1_REGISTER_CTRL_REG7_XL = 0x21;
    private static final int LSM9DS1_REGISTER_CTRL_REG8 = 0x22;
    private static final int LSM9DS1_REGISTER_CTRL_REG9 = 0x23;
    private static final int LSM9DS1_REGISTER_CTRL_REG10 = 0x24;
    private static final int LSM9DS1_REGISTER_OUT_X_L_XL = 0x28;
    private static final int LSM9DS1_REGISTER_OUT_X_H_XL = 0x29;
    private static final int LSM9DS1_REGISTER_OUT_Y_L_XL = 0x2A;
    private static final int LSM9DS1_REGISTER_OUT_Y_H_XL = 0x2B;
    private static final int LSM9DS1_REGISTER_OUT_Z_H_XL = 0x2D;
    private static final int LSM9DS1_REGISTER_OUT_Z_L_XL = 0x2C;
    // Magnetometer registers
    private static final int LSM9DS1_REGISTER_WHO_AM_I_M = 0x0F;
    private static final int LSM9DS1_REGISTER_CTRL_REG1_M = 0x20;
    private static final int LSM9DS1_REGISTER_CTRL_REG2_M = 0x21;
    private static final int LSM9DS1_REGISTER_CTRL_REG3_M = 0x22;
    private static final int LSM9DS1_REGISTER_CTRL_REG4_M = 0x23;
    private static final int LSM9DS1_REGISTER_CTRL_REG5_M = 0x24;
    private static final int LSM9DS1_REGISTER_STATUS_REG_M = 0x27;
    private static final int LSM9DS1_REGISTER_OUT_X_L_M = 0x28;
    private static final int LSM9DS1_REGISTER_OUT_X_H_M = 0x29;
    private static final int LSM9DS1_REGISTER_OUT_Y_L_M = 0x2A;
    private static final int LSM9DS1_REGISTER_OUT_Y_H_M = 0x2B;
    private static final int LSM9DS1_REGISTER_OUT_Z_L_M = 0x2C;
    private static final int LSM9DS1_REGISTER_OUT_Z_H_M = 0x2D;
    private static final int LSM9DS1_REGISTER_CFG_M = 0x30;
    private static final int LSM9DS1_REGISTER_INT_SRC_M = 0x31;
    // Accelerometer data rate
    private static final int LSM9DS1_ACCELDATARATE_POWERDOWN = 0b0000 << 4;
    private static final int LSM9DS1_ACCELDATARATE_3_125HZ = 0b0001 << 4;
    private static final int LSM9DS1_ACCELDATARATE_6_25HZ = 0b0010 << 4;
    private static final int LSM9DS1_ACCELDATARATE_12_5HZ = 0b0011 << 4;
    private static final int LSM9DS1_ACCELDATARATE_25HZ = 0b0100 << 4;
    private static final int LSM9DS1_ACCELDATARATE_50HZ = 0b0101 << 4;
    private static final int LSM9DS1_ACCELDATARATE_100HZ = 0b0110 << 4;
    private static final int LSM9DS1_ACCELDATARATE_200HZ = 0b0111 << 4;
    private static final int LSM9DS1_ACCELDATARATE_400HZ = 0b1000 << 4;
    private static final int LSM9DS1_ACCELDATARATE_800HZ = 0b1001 << 4;
    private static final int LSM9DS1_ACCELDATARATE_1600HZ = 0b1010 << 4;
    // Magnetometer data rate
    private static final int LSM9DS1_MAGDATARATE_3_125HZ = 0b000 << 2;
    private static final int LSM9DS1_MAGDATARATE_6_25HZ = 0b001 << 2;
    private static final int LSM9DS1_MAGDATARATE_12_5HZ = 0b010 << 2;
    private static final int LSM9DS1_MAGDATARATE_25HZ = 0b011 << 2;
    private static final int LSM9DS1_MAGDATARATE_50HZ = 0b100 << 2;
    private static final int LSM9DS1_MAGDATARATE_100HZ = 0b101 << 2;
    // STATUS_REG
    private static final int LSM9DS1_STATUS_REG_IG_XL = 0b01000000;
    private static final int LSM9DS1_STATUS_REG_IG_G = 0b00100000;
    private static final int LSM9DS1_STATUS_REG_INACT = 0b00010000;
    private static final int LSM9DS1_STATUS_REG_BOOT_STATUS = 0b00001000;
    private static final int LSM9DS1_STATUS_REG_TDA = 0b00000100;
    private static final int LSM9DS1_STATUS_REG_GDA = 0b00000010;
    private static final int LSM9DS1_STATUS_REG_XLDA = 0b00000001;

    // STATUS_REG_M
    private static final int LSM9DS1_STATUS_REG_M_ZYXOR = 0b10000000;
    private static final int LSM9DS1_STATUS_REG_M_ZOR = 0b01000000;
    private static final int LSM9DS1_STATUS_REG_M_YOR = 0b00100000;
    private static final int LSM9DS1_STATUS_REG_M_XOR = 0b00010000;
    private static final int LSM9DS1_STATUS_REG_M_ZYXDA = 0b00001000;
    private static final int LSM9DS1_STATUS_REG_M_ZDA = 0b00000100;
    private static final int LSM9DS1_STATUS_REG_M_YDA = 0b00000010;
    private static final int LSM9DS1_STATUS_REG_M_XDA = 0b00000001;

    private static final float LSM9DS1_TEMP_SCALE = 16.0F;
    private static final float LSM9DS1_TEMP_BIAS = 27.5F; // This is an empirical estimation
    private static final int LSM9DS1_ADDRESS_ACCELGYRO = 0x6B;
    private static final int LSM9DS1_ADDRESS_MAG = 0x1E;
    private static final byte LSM9DS1_XG_ID = 0b01101000;
    private static final byte LSM9DS1_MAG_ID = 0b00111101;
    // Linear Acceleration: mg per LSB
    private static final float LSM9DS1_ACCEL_MG_LSB_2G = 0.061F;
    private static final float LSM9DS1_ACCEL_MG_LSB_4G = 0.122F;
    private static final float LSM9DS1_ACCEL_MG_LSB_8G = 0.244F;
    private static final float LSM9DS1_ACCEL_MG_LSB_16G = 0.732F;
    // Magnetic Field Strength: gauss range
    private static final float LSM9DS1_MAG_MGAUSS_4GAUSS = 0.14F;
    private static final float LSM9DS1_MAG_MGAUSS_8GAUSS = 0.29F;
    private static final float LSM9DS1_MAG_MGAUSS_12GAUSS = 0.43F;
    private static final float LSM9DS1_MAG_MGAUSS_16GAUSS = 0.58F;
    // Angular Rate: dps per LSB
    private static final float LSM9DS1_GYRO_DPS_DIGIT_245DPS = 0.00875F;
    private static final float LSM9DS1_GYRO_DPS_DIGIT_500DPS = 0.01750F;
    private static final float LSM9DS1_GYRO_DPS_DIGIT_2000DPS = 0.07000F;
    // Temperature: LSB per degree celsius
    private static final int LSM9DS1_TEMP_LSB_DEGREE_CELSIUS = 8;  // 1°C = 8, 25° = 200, etc.
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
        this(bus, LSM9DS1_ADDRESS_ACCELGYRO, LSM9DS1_ADDRESS_MAG);
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

        // soft reset & reboot accel/gyro
        writeRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG8, (byte) 0x05);
        // soft reset & reboot magnetometer
        writeRegByte(SensorType.MAG, LSM9DS1_REGISTER_CTRL_REG2_M, (byte) 0x0C);

        SystemClock.sleep(10);

        byte idXg = readRegByte(SensorType.XG, LSM9DS1_REGISTER_WHO_AM_I_XG);
        byte idMag = readRegByte(SensorType.MAG, LSM9DS1_REGISTER_WHO_AM_I_M);
        if (idXg != LSM9DS1_XG_ID || idMag != LSM9DS1_MAG_ID) {
            throw new IllegalStateException("Could not find LSM9DS1, check wiring!");
        }

        // enable gyro continuous
        writeRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG1_G, (byte) 0xC0); // on XYZ

        // Enable the accelerometer continuous
        writeRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG5_XL, (byte) 0x38); // enable X Y and Z axis
        writeRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG6_XL, (byte) 0xC0); // 1 KHz out data rate, BW set by ODR, 408Hz anti-aliasing

        // enable mag continuous
        //writeRegByte(SensorType.MAG, LSM9DS1_REGISTER_CTRL_REG1_M, (byte) 0xFC); // high perf XY, 80 Hz ODR
        writeRegByte(SensorType.MAG, LSM9DS1_REGISTER_CTRL_REG3_M, (byte) 0x00); // continuous mode
        //writeRegByte(SensorType.MAG, LSM9DS1_REGISTER_CTRL_REG4_M, (byte) 0x0C); // high perf Z mode

        // Set default ranges for the various sensors
        setAccelerometerRange(LSM9DS1_ACCELRANGE_2G);
        setMagnetometerGain(LSM9DS1_MAGGAIN_4GAUSS);
        setGyroscopeScale(LSM9DS1_GYROSCALE_245DPS);
    }

    private byte getStatusRegister(@SensorType int type) throws IOException {
        int reg = type == SensorType.XG ? LSM9DS1_REGISTER_STATUS_REG : LSM9DS1_REGISTER_STATUS_REG_M;
        return readRegByte(type, reg);
    }

    /**
     * Temperature sensor new data available. Default value: false
     *
     * @return false if a new data is not yet available; true if a new data is available
     * @throws IOException
     */
    public boolean isTemperatureNewDataAvailable() throws IOException {
        return (getStatusRegister(SensorType.XG) & LSM9DS1_STATUS_REG_TDA) == LSM9DS1_STATUS_REG_TDA;
    }

    /**
     * Gyroscope new data available. Default value: false
     *
     * @return false if a new set of data is not yet available; true if a new set of data is available
     * @throws IOException
     */
    public boolean isGyroscopeNewDataAvailable() throws IOException {
        return (getStatusRegister(SensorType.XG) & LSM9DS1_STATUS_REG_GDA) == LSM9DS1_STATUS_REG_GDA;
    }

    /**
     * Accelerometer new data available. Default value: false
     *
     * @return false if a new set of data is not yet available; true if a new set of data is available
     * @throws IOException
     */
    public boolean isAccelerometerNewDataAvailable() throws IOException {
        return (getStatusRegister(SensorType.XG) & LSM9DS1_STATUS_REG_XLDA) == LSM9DS1_STATUS_REG_XLDA;
    }

    /**
     * Magnetometer X, Y and Z-axis new data available. Default value: false
     *
     * @return false if a new set of data is not yet available; true if a new set of data is available
     * @throws IOException
     */
    public boolean isMagnetometerXYZAxesNewDataAvailable() throws IOException {
        return (getStatusRegister(SensorType.MAG) & LSM9DS1_STATUS_REG_M_ZYXDA) == LSM9DS1_STATUS_REG_M_ZYXDA;
    }

    /**
     * Magnetometer X-axis new available. Default value: false
     *
     * @return false if a new data for the X-axis is not yet available; true if a new data for the X-axis is available
     * @throws IOException
     */
    public boolean isMagnetometerXAxisNewDataAvailable() throws IOException {
        return (getStatusRegister(SensorType.MAG) & LSM9DS1_STATUS_REG_M_XDA) == LSM9DS1_STATUS_REG_M_XDA;
    }

    /**
     * Magnetometer Y-axis new available. Default value: false
     *
     * @return false if a new data for the Y-axis is not yet available; true if a new data for the Y-axis is available
     * @throws IOException
     */
    public boolean isMagnetometerYAxisNewDataAvailable() throws IOException {
        return (getStatusRegister(SensorType.MAG) & LSM9DS1_STATUS_REG_M_YDA) == LSM9DS1_STATUS_REG_M_YDA;
    }

    /**
     * Magnetometer Z-axis new available. Default value: false
     *
     * @return false if a new data for the Z-axis is not yet available; true if a new data for the Z-axis is available
     * @throws IOException
     */
    public boolean isMagnetometerZAxisNewDataAvailable() throws IOException {
        return (getStatusRegister(SensorType.MAG) & LSM9DS1_STATUS_REG_M_ZDA) == LSM9DS1_STATUS_REG_M_ZDA;
    }

    /**
     * Get the accelerometer range.
     *
     * @throws IOException
     */
    public int getAccelerometerRange() throws IOException {
        byte reg = readRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG6_XL);
        return (reg & 0b00011000) & 0xFF;
    }

    /**
     * Set the accelerometer range.
     * Must be one of the {@link AccelerometerRange} values.
     *
     * @throws IOException
     */
    public void setAccelerometerRange(@AccelerometerRange int range) throws IOException {
        byte reg = readRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG6_XL);
        reg &= ~(0b00011000);
        reg |= range;
        writeRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG6_XL, reg);

        switch (range) {
            case LSM9DS1_ACCELRANGE_2G:
                mAccelMgLsb = LSM9DS1_ACCEL_MG_LSB_2G;
                break;
            case LSM9DS1_ACCELRANGE_4G:
                mAccelMgLsb = LSM9DS1_ACCEL_MG_LSB_4G;
                break;
            case LSM9DS1_ACCELRANGE_8G:
                mAccelMgLsb = LSM9DS1_ACCEL_MG_LSB_8G;
                break;
            case LSM9DS1_ACCELRANGE_16G:
                mAccelMgLsb = LSM9DS1_ACCEL_MG_LSB_16G;
                break;
        }
    }

    /**
     * Get the magnetometer gain.
     *
     * @throws IOException
     */
    public int getMagnetometerGain() throws IOException {
        byte reg = readRegByte(SensorType.MAG, LSM9DS1_REGISTER_CTRL_REG2_M);
        return (reg & 0b01100000) & 0xFF;
    }

    /**
     * Set the magnetometer gain.
     * Must be one of the {@link MagnetometerGain} values.
     *
     * @throws IOException
     */
    private void setMagnetometerGain(@MagnetometerGain int gain) throws IOException {
        byte reg = readRegByte(SensorType.MAG, LSM9DS1_REGISTER_CTRL_REG2_M);
        reg &= ~(0b01100000);
        reg |= gain;
        writeRegByte(SensorType.MAG, LSM9DS1_REGISTER_CTRL_REG2_M, reg);

        switch (gain) {
            case LSM9DS1_MAGGAIN_4GAUSS:
                mMagMgaussLsb = LSM9DS1_MAG_MGAUSS_4GAUSS;
                break;
            case LSM9DS1_MAGGAIN_8GAUSS:
                mMagMgaussLsb = LSM9DS1_MAG_MGAUSS_8GAUSS;
                break;
            case LSM9DS1_MAGGAIN_12GAUSS:
                mMagMgaussLsb = LSM9DS1_MAG_MGAUSS_12GAUSS;
                break;
            case LSM9DS1_MAGGAIN_16GAUSS:
                mMagMgaussLsb = LSM9DS1_MAG_MGAUSS_16GAUSS;
                break;
        }
    }

    /**
     * Get the gyroscope scale.
     *
     * @throws IOException
     */
    public int getGyroscopeScale() throws IOException {
        byte reg = readRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG1_G);
        return (reg & 0b00110000) & 0xFF;
    }

    /**
     * Set the gyroscope scale.
     * Must be one of the {@link GyroscopeScale} values.
     *
     * @throws IOException
     */
    private void setGyroscopeScale(@GyroscopeScale int scale) throws IOException {
        byte reg = readRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG1_G);
        reg &= ~(0b00110000);
        reg |= scale;
        writeRegByte(SensorType.XG, LSM9DS1_REGISTER_CTRL_REG1_G, reg);

        switch (scale) {
            case LSM9DS1_GYROSCALE_245DPS:
                mGyroDpsDigit = LSM9DS1_GYRO_DPS_DIGIT_245DPS;
                break;
            case LSM9DS1_GYROSCALE_500DPS:
                mGyroDpsDigit = LSM9DS1_GYRO_DPS_DIGIT_500DPS;
                break;
            case LSM9DS1_GYROSCALE_2000DPS:
                mGyroDpsDigit = LSM9DS1_GYRO_DPS_DIGIT_2000DPS;
                break;
        }
    }

    /**
     * Read the raw accelerometer sensor values.
     * <p>
     * If you want the acceleration in SI units (m/s^2) use the {@link #getAcceleration()}
     *
     * @return an integer array containing X, Y, Z axis raw values
     * @throws IOException
     */
    public int[] getRawAccelerometerData() throws IOException {
        byte[] buffer = new byte[6];
        int[] result = new int[3];
        readRegBuffer(SensorType.XG, LSM9DS1_REGISTER_OUT_X_L_XL, buffer, buffer.length);
        result[0] = (buffer[1] << 8) | buffer[0]; // Store x-axis values
        result[1] = (buffer[3] << 8) | buffer[2]; // Store y-axis values
        result[2] = (buffer[5] << 8) | buffer[4]; // Store z-axis values
        return result;
    }

    /**
     * Get the acceleration on the X, Y, Z axis in SI units (m/s^2).
     *
     * @return a float array containing X, Y, Z axis values in SI units (m/s^2)
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
     * If you want the magnetic induction in SI units (Gs) use the {@link #getMagneticInduction()}
     *
     * @return an integer array containing X, Y, Z axis raw values
     * @throws IOException
     */
    public int[] getRawMagnetometerData() throws IOException {
        byte[] buffer = new byte[6];
        int[] result = new int[3];
        readRegBuffer(SensorType.MAG, LSM9DS1_REGISTER_OUT_X_L_M, buffer, buffer.length);
        result[0] = (buffer[1] << 8) | buffer[0]; // Store x-axis values
        result[1] = (buffer[3] << 8) | buffer[2]; // Store y-axis values
        result[2] = (buffer[5] << 8) | buffer[4]; // Store z-axis values
        return result;
    }

    /**
     * Get the magnetic induction on the X, Y, Z axis in SI units (Gs).
     *
     * @return a float array containing X, Y, Z axis values in SI units (Gs)
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
     * If you want the angular velocity in SI units (deg/s) use the {@link #getAngularVelocity()}
     *
     * @return an integer array containing X, Y, Z axis raw values
     * @throws IOException
     */
    public int[] getRawGyroscopeData() throws IOException {
        byte[] buffer = new byte[6];
        int[] result = new int[3];
        readRegBuffer(SensorType.XG, LSM9DS1_REGISTER_OUT_X_L_G, buffer, buffer.length);
        result[0] = (buffer[1] << 8) | buffer[0]; // Store x-axis values
        result[1] = (buffer[3] << 8) | buffer[2]; // Store y-axis values
        result[2] = (buffer[5] << 8) | buffer[4]; // Store z-axis values
        return result;
    }

    /**
     * Get the angular velocity on the X, Y, Z axis in SI units (deg/s).
     *
     * @return a float array containing X, Y, Z axis values in SI units (deg/s)
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
     * {@link #LSM9DS1_REGISTER_TEMP_OUT_L} and {@link #LSM9DS1_REGISTER_TEMP_OUT_H} registers together
     * express a 16-bit word in two's complement right-justified quoted at 16 LSB/⁰C.
     *
     * @return raw data temperature
     * @throws IOException
     */
    public int getRawTemperature() throws IOException {
        byte[] buffer = new byte[2];
        readRegBuffer(SensorType.XG, LSM9DS1_REGISTER_TEMP_OUT_L, buffer, buffer.length);
        return ((int) buffer[1] << 8) | buffer[0];
    }

    /**
     * Get the temperature of the sensor in degrees Celsius.
     * <p>
     * The intent of the temperature sensor is to keep track of the (gyro) die
     * temperature and compensate if necessary. It is not intended as an
     * environmental sensor.
     *
     * @return the temperature in degrees Celsius
     * @throws IOException
     */
    public float getTemperature() throws IOException {
        return getRawTemperature() / LSM9DS1_TEMP_SCALE + LSM9DS1_TEMP_BIAS;
    }

    /**
     * Get the current value used for gravity in SI units (m/s^2)
     */
    public float getGravity() {
        return mGravity;
    }

    /**
     * Set the current value used for gravity in SI units (m/s^2)
     */
    public void setGravity(float gravity) {
        mGravity = gravity;
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
        if (sensorType == SensorType.MAG) {
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
        if (sensorType == SensorType.MAG) {
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
        if (sensorType == SensorType.MAG) {
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

    // Accelerometer range
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LSM9DS1_ACCELRANGE_2G,
            LSM9DS1_ACCELRANGE_4G,
            LSM9DS1_ACCELRANGE_8G,
            LSM9DS1_ACCELRANGE_16G}
    )
    @interface AccelerometerRange {
        int LSM9DS1_ACCELRANGE_2G = 0;
        int LSM9DS1_ACCELRANGE_16G = 0b01 << 3;
        int LSM9DS1_ACCELRANGE_4G = 0b10 << 3;
        int LSM9DS1_ACCELRANGE_8G = 0b11 << 3;
    }

    // Magnetometer gain
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LSM9DS1_MAGGAIN_4GAUSS,
            LSM9DS1_MAGGAIN_8GAUSS,
            LSM9DS1_MAGGAIN_12GAUSS,
            LSM9DS1_MAGGAIN_16GAUSS}
    )
    @interface MagnetometerGain {
        int LSM9DS1_MAGGAIN_4GAUSS = 0;          // +/- 4 gauss
        int LSM9DS1_MAGGAIN_8GAUSS = 0b01 << 5;  // +/- 8 gauss
        int LSM9DS1_MAGGAIN_12GAUSS = 0b10 << 5; // +/- 12 gauss
        int LSM9DS1_MAGGAIN_16GAUSS = 0b11 << 5; // +/- 16 gauss
    }

    // Gyroscope scale
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LSM9DS1_GYROSCALE_245DPS,
            LSM9DS1_GYROSCALE_500DPS,
            LSM9DS1_GYROSCALE_2000DPS}
    )
    @interface GyroscopeScale {
        int LSM9DS1_GYROSCALE_245DPS = 0;          // +/- 245 degrees per second rotation
        int LSM9DS1_GYROSCALE_500DPS = 0b01 << 4;  // +/- 500 degrees per second rotation
        int LSM9DS1_GYROSCALE_2000DPS = 0b11 << 4; // +/- 2000 degrees per second rotation
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SensorType.MAG, SensorType.XG})
    private @interface SensorType {
        int MAG = 0;
        int XG = 1;
    }
}
