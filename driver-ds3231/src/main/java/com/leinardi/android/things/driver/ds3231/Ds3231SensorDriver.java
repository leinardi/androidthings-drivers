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

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.sensor.UserSensor;
import com.google.android.things.userdriver.sensor.UserSensorDriver;
import com.google.android.things.userdriver.sensor.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class Ds3231SensorDriver implements AutoCloseable {
    // DRIVER parameters
    // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
    private static final String DRIVER_VENDOR = "Maxim Integrated Products";
    private static final String DRIVER_NAME = "DS3231";
    private static final int DRIVER_MIN_DELAY_US = Ds3231.TEMP_MEASUREMENT_INTERVAL_MS * 1000;
    private static final int DRIVER_MAX_DELAY_US = Ds3231.TEMP_MEASUREMENT_INTERVAL_MS * 1000;
    private static final float DRIVER_MAX_RANGE = Ds3231.MAX_TEMP_RANGE;
    private static final float DRIVER_POWER = Ds3231.MAX_POWER_CONSUMPTION_UA / 1000.f;
    private static final float DRIVER_RESOLUTION = Ds3231.TEMPERATURE_RESOLUTION;
    private static final int DRIVER_VERSION = 1;

    private Ds3231 mDevice;

    private TemperatureUserDriver mTemperatureUserDriver;

    /**
     * Create a new framework sensor driver connected on the given bus and address.
     * The driver emits {@link Sensor} with temperature data when registered.
     *
     * @param bus I2C bus the sensor is connected to.
     * @throws IOException
     */
    public Ds3231SensorDriver(String bus) throws IOException {
        this(bus, Ds3231.I2C_ADDRESS);
    }

    /**
     * Create a new framework sensor driver connected on the given bus and address.
     * The driver emits {@link Sensor} with temperature data when registered.
     *
     * @param bus        I2C bus the sensor is connected to.
     * @param i2cAddress I2C address of the RTC
     * @throws IOException
     */
    public Ds3231SensorDriver(String bus, int i2cAddress) throws IOException {
        mDevice = new Ds3231(bus, i2cAddress);
    }

    /**
     * Close the driver and the underlying device.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
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

    private class TemperatureUserDriver implements UserSensorDriver {
        private UserSensor mUserSensor;

        private UserSensor getUserSensor() {
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_AMBIENT_TEMPERATURE)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMaxRange(DRIVER_MAX_RANGE)
                        .setResolution(DRIVER_RESOLUTION)
                        .setPower(DRIVER_POWER)
                        .setMinDelay(DRIVER_MIN_DELAY_US)
                        .setMaxDelay(DRIVER_MAX_DELAY_US)
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

    }
}
