package ch.zhaw.android.measuringdata.data;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class Data {

    final static String TAG="Data";
    private enum Endian {LITTLE, BIG;}

    float count=1;
    //
    //-------------------------------------------|| ||
    short[] rxData; //little Endian, '00 00'=0, '01 00'=1,  '02 00'=2,...
    ArrayList<Entry> dataList=new ArrayList();

    public boolean isReady(){
        return true;
    }

    public void startRead(){

    }

    public void setData(byte[] data) {
        String log="";
        //int rxDataLen=(data.length-1)/2;
        int rxDataLen=(data.length)/2;
        rxData=new short[rxDataLen];

        //first byte is package no
        //for (int j = 1; j < data.length; j+=2) {
        for (int j = 0; j < data.length; j+=2) {
            //rxData[(j-1)/2]=(short)((data[j]<<8)+data[j+1]);
            rxData[(j)/2]= (short)((data[j+1]<<8)+data[j]);
            log+=String.format("%d_%03d ",j,rxData[(j)/2]);
        }
        Log.d(TAG,"data:"+log);
    }

    public ArrayList<Entry> getLastData() {
        count+=.5;
        if(count==10.0){
            count=1;
        }

        ArrayList<Entry> list = new ArrayList<>();
        for (int i = 0; i < rxData.length; i++) {
            list.add(new Entry(i*100,count*rxData[i]));
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
