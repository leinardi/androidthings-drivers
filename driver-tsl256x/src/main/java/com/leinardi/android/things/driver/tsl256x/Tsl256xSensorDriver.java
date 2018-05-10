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

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.sensor.UserSensor;
import com.google.android.things.userdriver.sensor.UserSensorDriver;
import com.google.android.things.userdriver.sensor.UserSensorReading;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Tsl256xSensorDriver implements AutoCloseable {
    // DRIVER parameters
    // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
    private static final String DRIVER_VENDOR = "ams";
    private static final String DRIVER_NAME = "TSL2561";
    private static final int DRIVER_XG_MIN_DELAY_US = (int) TimeUnit.SECONDS.toMicros(1);
    private static final int DRIVER_XG_MAX_DELAY_US = (int) TimeUnit.SECONDS.toMicros(1);
    private static final float DRIVER_MAX_RANGE = Tsl256x.MAX_RANGE_LUX;
    private static final float DRIVER_POWER = Tsl256x.MAX_POWER_CONSUMPTION_UA / 1000.f;
    private static final int DRIVER_VERSION = 1;

    private Tsl256x mDevice;

    private LightUserDriver mLightUserDriver;

    /**
     * Create a new framework sensor driver connected on the given bus.
     * The driver emits {@link android.hardware.Sensor} with illuminance data when registered.
     *
     * @param bus I2C bus the sensor is connected to.
     * @throws IOException
     */
    public Tsl256xSensorDriver(String bus) throws IOException {
        this(bus, Tsl256x.I2C_ADDRESS);
    }

    /**
     * Create a new framework sensor driver connected on the given bus and address.
     * The driver emits {@link android.hardware.Sensor} with illuminance data when registered.
     *
     * @param bus        I2C bus the sensor is connected to.
     * @param i2cAddress I2C address of the Accelerometer/Gyroscope sensor.
     * @throws IOException
     */
    public Tsl256xSensorDriver(String bus, int i2cAddress) throws IOException {
        mDevice = new Tsl256x(bus, i2cAddress);
        mDevice.setIntegrationTime(Tsl256x.IntegrationTime.INTEGRATION_TIME_402MS);
        mDevice.setAutoGain(true);
    }

    /**
     * Close the driver and the underlying device.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        unregisterLightSensor();
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Register a {@link UserSensor} that pipes illuminance readings into the Android SensorManager.
     *
     * @see #unregisterLightSensor()
     */
    public void registerLightSensor() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mLightUserDriver == null) {
            mLightUserDriver = new LightUserDriver();
            UserDriverManager.getInstance().registerSensor(mLightUserDriver.getUserSensor());
        }
    }

    /**
     * Unregister the illuminance {@link UserSensor}.
     */
    public void unregisterLightSensor() {
        if (mLightUserDriver != null) {
            UserDriverManager.getInstance().unregisterSensor(mLightUserDriver.getUserSensor());
            mLightUserDriver = null;
        }
    }

    private class LightUserDriver implements UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_LIGHT)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMaxRange(DRIVER_MAX_RANGE)
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
            return new UserSensorReading(new float[]{mDevice.readLux()});
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            if (enabled) {
                mDevice.setMode(Tsl256x.Mode.MODE_ACTIVE);
            } else {
                mDevice.setMode(Tsl256x.Mode.MODE_STANDBY);
            }
        }
    }

}
