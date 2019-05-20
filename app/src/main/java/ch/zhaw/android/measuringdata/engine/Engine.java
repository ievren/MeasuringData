package ch.zhaw.android.measuringdata.engine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.util.ArrayList;

import ch.zhaw.android.measuringdata.ActivityStore;
import ch.zhaw.android.measuringdata.MainActivity;
import ch.zhaw.android.measuringdata.ui.ChartActivity;
import ch.zhaw.android.measuringdata.data.Data;
import ch.zhaw.android.measuringdata.ui.UartActivity;
import ch.zhaw.android.measuringdata.IntentStore;
import ch.zhaw.android.measuringdata.utils.Utils;

enum State {IDLE, CONNECT, CONNECTED, READ_DATA, DISPLAY, CONNECTION_LOST, EXIT}

public class Engine extends AsyncTask  {
    static String TAG = "Engine";

    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final long DURATION = 2000;
    public static String DEVICE_NAME ="device";
    private Context context;

    ChartActivity chart;
    UartActivity uart;
    MainActivity main;

    Intent chartIntent;
    Intent uartIntent;
    Intent mainIntent;
    Utils utils;
    Data data;

    boolean isFirstConnection = true;
    boolean isDisplayConnectedTo = false;
    boolean display=false;
    boolean isDisplayReady = false;
    int displayOrientation = 0;
    boolean userHasRotated = false;
    boolean isConnect;
    boolean btServiceBound = false;
    int btServiceState = 0;  //0=Disconnected, 1=Connecting, 2=Connected
    boolean loop = true;
    private boolean run = false;
    public State state = State.IDLE;
    State oldState;
    ArrayList<Entry> lastData;
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

    public boolean isConnectionEnabled(){
        return isConnect;
    }

    @Override
    protected void onCancelled(Object o) {
        super.onCancelled(o);
        setRun(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //checkActivityStore();
        Log.d(TAG,"preexecute");

    }

    @Override
    protected Integer doInBackground(Object[] objects) {
        while (loop) {
            if (run && !isCancelled()) {
                checkActivityStore();
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

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        //Log.d(TAG, "onPostExecute");


    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        //uartIntent = IntentStore.get("activity_uart");
        //mainIntent = IntentStore.get("main");
        chartIntent = IntentStore.get("chart");
        // DISPLAY
        if(display && (delay > 2) || userHasRotated && (delay > 2)) {
            delay=0;
            if (chart == null && state != State.EXIT) {
                Log.d(TAG, "chart:"+chart);
                chartIntent.putExtra("keep", true);
                ActivityStore.get("main").startActivity(chartIntent);
                chart = (ChartActivity) ActivityStore.get("chart");
            }
            else if(chart !=null) {
                if(chart.isUserWantCloseApp() || uart.isUserWantCloseApp()){
                    Log.d(TAG,"User want close app detected");
                    state = State.EXIT;
                }
                if (state == State.IDLE || isFirstConnection){
                    isFirstConnection = false;
                    display = false;
                    Log.d(TAG,"plot EmptyData");
                    lastData = data.getEmptyList();
                    chart.plot(lastData);
                }
                //RotatedScreen
                if(userHasRotated==true && isDisplayReady){
                    Log.d(TAG,"User Rotated detected");
                    userHasRotated=false;
                    chart.resetUserHasRotated();
                    chart.getSupportActionBar().setTitle("\u2611 Connected: " + DEVICE_NAME);
                    chart.toolbar.setTitleTextColor(Color.rgb(50, 205, 50));
                    chart.plot(lastData);
                    Log.d(TAG, "Battery level is->"+uart.getBatteryLevel());
                    chart.showBatteryLevel(uart.getBatteryLevel());
                    display = false;
                }
                if (uart.checkConnectionEstablished() == UART_PROFILE_CONNECTED && isDisplayReady) {
                    Log.d(TAG, "isFirstConn"+isFirstConnection);
                    if(!isDisplayConnectedTo) {
                        isDisplayConnectedTo = true;
                        Log.d(TAG, "Connected to:" + DEVICE_NAME);
                        chart.getSupportActionBar().setTitle("\u2611 Connected: " + DEVICE_NAME);
                        chart.toolbar.setTitleTextColor(Color.rgb(50, 205, 50));
                    }
                    chart.plot(lastData);
                    display = false;
                }
                if(uart.checkConnectionEstablished() == UART_PROFILE_DISCONNECTED && isDisplayReady){
                    chart.getSupportActionBar().setTitle("\u2612 Disconnected");
                    chart.toolbar.setTitleTextColor(Color.rgb(244,144,66));
                }
            }



        }

        /**
         * Chart Activity Special Cases
         */
        if(chart != null ){
            //show the Battery Value as value % in chart Activity
            if(uart.isBatteryLevelAvailable){
                uart.isBatteryLevelAvailable = false;
                Log.d(TAG, "Battery level is->"+uart.getBatteryLevel());
                chart.showBatteryLevel(uart.getBatteryLevel());

            }
            //show a Start Measurement Window in ChartActivity
            if(uart.isStartReceived){
                uart.isStartReceived = false;
                chart.showStartReceived(true);

            }
            //Close App
            if(chart.isUserWantGoBack()){
                chart.finish();
                display = false;
                Log.d(TAG, "disconnect called:"+state);
                state = State.IDLE;
                Log.d(TAG, "set state to:"+state);
                uart.setConnect(false);
                uart.Disconnect();
            }
            //Export in ChartActivity
            if(chart.isUserWantExport()){
                chart.userWantExport =false;
                //Context context = ActivityStore.get("main").getApplicationContext();
                //TODO EXPORT DATA

                String dir = "/download";
                String fileName = "measuringData.csv";
                data.exportData(lastData, dir, fileName, DEVICE_NAME);
                File listFile = new File(Environment.getExternalStorageDirectory() + "/" + dir,fileName);
                if(listFile.exists()) {
                    Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                    //Log.d(TAG,URLConnection.guessContentTypeFromName(listFile.getName()));
                    intentShareFile.setType("text/*");
                    // Added because Android 9 -> exposed beyond app through ClipData.Item.getUri()
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    // --End Android 9 -->
                    intentShareFile.putExtra(Intent.EXTRA_STREAM,
                            Uri.parse("file://" + listFile.getAbsolutePath()));
                    ActivityStore.get("chart").startActivity(Intent.createChooser(intentShareFile, "Share File"));
                }
                if (!listFile.exists()) {
                    Toast.makeText(context, "Couldnt export data", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /**
         * UART Activity Special Cases
         */
        if(uart !=null ){
            if(uart.checkConnectionEstablished() == UART_PROFILE_DISCONNECTED && chart !=null){
                chart.getSupportActionBar().setTitle("\u2612 Disconnected");
                chart.toolbar.setTitleTextColor(Color.rgb(244,144,66));
            }
            if(uart.isUserWantCloseApp()) {
                state = State.EXIT;
            }
            if(uart.checkConnectionEstablished() == UART_PROFILE_CONNECTED && isFirstConnection ){
                isFirstConnection = false;
                //state = State.CONNECTED;
            }
            if(uart.isConnectionLost()){
                if( chart != null) {
                    //Log.d(TAG, "isConnectionLost:" + uart.isConnectionLost());
                    chart.getSupportActionBar().setTitle("\u2612 Disconnected");
                    chart.toolbar.setTitleTextColor(Color.rgb(244, 144, 66));
                    chart.finish();
                }
                isFirstConnection = true;
                state = State.CONNECTION_LOST;

            }

        }
    }

    /**
     * checkActivityStore
     *
     *
     */
    private void checkActivityStore(){
        if (chart == null) {
            chart = (ChartActivity) ActivityStore.get("chart");
        } else {
            if (chart != ActivityStore.get("chart")) {
                chart = (ChartActivity) ActivityStore.get("chart");
            }
        }
        if (uart == null) {
            uart = (UartActivity) ActivityStore.get("activity_uart");
        } else {
            if (uart != ActivityStore.get("activity_uart")) {
                uart = (UartActivity) ActivityStore.get("activity_uart");
            }
        }

        if (main == null) {
            main = (MainActivity) ActivityStore.get("main");
        } else {
            if (main != ActivityStore.get("main")) {
                main = (MainActivity) ActivityStore.get("main");
            }
        }

    }

    /**
     * Process()
     * State Machine controlling UART and UI
     */
    private void process() {
        delay++;

        switch (state) {
            case IDLE:
                if(chart != null){
                    chart.finish();
                }
                if (uart != null) {
                    delay = 0;
                    if(oldState==State.CONNECTION_LOST || isFirstConnection){
                        // Auto Connect -> will tell uart to Connect to Last saved Device
                        uart.setConnect(true);
                    }
                    else{
                        uart.setConnect(false);
                    }
                    state = State.CONNECT;
                }
                break;
            case CONNECT:
                //check if a BT-Connection has been made
                btServiceBound = uart.checkBtServiceBound();
                if (btServiceBound) {
                    btServiceState = uart.checkConnectionEstablished();
                    //Log.v(TAG, "btServiceState = "+btServiceState);
                    if (btServiceState == UART_PROFILE_CONNECTED) {
                        state = State.CONNECTED;
                    }
                }
                break;
            case CONNECTED:
                if (uart.checkConnectionEstablished() == UART_PROFILE_CONNECTED) {
                    DEVICE_NAME = uart.getSavedDevice();
                    //Log.d(TAG,"Device_Name="+DEVICE_NAME);
                    display = true;
                    state = State.READ_DATA;
                }
                else {
                    if(delay > 2) {
                        delay = 0;
                        state = State.CONNECTION_LOST;
                    }
                }

                break;
            case READ_DATA:
                if (uart.isDataReady()) {
                    display= false;
                    data.setData(uart.getRecivedData());
                    lastData = data.getLastData();
                    uart.setDataReady(false);
                    state = State.DISPLAY;
                }
                if(uart.checkConnectionEstablished() == UART_PROFILE_DISCONNECTED){
                    state = State.CONNECTION_LOST;
                }
                break;
            case DISPLAY:
                display = true;
                // see onProgressUpdate
                state = State.CONNECTED;
                //}
                break;
            case CONNECTION_LOST:
                display = false;
                isDisplayConnectedTo = false;
                if(chart!=null){
                    chart.finish();
                }
                if(delay > 10){
                    delay = 0;
                    state = State.IDLE;
                }
                break;
            case EXIT:
                Log.d(TAG, "EXIT");
                run =false;
                display =false;
                this.cancel(true);
                uart.setConnect(false);
                Log.d(TAG, "this.cancel");
                if(chart !=null){
                    chart.finish();
                    Log.d(TAG, "chart.cancel");
                }
                if(uart != null){
                    uart.finish();
                    Log.d(TAG, "activity_uart.cancel");
                }
                main.closeApp();
                Log.d(TAG, "main.cancel");
                break;

        }
        publishProgress("");
        if (state != oldState) {
            Log.v(TAG, "old state:"+oldState+"state:" + state);
            oldState = state;

        }

    }

    public ArrayList<Entry> getLastData() {
        ArrayList<Entry> list = new ArrayList<>();
        if(data !=null){
            try {
                list = data.getLastData();
            }catch (NullPointerException e){
                list = null;
            }
        }
        else{
            list = null;
        }
        return list;

    }



    public boolean getIsAppClosing() {
        boolean ret;
        ret = state == State.EXIT;
        return ret;
    }




    public void setDisplayOrientation(int orientation) {
        displayOrientation = orientation;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setUserRotated(boolean b) {
        userHasRotated = b;
        Log.d(TAG,"User rotated: "+b);
    }

    public void setDisplayReady(boolean b) {
        isDisplayReady = b;
    }
}
