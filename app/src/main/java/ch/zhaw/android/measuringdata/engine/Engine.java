package ch.zhaw.android.measuringdata.engine;

import android.os.AsyncTask;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

import ch.zhaw.android.measuringdata.ActivityStore;
import ch.zhaw.android.measuringdata.data.Data;
import ch.zhaw.android.measuringdata.chart.ChartActivity;

public class Engine extends AsyncTask {

    static String TAG="Engine";
    enum State{IDLE,CONNECT, READ_DATA, DISPLAY};

    ChartActivity chart;
    Data data;
    boolean display;
    boolean connected;

    boolean loop=true;
    boolean run=false;
    State state=State.IDLE;
    ArrayList<Entry> lastData;
    int delay;


    public Engine() {
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

        switch(state){
            case IDLE:
                state=State.CONNECT;
                break;
            case CONNECT:
                if(connected==true) {
                    state = State.READ_DATA;
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

        if(chart==null) {
            chart = (ChartActivity) ActivityStore.get("chart");
        }else{
            if(display) {
                display = false;
                chart.plot(lastData);
            }
        }
        //Log.d(TAG, "onProgressUpdate");
    }




}
