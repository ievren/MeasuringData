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
import ch.zhaw.android.measuringdata.utils.IntentStore;

enum State {IDLE, CONNECT, CONNECTED, READ_DATA, DISPLAY, EXIT}

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

    boolean display;
    boolean isConnect;
    boolean btServiceBound = false;
    int btServiceState = 0;  //0=Disconnected, 1=Connecting, 2=Connected
    boolean loop = true;
    boolean run = false;
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
    }

    @Override
    protected Integer doInBackground(Object[] objects) {
        while (loop) {
            if (run && !isCancelled()) {
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

    private void process() {
        delay++;

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


        switch (state) {
            case IDLE:
                if (uart != null) {
                    uart.setConnect(true);
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
                    delay = 0;
                    DEVICE_NAME = (String) uart.getSavedDevice();
                    Log.d(TAG,"Device_Name="+DEVICE_NAME);
                    display = true;
                    state = State.READ_DATA;
                } else {
                    Log.v(TAG, "--Disconnected");
                    int i = 0;
                    //Try Reconnect
                    while (i < 3) {
                        Log.v(TAG, "--Try Reconnecting");
                        uart.connectDisconnect();
                        if (uart.checkConnectionEstablished() == UART_PROFILE_CONNECTED) {
                            i = UART_PROFILE_CONNECTED;
                        }
                        i++;
                    }
                    state = State.CONNECT;
                }
                break;
            case READ_DATA:
                if (uart.isDataReady() && delay > 20 ) {
                    display= false;
                    delay = 0;
                    data.setData(uart.getRecivedData());
                    lastData = data.getLastData();
                    uart.setDataReady(false);
                    state = State.DISPLAY;

                }
                break;
            case DISPLAY:
                // see onProgressUpdate
                if (delay > 5) {
                    delay = 0;
                    state = State.CONNECTED;
                }
                break;
            case EXIT:
                this.cancel(false);
                uart.setConnect(false);
                display =false;
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
                Log.d(TAG, "uart.cancel");
                break;

        }
        publishProgress("");
        if (state != oldState) {
            Log.v(TAG, "state:" + state);
            oldState = state;

        }

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
        if(display) {
            if (chart == null && state != State.EXIT) {
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
        }
        else if(uart !=null ){
            if(uart.checkConnectionEstablished() == UART_PROFILE_DISCONNECTED && chart !=null){
                chart.getSupportActionBar().setTitle("\u2612 Disconnected");
                chart.toolbar.setTitleTextColor(Color.rgb(244,144,66));
            }
            else if(uart.isConnectionLost()){
                state = State.IDLE;
            }
            else if(uart.isUserWantCloseApp()) {
                state = State.EXIT;
            }
        }
        //Log.d(TAG, "onProgressUpdate");
    }
}
