package ch.zhaw.android.measuringdata.data;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class Data {

    float count=1;

    public boolean isReady(){
        return true;
    }

    public void startRead(){

    }

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
}
