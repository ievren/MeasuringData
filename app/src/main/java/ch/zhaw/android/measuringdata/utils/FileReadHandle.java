package ch.zhaw.android.measuringdata.utils;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * Write File for Reading Exported Data
 */
public class FileReadHandle {

    File file;
    FileReader fr;
    BufferedReader br;

    public boolean open(String dir, String fileName) {
        boolean ret=false;
        try {
            file = new File(Environment.getExternalStorageDirectory() + "/"+dir, fileName);
            if (!file.exists()) {
                //error msg
            }
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            ret=true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileReadHandle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public String readLine(){
        String ret="";
        try {
            ret=br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public ArrayList<String> readAll(){
        ArrayList<String> ret=new ArrayList();
        String line;

        try {
            while((line=br.readLine())!=null){
                ret.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }


    public void close() {
        try {
            fr.close();
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(FileReadHandle.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
