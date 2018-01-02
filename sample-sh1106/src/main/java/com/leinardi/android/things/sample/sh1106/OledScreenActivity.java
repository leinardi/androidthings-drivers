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

package com.leinardi.android.things.sample.sh1106;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.leinardi.android.things.driver.sh1106.BitmapHelper;
import com.leinardi.android.things.driver.sh1106.Sh1106;

import java.io.IOException;

/**
 * Activity that tests the Sh1106 display.
 */
public class OledScreenActivity extends Activity {
    private static final String TAG = OledScreenActivity.class.getSimpleName();
    private static final int FPS = 30; // Frames per second on draw thread
    private static final int BITMAP_FRAMES_PER_MOVE = 4; // Frames to show bitmap before moving it

    private boolean mExpandingPixels = true;
    private int mDotMod = 1;
    private int mBitmapMod = 0;
    private int mTick = 0;
    private Modes mMode = Modes.BITMAP;
    private Sh1106 mScreen;

    private Handler mHandler = new Handler();
    private Bitmap mBitmap;

    enum Modes {
        CROSSHAIRS,
        DOTS,
        BITMAP
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mScreen = new Sh1106(BoardDefaults.getI2CPort());
        } catch (IOException e) {
            Log.e(TAG, "Error while opening screen", e);
            throw new RuntimeException(e);
        }
        Log.d(TAG, "OLED screen activity created");
        mHandler.post(mDrawRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // remove pending runnable from the handler
        mHandler.removeCallbacks(mDrawRunnable);
        // Close the device.
        try {
            mScreen.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing SH1106", e);
        } finally {
            mScreen = null;
        }
    }

    /**
     * Draws crosshair pattern.
     */
    private void drawCrosshairs() {
        mScreen.clearPixels();
        int y = mTick % mScreen.getLcdHeight();
        for (int x = 0; x < mScreen.getLcdWidth(); x++) {
            mScreen.setPixel(x, y, true);
            mScreen.setPixel(x, mScreen.getLcdHeight() - (y + 1), true);
        }
        int x = mTick % mScreen.getLcdWidth();
        for (y = 0; y < mScreen.getLcdHeight(); y++) {
            mScreen.setPixel(x, y, true);
            mScreen.setPixel(mScreen.getLcdWidth() - (x + 1), y, true);
        }
    }

    /**
     * Draws expanding and contracting pixels.
     */
    private void drawExpandingDots() {
        if (mExpandingPixels) {
            for (int x = 0; x < mScreen.getLcdWidth(); x++) {
                for (int y = 0; y < mScreen.getLcdHeight() && mMode == Modes.DOTS; y++) {
                    mScreen.setPixel(x, y, (x % mDotMod) == 1 && (y % mDotMod) == 1);
                }
            }
            mDotMod++;
            if (mDotMod > mScreen.getLcdHeight()) {
                mExpandingPixels = false;
                mDotMod = mScreen.getLcdHeight();
            }
        } else {
            for (int x = 0; x < mScreen.getLcdWidth(); x++) {
                for (int y = 0; y < mScreen.getLcdHeight() && mMode == Modes.DOTS; y++) {
                    mScreen.setPixel(x, y, (x % mDotMod) == 1 && (y % mDotMod) == 1);
                }
            }
            mDotMod--;
            if (mDotMod < 1) {
                mExpandingPixels = true;
                mDotMod = 1;
            }
        }
    }

    /**
     * Draws a BMP in one of three positions.
     */
    private void drawMovingBitmap() {
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower);
        }
        // Move the bmp every few ticks
        if (mTick % BITMAP_FRAMES_PER_MOVE == 0) {
            mScreen.clearPixels();
            // Move the bitmap back and forth based on mBitmapMod:
            // 0 - left aligned
            // 1 - centered
            // 2 - right aligned
            // 3 - centered
            int diff = mScreen.getLcdWidth() - mBitmap.getWidth();
            int mult = mBitmapMod == 3 ? 1 : mBitmapMod; // 0, 1, or 2
            int offset = mult * (diff / 2);
            BitmapHelper.setBmpData(mScreen, offset, 0, mBitmap, false);
            mBitmapMod = (mBitmapMod + 1) % 4;
        }
    }

    private Runnable mDrawRunnable = new Runnable() {
        /**
         * Updates the display and tick counter.
         */
        @Override
        public void run() {
            // exit Runnable if the device is already closed
            if (mScreen == null) {
                return;
            }
            mTick++;
            try {
                switch (mMode) {
                    case DOTS:
                        drawExpandingDots();
                        break;
                    case BITMAP:
                        drawMovingBitmap();
                        break;
                    default:
                        drawCrosshairs();
                        break;
                }
                mScreen.show();
                mHandler.postDelayed(this, 1000 / FPS);
            } catch (IOException e) {
                Log.e(TAG, "Exception during screen update", e);
            }
        }
    };
}
