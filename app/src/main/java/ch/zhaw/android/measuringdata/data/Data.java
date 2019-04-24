package ch.zhaw.android.measuringdata.data;

import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import androidx.annotation.NonNull;

public class Data {

    final static String TAG="Data";
    final static int TOTALPACKAGES=4;
    final static float FREQUENCE_Hz = 333; //Abtastrate

    double count=1;
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
                Log.d(TAG,"c(z):"+c+"("+z+")");
                log += String.format("[%d, %d]=%d ", i, ((j) / 2),  rxData[(i)][(j) / 2]); // rxdata & 0xFFFF -> unsigned
            }
            Log.d(TAG, "data:" + log);
            log = "";
        }




    }

    public static short byte2short(byte[] data)
    {
        short i = (short) ((data[0] << 8) | (data[1]));
        return i;
    }

    public ArrayList<Entry> getLastData() {
        String log="";
        // Manipulate arriving data
        ArrayList<Entry> list = new ArrayList<>();
        int a= 1;
        for (int i = 0; i < TOTALPACKAGES ; i++) {
            for (int j = 0; j < rxData[i].length; j++) {
                list.add(new Entry((float) ((a * 1/FREQUENCE_Hz)),  (int)(rxData[i][j]) ) ); //count * rxData[i][j]));
                //log += String.format("[%f, %f], ", (float) ((a * 1/FREQUENCE_Hz)),  (int)(rxData[i][j]) ); // rxdata & 0xFFFF -> unsigned
                a++;

            }
        }
        a=0;
        //Log.d(TAG, "plot:" + log);
        return list;
    }

    public ArrayList<Entry> getEmptyList(){
        ArrayList<Entry> dataVals = new ArrayList();
        dataVals.add(new Entry(0,0));
        //dataVals.add(new Entry(500,100));
        //dataVals.add(new Entry(1000,500));
        //dataVals.add(new Entry(1500,100));
        //dataVals.add(new Entry(2000,100));
        return dataVals;

    }
    //FIXME ADDING EXPORT-FUNCTION
    //Save chart data
    public void export(ArrayList<Entry> lastData, Context context) {
        Log.d(TAG,"export called");
        String fileName ="./exported_data.csv";
        FileOutputStream out=null;
        try{
            out = context.openFileOutput("listFile", Context.MODE_PRIVATE);
            ObjectOutputStream outputStream = new ObjectOutputStream(out);
            outputStream.writeObject(lastData);
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG,"export Data didnt work");
            e.printStackTrace();
        } finally {
            try {
                if( out !=  null){
                    out.close();
                    Log.d(TAG,"exported");
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

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
