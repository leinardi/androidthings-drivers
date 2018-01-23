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

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;

public class Tsl256xSensorDriver implements AutoCloseable {
    // DRIVER parameters
    // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
    //    private static final String DRIVER_VENDOR = "STMicroelectronics";
    //    private static final String DRIVER_NAME = "TSL2561";
    //    private static final int DRIVER_XG_MIN_DELAY_US = Math.round(1000000.f / Tsl256x.MAX_FREQ_HZ_XG);
    //    private static final int DRIVER_XG_MAX_DELAY_US = Math.round(1000000.f / Tsl256x.MIN_FREQ_HZ_XG);
    //    private static final int DRIVER_MAG_MIN_DELAY_US = Math.round(1000000.f / Tsl256x.MAX_FREQ_HZ_M);
    //    private static final int DRIVER_MAG_MAX_DELAY_US = Math.round(1000000.f / Tsl256x.MIN_FREQ_HZ_M);
    //
    private Tsl256x mDevice;

    private LightUserDriver mLightUserDriver;
    //
    //    /**
    //     * Create a new framework sensor driver connected on the given bus.
    //     * The driver emits {@link android.hardware.Sensor} with light, angular velocity,
    //     * magnetic induction and temperature data when registered.
    //     *
    //     * @param bus I2C bus the sensor is connected to.
    //     * @throws IOException
    //     */
    //    public Tsl256xSensorDriver(String bus) throws IOException {
    //        this(bus, Tsl256x.I2C_ADDRESS_ACCEL_GYRO, Tsl256x.I2C_ADDRESS_MAG);
    //    }
    //
    //    /**
    //     * Create a new framework sensor driver connected on the given bus and address.
    //     * The driver emits {@link android.hardware.Sensor} with light, angular velocity,
    //     * magnetic induction and temperature data when registered.
    //     *
    //     * @param bus              I2C bus the sensor is connected to.
    //     * @param addressAccelGyro I2C address of the Accelerometer/Gyroscope sensor.
    //     * @param addressMag       I2C address of the Magnetometer sensor.
    //     * @throws IOException
    //     */
    //    public Tsl256xSensorDriver(String bus, int addressAccelGyro, int addressMag) throws IOException {
    //        mDevice = new Tsl256x.Builder(bus)
    //                .setI2cAddressAccelGyro(addressAccelGyro)
    //                .setI2cAddressMag(addressMag)
    //                .build();
    //    }

    /**
     * Close the driver and the underlying device.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        unregisterAccelerometerSensor();
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Register a {@link UserSensor} that pipes light readings into the Android SensorManager.
     *
     * @see #unregisterAccelerometerSensor()
     */
    public void registerAccelerometerSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mLightUserDriver == null) {
            mLightUserDriver = new LightUserDriver();
            UserDriverManager.getManager().registerSensor(mLightUserDriver.getUserSensor());
        }
    }

    /**
     * Unregister the light {@link UserSensor}.
     */
    public void unregisterAccelerometerSensor() {
        if (mLightUserDriver != null) {
            UserDriverManager.getManager().unregisterSensor(mLightUserDriver.getUserSensor());
            mLightUserDriver = null;
        }
    }

    private class LightUserDriver extends UserSensorDriver {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        //        private static final float DRIVER_MAX_RANGE = Tsl256x.MAX_ACCEL_RANGE_G_DEFAULT;
        //        private static final float DRIVER_POWER = Tsl256x.MAX_POWER_CONSUMPTION_X_UA / 1000.f;
        //        private static final int DRIVER_VERSION = 1;
        //        private static final String DRIVER_REQUIRED_PERMISSION = "";

        private boolean mEnabled;
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            //            if (mUserSensor == null) {
            //                mUserSensor = new UserSensor.Builder()
            //                        .setType(Sensor.TYPE_ACCELEROMETER)
            //                        .setName(DRIVER_NAME)
            //                        .setVendor(DRIVER_VENDOR)
            //                        .setVersion(DRIVER_VERSION)
            //                        .setMaxRange(DRIVER_MAX_RANGE)
            //                        .setResolution(mDevice.getLightSensitivity())
            //                        .setPower(DRIVER_POWER)
            //                        .setMinDelay(DRIVER_XG_MIN_DELAY_US)
            //                        .setMaxDelay(DRIVER_XG_MAX_DELAY_US)
            //                        .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
            //                        .setUuid(UUID.randomUUID())
            //                        .setDriver(this)
            //                        .build();
            //            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return null; //new UserSensorReading(mDevice.readLight());
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }

}
