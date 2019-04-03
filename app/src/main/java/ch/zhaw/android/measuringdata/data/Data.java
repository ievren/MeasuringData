package ch.zhaw.android.measuringdata.data;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class Data {

    final static String TAG="Data";
    final static int TOTALPACKAGES=3;

    float count=1;
    //
    short[][] rxData;
    ArrayList<Entry> dataList=new ArrayList();

    public boolean isReady(){
        return true;
    }

    public void startRead(){

    }

    public void setData(byte[] data, int packageNr) {
        String log="";
        //int rxDataLen=(data.length-1)/2;
        int rxDataLen=(data.length)/2;
        rxData=new short[TOTALPACKAGES][rxDataLen];

        //first byte is package no "NOT Implemented"
        //for (int j = 1; j < data.length; j+=2) {
        for (int j = 0; j < data.length; j+=2) {
            //rxData[(j-1)/2]=(short)((data[j]<<8)+data[j+1]);
            // rxData[(j)/2]= (short)((data[j+1]<<8)+data[j]); //no shift necessary
            rxData[packageNr][(j)/2] = (short) ((data[j]<<8)+data[j+1]);

            log += String.format("[%d, %d]=%03d ", packageNr,j, rxData[(packageNr)][(j)/2]);
        }
        Log.d(TAG, "data:" + log);


    }

    public ArrayList<Entry> getLastData() {
        // Manipulate arriving text with count
        /*count+=.5;
        if(count==10.0){
            count=1;
        }*/

        ArrayList<Entry> list = new ArrayList<>();
        for (int i = 0; i < TOTALPACKAGES ; i++) {
            for (int j = 0; j < rxData[i].length; j++) {
                list.add(new Entry(j * 100, rxData[i][j])); //count * rxData[i][j]));
            }
        }
        return list;
    }

    /*
    public ArrayList<Entry> getLastData(){
        count+=.1;
        if(count==3.0){
            count=1;
        }
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
    */
}
