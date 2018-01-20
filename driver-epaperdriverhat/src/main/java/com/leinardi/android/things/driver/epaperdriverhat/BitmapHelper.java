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

package com.leinardi.android.things.driver.epaperdriverhat;

import android.graphics.Bitmap;

public class BitmapHelper {
    private static final int GRADIENT_CUTOFF = 85; // Tune for gradient picker on grayscale images.

    private BitmapHelper() {
    }

    /**
     * Converts a bitmap image to LCD screen data and sets it on the given screen at the specified
     * offset.
     *
     * @param mScreen   The e-paper screen to write the bitmap data to.
     * @param xOffset   The horizontal offset to draw the image at.
     * @param yOffset   The vertical offset to draw the image at.
     * @param bmp       The bitmap image that you want to convert to screen data.
     * @param drawWhite true for drawing only white pixels, false for drawing grayscale pixel
     *                  based on {@link #GRADIENT_CUTOFF}.
     */
    public static void setBmpData(Epd mScreen, int xOffset, int yOffset, Bitmap bmp, boolean drawWhite) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bmp.getPixel(x, y);
                if (!drawWhite) { // Look at Alpha channel instead
                    if ((pixel & 0xFF) <= GRADIENT_CUTOFF) {
                        mScreen.setPixel(x + xOffset, y + yOffset, true);
                    } else {
                        mScreen.setPixel(x + xOffset, y + yOffset, false);
                    }
                } else {
                    if (pixel == -1) { // Only draw white pixels
                        mScreen.setPixel(x + xOffset, y + yOffset, true);
                    } else {
                        mScreen.setPixel(x + xOffset, y + yOffset, false);
                    }
                }
            }
        }
    }
}
