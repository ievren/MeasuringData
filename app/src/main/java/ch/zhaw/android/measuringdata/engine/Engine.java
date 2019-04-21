package ch.zhaw.android.measuringdata.engine;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

import ch.zhaw.android.measuringdata.ActivityStore;
import ch.zhaw.android.measuringdata.MainActivity;
import ch.zhaw.android.measuringdata.ui.ChartActivity;
import ch.zhaw.android.measuringdata.data.Data;
import ch.zhaw.android.measuringdata.ui.UartActivity;
import ch.zhaw.android.measuringdata.IntentStore;

enum State {IDLE, CONNECT, CONNECTED, READ_DATA, DISPLAY, CONNECTION_LOST, EXIT}

public class Engine extends AsyncTask {
    static String TAG = "Engine";

    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final long DURATION = 2000;
    public static String DEVICE_NAME ="device";
    private Context context;

    ChartActivity chart;
    UartActivity uart;
    MainActivity main;

    Data data;

    boolean display=false;
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

        // DISPLAY
        if(display && (delay > 2)) {
            delay=0;
            if (chart == null && state != State.EXIT) {
                Log.d(TAG, "chart:"+chart);
                ActivityStore.get("main").startActivity(IntentStore.get("chart"));
                chart = (ChartActivity) ActivityStore.get("chart");
            } else {
                if(chart.isUserWantCloseApp() || uart.isUserWantCloseApp()){
                    state = State.EXIT;
                }
                else if (state == State.IDLE){
                    display = false;
                    Log.d(TAG,"plot EmptyData");
                    lastData = data.getEmptyList();
                    chart.plot(lastData);

                }
                else if (uart.checkConnectionEstablished() == UART_PROFILE_CONNECTED) {
                    Log.d(TAG,"Connected to:"+DEVICE_NAME);
                    display = false;
                    chart.getSupportActionBar().setTitle("\u2611 Connected to: "+DEVICE_NAME);
                    chart.toolbar.setTitleTextColor(Color.rgb(50,205,50));
                    chart.plot(lastData);


                }

            }
        }
        else if(chart != null ){
            if(chart.isUserWantCloseApp()) {
                state = State.EXIT;
            }
            else if(uart.isStartReceived){
                uart.isStartReceived = false;
                chart.showStartReceived(true);
            }
        }
        else if(uart !=null ){
            if(uart.checkConnectionEstablished() == UART_PROFILE_DISCONNECTED && chart !=null){
                chart.getSupportActionBar().setTitle("\u2612 Disconnected");
                chart.toolbar.setTitleTextColor(Color.rgb(244,144,66));
            }
            else if(uart.isUserWantCloseApp()) {
                state = State.EXIT;
            }
            else if(uart.checkConnectionEstablished() == UART_PROFILE_CONNECTED){
                state = State.CONNECTED;
            }
            else if(uart.isConnectionLost()){
                if( chart != null) {
                    Log.d(TAG, "isConnectionLost:" + uart.isConnectionLost());
                    chart.getSupportActionBar().setTitle("\u2612 Disconnected");
                    chart.toolbar.setTitleTextColor(Color.rgb(244, 144, 66));
                }
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
            if (chart != (ChartActivity) ActivityStore.get("chart")) {
                chart = (ChartActivity) ActivityStore.get("chart");
            }
        }
        if (uart == null) {
            uart = (UartActivity) ActivityStore.get("uart");
        } else {
            if (uart != (UartActivity) ActivityStore.get("uart")) {
                uart = (UartActivity) ActivityStore.get("uart");
            }
        }

        if (main == null) {
            main = (MainActivity) ActivityStore.get("main");
        } else {
            if (main != (MainActivity) ActivityStore.get("main")) {
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
                else if (uart != null) {
                    delay = 0;
                    if(oldState!=State.CONNECTION_LOST){
                        uart.setConnect(true);
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
                    DEVICE_NAME = (String) uart.getSavedDevice();
                    Log.d(TAG,"Device_Name="+DEVICE_NAME);
                    display = true;
                    state = State.READ_DATA;
                }
                else {
                    state = State.IDLE;
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
                else if(uart.isConnectionLost()){
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
                if(delay > 20){
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
                    Log.d(TAG, "uart.cancel");
                }
                main.closeApp();
                Log.d(TAG, "main.cancel");
                break;

        }
        publishProgress("");
        if (state != oldState) {
            Log.v(TAG, "state:" + state);
            oldState = state;

        }

    }

    public boolean getIsAppClosing() {
        boolean ret;
        if(state == State.EXIT){
            ret= true;
        }
        else{
            ret = false;
        }
        return ret;
    }
}
