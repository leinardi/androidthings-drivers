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

package com.leinardi.android.things.sample.epaperdriverhat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.leinardi.android.things.driver.epaperdriverhat.BitmapHelper;
import com.leinardi.android.things.driver.epaperdriverhat.Gdew075t8Epd;
import com.leinardi.android.things.sample.epd.R;

import java.io.IOException;

/**
 * Activity that tests the EPD.
 */
public class EpdScreenActivity extends Activity {
    private static final String TAG = EpdScreenActivity.class.getSimpleName();
    Gdew075t8Epd mEpd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "EPD screen activity created");
        try {
            mEpd = new Gdew075t8Epd();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.monocolor_640x384);
            BitmapHelper.setBmpData(mEpd, 0, 0, bitmap, false);
            mEpd.show(); // render the pixel data
        } catch (IOException e) {
            Log.e(TAG, "Error initializing EPD", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Closing EPD");
        try {
            mEpd.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception closing EPD", e);
        } finally {
            mEpd = null;
        }
    }
}
