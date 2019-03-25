package ch.zhaw.android.measuringdata.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
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

enum State{IDLE,CONNECT,CONNECTED, READ_DATA, DISPLAY}

public class Engine extends AsyncTask {

    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final long DURATION = 2000 ;
    private Context context;

    static String TAG="Engine";
    ChartActivity   chart;;
    UartActivity    uart;
    Data            data;
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


    public Engine(Context context) {
        this.context = context;
    }

    public void setChart(ChartActivity chart) {
        this.chart = chart;
    }


    public void setData(Data data) {
        this.data = data;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "onPreExecute");
        new Handler().postDelayed(() -> {
            final Intent intent = new Intent(context, UartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(intent);
            //finish();
        }, DURATION);
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
                if(uart!=null){
                    Log.v(TAG, "<----- State=CONNECT ---->");
                    state=State.CONNECT;
                }
                break;
            case CONNECT:
                //check if a BT-Connection has been made
                btServiceBound = uart.checkBtServiceBound();
                if(btServiceBound) {
                    btServiceState = uart.checkConnectionEstablished();
                    //Log.v(TAG, "btServiceState = "+btServiceState);
                    if(btServiceState==UART_PROFILE_CONNECTED) {
                        state = State.CONNECTED;
                    }
                }
                break;
            case CONNECTED:
                if(uart.checkConnectionEstablished()==UART_PROFILE_CONNECTED) {
                    //state= State.READ_DATA;
                }
                else {
                    Log.v(TAG, "--Disconnected");
                    int i=0;
                    //Try Reconnect
                    while(i <3) {
                        Log.v(TAG, "--Try Reconnecting");
                        uart.btnConnectDisconnect.performClick();
                        if(uart.checkConnectionEstablished()==UART_PROFILE_CONNECTED){
                            i=UART_PROFILE_CONNECTED;
                        }
                        i++;
                    }
                    state= State.CONNECT;
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
}
