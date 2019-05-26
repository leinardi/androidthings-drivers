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

package com.leinardi.android.things.driver.lsm9ds1;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.sensor.UserSensor;
import com.google.android.things.userdriver.sensor.UserSensorDriver;
import com.google.android.things.userdriver.sensor.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

import static com.leinardi.android.things.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_952HZ;
import static com.leinardi.android.things.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_POWER_DOWN;
import static com.leinardi.android.things.driver.lsm9ds1.Lsm9ds1.MagnetometerSystemOperatingMode.MAG_CONTINUOUS_CONVERSION;
import static com.leinardi.android.things.driver.lsm9ds1.Lsm9ds1.MagnetometerSystemOperatingMode.MAG_POWER_DOWN;

public class Lsm9ds1SensorDriver implements AutoCloseable {
    // DRIVER parameters
    // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
    private static final String DRIVER_VENDOR = "STMicroelectronics";
    private static final String DRIVER_NAME = "LSM9DS1";
    private static final int DRIVER_XG_MIN_DELAY_US = Math.round(1000000.f / Lsm9ds1.MAX_FREQ_HZ_XG);
    private static final int DRIVER_XG_MAX_DELAY_US = Math.round(1000000.f / Lsm9ds1.MIN_FREQ_HZ_XG);
    private static final int DRIVER_MAG_MIN_DELAY_US = Math.round(1000000.f / Lsm9ds1.MAX_FREQ_HZ_M);
    private static final int DRIVER_MAG_MAX_DELAY_US = Math.round(1000000.f / Lsm9ds1.MIN_FREQ_HZ_M);

    private Lsm9ds1 mDevice;

    private TemperatureUserDriver mTemperatureUserDriver;
    private AccelerationUserDriver mAccelerationUserDriver;
    private AngularVelocityUserDriver mAngularVelocityUserDriver;
    private MagneticInductionUserDriver mMagneticInductionUserDriver;

    /**
     * Create a new framework sensor driver connected on the given bus.
     * The driver emits {@link android.hardware.Sensor} with acceleration, angular velocity,
     * magnetic induction and temperature data when registered.
     *
     * @param bus I2C bus the sensor is connected to.
     * @throws IOException
     */
    public Lsm9ds1SensorDriver(String bus) throws IOException {
        this(bus, Lsm9ds1.I2C_ADDRESS_ACCEL_GYRO, Lsm9ds1.I2C_ADDRESS_MAG);
    }

    /**
     * Create a new framework sensor driver connected on the given bus and address.
     * The driver emits {@link android.hardware.Sensor} with acceleration, angular velocity,
     * magnetic induction and temperature data when registered.
     *
     * @param bus              I2C bus the sensor is connected to.
     * @param addressAccelGyro I2C address of the Accelerometer/Gyroscope sensor.
     * @param addressMag       I2C address of the Magnetometer sensor.
     * @throws IOException
     */
    public Lsm9ds1SensorDriver(String bus, int addressAccelGyro, int addressMag) throws IOException {
        mDevice = new Lsm9ds1.Builder(bus)
                .setI2cAddressAccelGyro(addressAccelGyro)
                .setI2cAddressMag(addressMag)
                .build();
    }

    /**
     * Close the driver and the underlying device.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        unregisterAccelerometerSensor();
        unregisterGyroscopeSensor();
        unregisterMagneticFieldSensor();
        unregisterTemperatureSensor();
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Register a {@link UserSensor} that pipes acceleration readings into the Android SensorManager.
     *
     * @see #unregisterAccelerometerSensor()
     */
    public void registerAccelerometerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mAccelerationUserDriver == null) {
            mAccelerationUserDriver = new AccelerationUserDriver();
            UserDriverManager.getInstance().registerSensor(mAccelerationUserDriver.getUserSensor());
        }
    }

    /**
     * Unregister the acceleration {@link UserSensor}.
     */
    public void unregisterAccelerometerSensor() {
        if (mAccelerationUserDriver != null) {
            UserDriverManager.getInstance().unregisterSensor(mAccelerationUserDriver.getUserSensor());
            mAccelerationUserDriver = null;
        }
    }

    /**
     * Register a {@link UserSensor} that pipes angular velocity readings into the Android SensorManager.
     *
     * @see #unregisterGyroscopeSensor()
     */
    public void registerGyroscopeSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mAngularVelocityUserDriver == null) {
            mAngularVelocityUserDriver = new AngularVelocityUserDriver();
            UserDriverManager.getInstance().registerSensor(mAngularVelocityUserDriver.getUserSensor());
        }
    }

    /**
     * Unregister the angular velocity {@link UserSensor}.
     */
    public void unregisterGyroscopeSensor() {
        if (mAngularVelocityUserDriver != null) {
            UserDriverManager.getInstance().unregisterSensor(mAngularVelocityUserDriver.getUserSensor());
            mAngularVelocityUserDriver = null;
        }
    }

    /**
     * Register a {@link UserSensor} that pipes magnetic induction readings into the Android SensorManager.
     *
     * @see #unregisterMagneticFieldSensor()
     */
    public void registerMagneticFieldSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mMagneticInductionUserDriver == null) {
            mMagneticInductionUserDriver = new MagneticInductionUserDriver();
            UserDriverManager.getInstance().registerSensor(mMagneticInductionUserDriver.getUserSensor());
        }
    }

    /**
     * Unregister the magnetic induction {@link UserSensor}.
     */
    public void unregisterMagneticFieldSensor() {
        if (mMagneticInductionUserDriver != null) {
            UserDriverManager.getInstance().unregisterSensor(mMagneticInductionUserDriver.getUserSensor());
            mMagneticInductionUserDriver = null;
        }
    }

    /**
     * Register a {@link UserSensor} that pipes temperature readings into the Android SensorManager.
     *
     * @see #unregisterTemperatureSensor()
     */
    public void registerTemperatureSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mTemperatureUserDriver == null) {
            mTemperatureUserDriver = new TemperatureUserDriver();
            UserDriverManager.getInstance().registerSensor(mTemperatureUserDriver.getUserSensor());
        }
    }

    /**
     * Unregister the temperature {@link UserSensor}.
     */
    public void unregisterTemperatureSensor() {
        if (mTemperatureUserDriver != null) {
            UserDriverManager.getInstance().unregisterSensor(mTemperatureUserDriver.getUserSensor());
            mTemperatureUserDriver = null;
        }
    }

    /*
     * You cannot have Gyro on and Accel off but you can have Accel on and Gyro off.
     * Temp requires Accel on.
     * Check 3.1 Operating modes in the datasheet (page 19).
     */
    private void maybeSleep() throws IOException {
        if (mAccelerationUserDriver == null || !mAccelerationUserDriver.isEnabled()) {              // Turn off Accel
            if ((mAngularVelocityUserDriver == null || !mAngularVelocityUserDriver.isEnabled())     // if Gyro is off
                    && (mTemperatureUserDriver == null || !mTemperatureUserDriver.isEnabled())) {   // and Temp is off
                mDevice.setAccelerometerOdr(ODR_POWER_DOWN);
            }
        } else {                                                                                    // Turn on Accel
            if ((mAngularVelocityUserDriver == null || !mAngularVelocityUserDriver.isEnabled())     // if Gyro is off
                    && (mTemperatureUserDriver == null || !mTemperatureUserDriver.isEnabled())) {   // and Temp is off
                mDevice.setAccelerometerOdr(ODR_952HZ);
            }
        }

        if (mAngularVelocityUserDriver == null || !mAngularVelocityUserDriver.isEnabled()) {        // Turn off Gyro
            mDevice.setGyroscopeOdr(ODR_POWER_DOWN);
            if ((mAccelerationUserDriver != null && mAccelerationUserDriver.isEnabled())            // if Accel is on
                    || (mTemperatureUserDriver != null && mTemperatureUserDriver.isEnabled())) {    // or Temp is on
                mDevice.setAccelerometerOdr(ODR_952HZ);
            }
        } else {                                                                                    // Turn on Gyro
            mDevice.setGyroscopeOdr(ODR_952HZ);
        }

        if (mTemperatureUserDriver == null || !mTemperatureUserDriver.isEnabled()) {                // Turn off Temp
            if ((mAngularVelocityUserDriver == null || !mAngularVelocityUserDriver.isEnabled())     // if Gyro is off
                    && (mAccelerationUserDriver == null || !mAccelerationUserDriver.isEnabled())) { // and Accel is off
                mDevice.setAccelerometerOdr(ODR_POWER_DOWN);
            }
        } else {                                                                                    // Turn on Temp
            if ((mAngularVelocityUserDriver == null || !mAngularVelocityUserDriver.isEnabled())     // if Gyro is off
                    && (mTemperatureUserDriver == null || !mTemperatureUserDriver.isEnabled())) {   // and Accel is off
                mDevice.setAccelerometerOdr(ODR_952HZ);
            }
        }

        if (mMagneticInductionUserDriver == null || !mMagneticInductionUserDriver.isEnabled()) {
            mDevice.setMagnetometerSystemOperatingMode(MAG_POWER_DOWN);
        } else {
            mDevice.setMagnetometerSystemOperatingMode(MAG_CONTINUOUS_CONVERSION);
        }
    }

    private class AccelerationUserDriver implements UserSensorDriver {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        private static final float DRIVER_MAX_RANGE = Lsm9ds1.MAX_ACCEL_RANGE_G_DEFAULT;
        private static final float DRIVER_POWER = Lsm9ds1.MAX_POWER_CONSUMPTION_X_UA / 1000.f;
        private static final int DRIVER_VERSION = 1;

        private boolean mEnabled;
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_ACCELEROMETER)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMaxRange(DRIVER_MAX_RANGE)
                        .setResolution(mDevice.getAccelerationSensitivity())
                        .setPower(DRIVER_POWER)
                        .setMinDelay(DRIVER_XG_MIN_DELAY_US)
                        .setMaxDelay(DRIVER_XG_MAX_DELAY_US)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(mDevice.readAcceleration());
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
            maybeSleep();
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }

    private class AngularVelocityUserDriver implements UserSensorDriver {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        private static final float DRIVER_MAX_RANGE = Lsm9ds1.MAX_GYRO_RATE_DPS_DEFAULT;
        private static final float DRIVER_POWER = Lsm9ds1.MAX_POWER_CONSUMPTION_G_UA / 1000.f;
        private static final int DRIVER_VERSION = 1;

        private boolean mEnabled;
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_GYROSCOPE)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMaxRange(DRIVER_MAX_RANGE)
                        .setResolution(mDevice.getAngularVelocitySensitivity())
                        .setPower(DRIVER_POWER)
                        .setMinDelay(DRIVER_XG_MIN_DELAY_US)
                        .setMaxDelay(DRIVER_XG_MAX_DELAY_US)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(mDevice.readAngularVelocity());
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
            maybeSleep();
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }

    private class MagneticInductionUserDriver implements UserSensorDriver {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        private static final float DRIVER_MAX_RANGE = Lsm9ds1.MAX_MAG_GAIN_GS_DEFAULT;
        private static final float DRIVER_POWER = Lsm9ds1.MAX_POWER_CONSUMPTION_M_UA / 1000.f;
        private static final int DRIVER_VERSION = 1;

        private boolean mEnabled;
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_MAGNETIC_FIELD)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMaxRange(DRIVER_MAX_RANGE)
                        .setResolution(mDevice.getMagneticInductionSensitivity())
                        .setPower(DRIVER_POWER)
                        .setMinDelay(DRIVER_MAG_MIN_DELAY_US)
                        .setMaxDelay(DRIVER_MAG_MAX_DELAY_US)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(mDevice.readMagneticInduction());
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
            maybeSleep();
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }

    private class TemperatureUserDriver implements UserSensorDriver {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        private static final float DRIVER_MAX_RANGE = Lsm9ds1.MAX_TEMP_C;
        // The temperature is provided by the Accel
        private static final float DRIVER_POWER = Lsm9ds1.MAX_POWER_CONSUMPTION_X_UA / 1000.f;
        private static final int DRIVER_VERSION = 1;

        private boolean mEnabled;
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_AMBIENT_TEMPERATURE)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMaxRange(DRIVER_MAX_RANGE)
                        .setResolution(mDevice.getTemperatureSensitivity())
                        .setPower(DRIVER_POWER)
                        .setMinDelay(DRIVER_XG_MIN_DELAY_US)
                        .setMaxDelay(DRIVER_XG_MAX_DELAY_US)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(new float[]{mDevice.readTemperature()});
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
            maybeSleep();
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }
}
