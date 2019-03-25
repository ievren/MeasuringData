package ch.zhaw.android.measuringdata.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;


import androidx.appcompat.app.AppCompatActivity;
import ch.zhaw.android.measuringdata.ActivityStore;
import ch.zhaw.android.measuringdata.MainActivity;
import ch.zhaw.android.measuringdata.data.Data;
import ch.zhaw.android.measuringdata.chart.ChartActivity;
import ch.zhaw.android.measuringdata.uart.UartActivity;
import ch.zhaw.android.measuringdata.uart.UartService;

public class Engine extends AsyncTask {

    static String TAG="Engine";
    ChartActivity   chart;;
    UartActivity    uart;
    Data            data;
    UartService     btService; //Bluetooth Connection
    Intent          btBindIntent;
    boolean         display;
    boolean         connect;
    boolean         btServiceBound=false;
    int             btServiceState=0;  //0=Disconnected, 1=Connecting, 2=Connected
    boolean             loop=true;
    boolean             run=false;
    State               state=State.IDLE;
    ArrayList<Entry>    lastData;
    int delay;
    public Engine() {
    }

    public void setChart(ChartActivity chart) {
        this.chart = chart;
    }

    public void setBtService(UartService btService) {
        this.btService = btService;


    }

    public void setData(Data data) {
        this.data = data;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    @Override
    protected Integer doInBackground(Object[] objects) {
        while(loop) {
            if(run) {
                process();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void process(){
        delay++;

        if(chart==null){
            chart= (ChartActivity) ActivityStore.get("chart");
        }else{
            if(chart!=(ChartActivity) ActivityStore.get("chart")){
                chart=(ChartActivity) ActivityStore.get("chart");
            }
        }
        if(uart==null){
            uart= (UartActivity) ActivityStore.get("uart");
        }else{
            if(uart!=(UartActivity) ActivityStore.get("uart")){
                uart=(UartActivity) ActivityStore.get("uart");
            }
        }


        switch(state){
            case IDLE:
                if(uart!=null) {
                    uart.bindBtService();
                    state=State.CONNECT;
                }
                break;
            case CONNECT:
                //check if a BT-Connection has been made
                btServiceBound = uart.checkBtServiceBound();
                if(btServiceBound) {
                    btServiceState = btService.checkConnectionEstablished();
                }
                if(btServiceState==2) {

                    //state= State.READ_DATA;
                }
                break;
            case READ_DATA:
                if(data.isReady() && delay>10){
                    lastData=data.getLastData();
                    delay=0;
                    display=true;
                    state=State.DISPLAY;
                }
                break;
            case DISPLAY:
                if(delay>20) {
                    delay=0;
                    data.startRead();
                    state = State.READ_DATA;
                }
                break;

        }
        publishProgress("");

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "onPreExecute");


    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        Log.d(TAG, "onPostExecute");
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        if(uart==null){
            uart = (UartActivity) ActivityStore.get("uart");
        }
        else {
            if(connect){
                connect = false;
                //uart.btnConnectDisconnect.performClick();
            }

        }


        if(chart==null) {
            chart = (ChartActivity) ActivityStore.get("chart");
        }
        else {
            if(display) {
                display = false;
                chart.plot(lastData);
            }
        }
        //Log.d(TAG, "onProgressUpdate");
    }

    enum State{IDLE,CONNECT, READ_DATA, DISPLAY}




}
