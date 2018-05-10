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

package com.leinardi.android.things.driver.hcsr04;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Driver for the HC-SR04 ultrasonic ranging module.
 */
@SuppressWarnings("WeakerAccess")
public class Hcsr04 implements Closeable {
    public static final int MIN_RANGE = 2;
    public static final int MAX_RANGE = 400;
    public static final float ACCURACY = 0.3f; // 3 mm
    public static final int MEASUREMENT_INTERVAL_MS = 500;
    public static final int NO_DATA = -1;
    static final float MAX_POWER_CONSUMPTION_UA = 3240f;
    private static final String TAG = Hcsr04.class.getSimpleName();
    private static final float MAGIC_NUMBER_FROM_DATASHEET = 58.23f;
    private static final int MAX_ECHO_WAIT_NS =
            (int) (MAX_RANGE * TimeUnit.MICROSECONDS.toNanos(1) * MAGIC_NUMBER_FROM_DATASHEET);
    private static final int TRIG_PULSE_DURATION_IN_US = 10;
    private Gpio mTrigGpio;
    private Gpio mEchoGpio;
    private final Hcsr04HandlerThread mHandlerThread;
    private float mDistance;

    /**
     * Create a new HC-SR04 ultrasonic ranging module driver.
     *
     * @param trigPin The Gpio name for the trigger pin
     * @param echoPin The Gpio name for the echo pin
     * @throws IOException
     */
    public Hcsr04(String trigPin, String echoPin) throws IOException {
        PeripheralManager pioService = PeripheralManager.getInstance();
        Gpio trigGpio = pioService.openGpio(trigPin);
        Gpio echoGpio = pioService.openGpio(echoPin);
        try {
            connect(trigGpio, echoGpio);
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }
        mHandlerThread = new Hcsr04HandlerThread();
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

    private void measureDistance() {
        long startTime, endTime;
        mDistance = NO_DATA;
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
            // SUPPRESS CHECKSTYLE EmptyBlock
            do {
            } while (!mEchoGpio.getValue() && (System.nanoTime() - startTime < 1_000_000));

            startTime = System.nanoTime();
            // Wait for the end of the pulse on the ECHO pin
            // SUPPRESS CHECKSTYLE EmptyBlock
            do {
            } while (mEchoGpio.getValue() && (System.nanoTime() - startTime < MAX_ECHO_WAIT_NS));
            endTime = System.nanoTime();

            // Measure how long the echo pin was held high (pulse width)
            long echoDuration = endTime - startTime;

            // Calculate distance in centimeters. The constants
            // are coming from the datasheet, and calculated from the assumed speed
            // of sound in air at sea level (~340 m/s).
            float distance = TimeUnit.NANOSECONDS.toMicros(echoDuration) / MAGIC_NUMBER_FROM_DATASHEET; //cm

            if (distance > MIN_RANGE && distance < MAX_RANGE) {
                mDistance = distance;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.d(TAG, "Hcsr04 thread interrupted");
            return;
        } catch (IOException e) {
            Log.e(TAG, "GPIO error", e);
        }
    }

    /**
     * Get the distance in centimeters.
     * <p>
     * NOTE: The measurement of the distance can take up to 25 ms.
     *
     * @return a float containing the distance in cm.
     */
    public float readDistance() {
        synchronized (mHandlerThread) {
            mHandlerThread.measure();
        }
        return mDistance;
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() {
        mHandlerThread.quit();
        if (mEchoGpio != null) {
            try {
                mEchoGpio.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO device", e);
            } finally {
                mEchoGpio = null;
            }
        }

        if (mTrigGpio != null) {
            try {
                mTrigGpio.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO device", e);
            } finally {
                mTrigGpio = null;
            }
        }
    }

    private void busyWaitMicros(long micros) {
        long waitUntil = System.nanoTime() + (micros * 1000);
        while (waitUntil > System.nanoTime()) {
            System.nanoTime();
        }
    }

    private class Hcsr04HandlerThread extends HandlerThread {
        private final Handler mHandler;

        Hcsr04HandlerThread() {
            super("hcsr04-handler-thread", Thread.NORM_PRIORITY + 1);
            start();
            mHandler = new Handler(getLooper());
        }

        private synchronized void notifyMeasurementDone() {
            notify();
        }

        void measure() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    measureDistance();
                    notifyMeasurementDone();
                }
            });
            try {
                wait();
            } catch (InterruptedException e) {
                Log.w(TAG, "wait was interrupted");
            }
        }
    }
}
