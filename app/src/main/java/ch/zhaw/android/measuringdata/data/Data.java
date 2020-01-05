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

package ch.zhaw.android.measuringdata.data;

import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import ch.zhaw.android.measuringdata.utils.FileWriteHandle;

public class Data {

    final static String TAG="Data";
    final static int TOTALPACKAGES=4;
    final static float FREQUENCE_MS = 0.003f; //333Hz Abtastrate


    int count=1;
    //
    int[][] rxData;
    ArrayList<Entry> dataList=new ArrayList();

    public boolean isReady(){
        return true;
    }


    /**
     * data is a 2D array and contains [packageNR] [rxValue]
     * @param data
     */
    @NonNull
    public void setData(byte[][] data) {
        Log.d(TAG, "Data col:"+data[0].length+" row:"+data.length);
        int packagenrs = data.length-1;
        String log="";
        //int rxDataLen=(data.length-1)/2;
        int rxDataLen=((data[0].length)/2);
        rxData=new int[TOTALPACKAGES][rxDataLen];

        //TODO first byte is package Number "NOT Implemented"
        //for (int j = 1; j < data.length; j+=2) {
        int z=0;
        int a=0;
        int b=0;
        for (int i = 0; i <= packagenrs; i++) {
            for (int j = 0; j < data[0].length; j += 2) {
                if(data[i][j]<0){
                    a=(data[i][j] + 256)<<8 ;
                }
                else {
                    a=(data[i][j]<<8);
                }
                if(data[i][j+1]<0){
                    b=(data[i][j+1] + 256);
                }
                else {
                    b=(data[i][j+1]);
                }
                int c=(a+b);
                z++;
                rxData[i][(j) / 2] = c;
                //Log.d(TAG,"c(z):"+c+"("+z+")");
                //log += String.format("[%d, %d]=%d ", i, ((j) / 2),  rxData[(i)][(j) / 2]); // rxdata & 0xFFFF -> unsigned
            }
            //Log.d(TAG, "data:" + log);
            //log = "";
        }




    }

    public static short byte2short(byte[] data)
    {
        short i = (short) ((data[0] << 8) | (data[1]));
        return i;
    }

    public ArrayList<Entry> getLastData() {
        String log="";
        float[] meanAverageData= new float[rxData[0].length* TOTALPACKAGES];
        // Manipulate arriving data
        ArrayList<Entry> list = new ArrayList<>();
        int a= 1;
        for (int i = 0; i < TOTALPACKAGES ; i++) {
            for (int j = 0; j < rxData[i].length; j++) {
                meanAverageData[a-1] =rxData[i][j];
                //list.add(new Entry(((a * FREQUENCE_MS)), (rxData[i][j])) ); //count * rxData[i][j]));
                //log += String.format("[%f, %f], ", (float) ((a * FREQUENCE_MS)),  (int)(rxData[i][j]) ); // rxdata & 0xFFFF -> unsigned
                a++;

            }
        }
        a=0;
        //Log.d(TAG, "plot:" + log);

        //******Moving-Average Filter*****
        // #list is our input data
        //int period = (int) (0.1/FREQUENCE_MS);
        int period = 15;
        MeanAverageFilter obj = new MeanAverageFilter(period);
        for (int i = 0; i< meanAverageData.length; i++) {
            obj.addData(meanAverageData[i]);
            double meanRounded = (double) Math.round(obj.getMean() * 100) / 100;
            //System.out.println("New number added is " + meanAverageData[i] + ", SMA = " + meanRounded);
            list.add(new Entry(((i * FREQUENCE_MS)), (float) (meanRounded))); //count * rxData[i][j]));
        }

        return list;
    }

    public ArrayList<Entry> getEmptyList(){
        ArrayList<Entry> dataVals = new ArrayList();
        dataVals.add(new Entry(0,0));
        return dataVals;

    }



    //FIXME ADDING EXPORT-FUNCTION
    //Save chart data
    public void exportData(ArrayList<Entry> lastData, String dir, String fileName, String device) {
        Log.d(TAG,"export called:"+lastData.get(1));
        File root = android.os.Environment.getExternalStorageDirectory();
        Log.d(TAG,"\nExternal file system root: "+root);
        FileWriteHandle fileWriteHandle = new FileWriteHandle();
        fileWriteHandle.open(dir,fileName, false);
        String seperator = ";";
        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        fileWriteHandle.writeFile("Device:"+seperator+device+"\n");
        fileWriteHandle.writeFile("Measuring Data from:"+seperator+currentDateTimeString+"\n");
        fileWriteHandle.writeFile("Time[s]"+seperator+"Force[N]\n");
        for (int n = 0; n < lastData.size(); n++) {
            String line = ""+String.format("%.3f",lastData.get(n).getX())+seperator+lastData.get(n).getY()+"\n";
            fileWriteHandle.writeFile(line);
        }
        fileWriteHandle.close();

    }


    public ArrayList<Entry> getTestData(){
        ArrayList<Entry> dataVals = new ArrayList();
        dataVals.add(new Entry(0,20*count));
        dataVals.add(new Entry(50,40*count));
        dataVals.add(new Entry(100,50*count));
        dataVals.add(new Entry(150,60*count));
        dataVals.add(new Entry(200,70*count));
        dataVals.add(new Entry(250,70*count));
        dataVals.add(new Entry(300,75*count));
        dataVals.add(new Entry(350,70*count));
        dataVals.add(new Entry(400,70*count));
        dataVals.add(new Entry(450,74*count));
        dataVals.add(new Entry(500,65*count));
        dataVals.add(new Entry(550,220*count));
        dataVals.add(new Entry(600,350*count));
        dataVals.add(new Entry(650,280*count));
        dataVals.add(new Entry(700,80*count));
        dataVals.add(new Entry(750,0*count));
        dataVals.add(new Entry(800,0*count));
        dataVals.add(new Entry(850,0*count));
        dataVals.add(new Entry(900,0*count));
        dataVals.add(new Entry(950,0*count));
        dataVals.add(new Entry(1000,0*count));
        dataVals.add(new Entry(1050,0*count));

        return dataVals;

    }



}
