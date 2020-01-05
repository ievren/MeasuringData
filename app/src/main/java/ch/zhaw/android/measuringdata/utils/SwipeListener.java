/*
 * Zuercher Hochschule fuer Angewandte Wissenschaften (ZHAW)
 * School of Engineering (SoE)
 * InES Institut of Embedded Systems
 *
 *   Bachelorarbeit BA19_gruj_10
 *   Projekt FMS
 *   Darius Eckhardt (eckhadar)
 *   Ibrahim Evren   (evrenibr)
 *
 *   07.06.2019, Winterthur Switzerland
 *
 *   This Software is based on the ble_app_uart, SDK Version 15.2.0
 *   It has been modified to fit the needs of the Project FMS
 *   For correct functionality this Software has to be placed into the same folder as the SDK.
 *
 *   Copyright (c) 2015, Nordic Semiconductor
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the name of nRF UART nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *   FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package ch.zhaw.android.measuringdata.utils;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeListener extends GestureDetector.SimpleOnGestureListener {

    private static final String TAG ="SwipeListener";
    // Minimal x and y axis swipe distance.
    private static int MIN_SWIPE_DISTANCE_X = 100;
    private static int MIN_SWIPE_DISTANCE_Y = 100;

    // Maximal x and y axis swipe distance.
    private static int MAX_SWIPE_DISTANCE_X = 1000;
    private static int MAX_SWIPE_DISTANCE_Y = 1000;


    /* This method is invoked when a swipe gesture happened. */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        // Get swipe delta value in x axis.
        float deltaX = e1.getX() - e2.getX();

        // Get swipe delta value in y axis.
        float deltaY = e1.getY() - e2.getY();

        // Get absolute value.
        float deltaXAbs = Math.abs(deltaX);
        float deltaYAbs = Math.abs(deltaY);

        // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
        if((deltaXAbs >= MIN_SWIPE_DISTANCE_X) && (deltaXAbs <= MAX_SWIPE_DISTANCE_X))
        {
            if(deltaX > 0)
            {
                Log.d(TAG,"Swipe to left");

            }else
            {
                Log.d(TAG,"Swipe to right");

            }
        }

        if((deltaYAbs >= MIN_SWIPE_DISTANCE_Y) && (deltaYAbs <= MAX_SWIPE_DISTANCE_Y))
        {
            if(deltaY > 0)
            {
                Log.d(TAG,"Swipe up");
            }else
            {
                Log.d(TAG,"Swipe down");
            }
        }


        return true;
    }

    // Invoked when single tap screen.
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG,"Single tap");
        return true;
    }

    // Invoked when double tap screen.
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG,"Double tap");
        return true;
    }
}
