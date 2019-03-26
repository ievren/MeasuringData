package ch.zhaw.android.measuringdata.data;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class Data {
    private enum Endian {LITTLE, BIG;}

    float count=1;
    //
    //-------------------------------------------|| ||
    short rxData=0; //little Endian, '00 00'=0, '01 00'=1,  '02 00'=2,...

    public boolean isReady(){
        return true;
    }

    public void startRead(){

    }

    /*public short createRxExampleData (int x){
        short test[x]=0;
        for (int i = 0; i < x; i=i+2) {
            test[i]=0;
            test[i+1]=i;
        }
        return test;
    }*/

    public ArrayList<Entry> getExampleData(byte rxData[]){
        ArrayList<Entry> dataVals = new ArrayList();
        for (int i = 0; i < rxData.length ; i++) {
            dataVals.add(new Entry(i*20,rxData[i]));
            getInt16Value(getByteAsList(rxData,0),0,Endian.LITTLE);
        }
        return dataVals;
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

    private int getInt16Value(ArrayList<Byte> bytes, int startByte, Endian endian) {
        return (short) getByteValue(bytes, startByte, 2, endian);
    }

    private long getByteValue(ArrayList<Byte> bytes, int startByte, int byteSize, Endian endian) {
        long value = 0;
        for (int j = 0; j < byteSize; j++) {
            if (endian == Endian.LITTLE) {
                //Swap 2 Bytes(value)
                value = value + ((long) (bytes.get(j + startByte)&0xFF) << (j * 8));
            } else {
                value = value + ((long) (bytes.get(j + startByte)&0xFF) << ((byteSize - 1 - j) * 8));
            }
        }
        return value;
    }

    private ArrayList<Byte> getByteAsList(byte[] bytes, int startIndex) {
        ArrayList<Byte> list = new ArrayList<>();
        for (int i = startIndex; i < bytes.length; i++) {
            list.add(bytes[i]);
        }
        return list;
    }

}
