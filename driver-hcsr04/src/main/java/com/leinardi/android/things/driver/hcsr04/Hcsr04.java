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

package com.leinardi.android.things.driver.hcsr04;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Driver for the HC-SR04 ultrasonic ranging module.
 */
public class Hcsr04 implements Closeable {
    static final float MAX_POWER_CONSUMPTION_UA = 3240f;
    public static final int MIN_RANGE = 2;
    public static final int MAX_RANGE = 400;
    public static final float ACCURACY = 0.3f; // 3 mm
    private static final String TAG = Hcsr04.class.getSimpleName();
    private static final int TRIG_PULSE_DURATION_IN_US = 10;
    public static final int MEASUREMENT_INTERVAL_MS = 500;
    private Gpio mTrigGpio;
    private Gpio mEchoGpio;
    private boolean mEnabled;
    private Thread mThread;
    private float mDistance;

    /**
     * @param trigPin
     * @param echoPin
     * @throws IOException
     */
    public Hcsr04(String trigPin, String echoPin) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        Gpio trigGpio = pioService.openGpio(trigPin);
        Gpio echoGpio = pioService.openGpio(echoPin);
        try {
            connect(trigGpio, echoGpio);
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }
    }

    private void connect(Gpio trigGpio, Gpio echoGpio) throws IOException {
        mTrigGpio = trigGpio;
        mEchoGpio = echoGpio;
        mTrigGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mEchoGpio.setDirection(Gpio.DIRECTION_IN);

        mTrigGpio.setActiveType(Gpio.ACTIVE_HIGH);
        mEchoGpio.setActiveType(Gpio.ACTIVE_HIGH);
        mEchoGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);

    }

    /**
     * @param enabled
     * @throws IOException
     */
    public void setEnabled(boolean enabled) throws IOException {
        if (mEchoGpio == null || mTrigGpio == null) {
            throw new IllegalStateException("GPIO Device not open");
        }
        if (mEnabled != enabled) {
            mEnabled = enabled;
            if (enabled) {
                startNewThread();
            } else {
                stopThread();
            }
        }
    }

    private void startNewThread() throws IOException {
        stopThread();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime, endTime;
                while (!Thread.currentThread().isInterrupted()) {
                    mDistance = -1;
                    try {
                        // Just to be sure, set the trigger first to false
                        mTrigGpio.setValue(false);
                        Thread.sleep(1);

                        // Hold the trigger pin HIGH for at least 10 us
                        mTrigGpio.setValue(true);
                        // Thread.sleep() takes minimum ~100.000 ns to be executed on RPi3, even if you set only 10 ns
                        busyWaitMicros(TRIG_PULSE_DURATION_IN_US);

                        // Reset the trigger pin
                        mTrigGpio.setValue(false);

                        // Wait for pulse on echo pin
                        startTime = System.nanoTime();
                        do {
                            Thread.sleep(0, 10_000); // ~120.000 ns on RPi3
                        } while (!mEchoGpio.getValue() && (System.nanoTime() - startTime < 1_000_000));

                        startTime = System.nanoTime();
                        // Wait for the end of the pulse on the ECHO pin
                        do {
                            Thread.sleep(0, 10_000); // ~120.000 ns on RPi3
                        } while (mEchoGpio.getValue());
                        endTime = System.nanoTime();

                        // Measure how long the echo pin was held high (pulse width)
                        long echoDuration = endTime - startTime;

                        // Calculate distance in centimeters. The constants
                        // are coming from the datasheet, and calculated from the assumed speed
                        // of sound in air at sea level (~340 m/s).
                        float distance = TimeUnit.NANOSECONDS.toMicros(echoDuration) / 58.23f; //cm

                        if (distance > MIN_RANGE && distance < MAX_RANGE) {
                            mDistance = distance;
                        }
                        Thread.sleep(MEASUREMENT_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Log.d(TAG, "Hcsr04 thread interrupted");
                        return;
                    } catch (IOException e) {
                        Log.e(TAG, "GPIO error", e);
                        try {
                            stopThread();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        });
        mThread.setPriority(Thread.NORM_PRIORITY + 1);
        mThread.start();
    }

    private void stopThread() throws IOException {
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }
        mTrigGpio.setValue(false);
    }

    /**
     * @return
     */
    public float readDistance() {
        return mDistance;
    }

    /**
     * Close the driver and the underlying device.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if (mEchoGpio != null) {
            try {
                mEchoGpio.close();
            } finally {
                mEchoGpio = null;
            }
        }

        if (mTrigGpio != null) {
            try {
                mTrigGpio.close();
            } finally {
                mTrigGpio = null;
            }
        }
    }

    private static void busyWaitMicros(long micros) {
        long waitUntil = System.nanoTime() + (micros * 1000);
        while (waitUntil > System.nanoTime()) {
            System.nanoTime();
        }
    }
}
