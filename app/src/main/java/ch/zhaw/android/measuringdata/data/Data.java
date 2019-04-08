package ch.zhaw.android.measuringdata.data;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class Data {

    final static String TAG="Data";
    final static int TOTALPACKAGES=3;
    final static int FREQUENCE_S = 20; //Abtastrate 50 Hz -> 20ms

    float count=1;
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
        for (int i = 0; i <= packagenrs; i++) {
            for (int j = 0; j < data[0].length; j += 2) {
                //rxData[(j-1)/2]=(short)((data[j]<<8)+data[j+1]);
                // rxData[(j)/2]= (short)((data[j+1]<<8)+data[j]);
                // no shift necessary
                //rxData[i][(j) / 2] = ( ((data[i][j] << 8) + data[i][j + 1]) & 0xFF);
                //rxData[i][(j) / 2] =   ((data[i][j]<<8) + data[i][j + 1]);
                int a = (short) (data[i][j]<<8 );
                int b = (byte) (data[i][j + 1] );
                int c = 0;
                if(a> 255){
                    c = (a+b);
                }
                else {
                    c = (a+b) &0xff;
                }
                rxData[i][(j) / 2] = c;
                log += String.format("[%d, %d]=%d ", i, j,  rxData[(i)][(j) / 2]); // rxdata & 0xFFFF -> unsigned
            }
            Log.d(TAG, "data:" + log);
            log = "";
        }




    }

    public ArrayList<Entry> getLastData() {


        // Manipulate arriving text with count
        count+=0.1;
        if(count>=2){
            count=1;
        }

        ArrayList<Entry> list = new ArrayList<>();
        int a= 1;
        for (int i = 0; i < TOTALPACKAGES ; i++) {
            for (int j = 0; j < rxData[i].length; j++) {
                list.add(new Entry((a) * FREQUENCE_S/100, count* (rxData[i][j])) ); //count * rxData[i][j]));
                a++;
            }
        }
        a=0;
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
