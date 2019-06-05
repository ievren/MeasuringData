/*
 * Zuercher Hochschule fuer Angewandte Wissenschaften (ZHAW)
 * School of Engineering (SoE)
 * InES Institut of Embedded Systems
 *
 *   Bachelorarbeit BA19_gruj_10
 *   Projekt FMS
 *   Darius Eckhardt (eckhadar)
 *   Ibrahim Evren   (evrenibr)
 *
 *   07.06.2019, Winterthur Switzerland
 *
 *   This Software is based on the ble_app_uart, SDK Version 15.2.0
 *   It has been modified to fit the needs of the Project FMS
 *   For correct functionality this Software has to be placed into the same folder as the SDK.
 *
 */

package ch.zhaw.android.measuringdata.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * Write File for Export
 */
public class FileWriteHandle {

    File file;
    java.io.FileWriter fw;
    BufferedWriter bw;
    String entryStr;
    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public boolean open(String dir, String fileName, boolean append) {
        boolean ret=false;
        try {
            file = new File(Environment.getExternalStorageDirectory() + "/" + dir, fileName);
            if (!file.exists()) {
                //error msg
            }
            fw = new java.io.FileWriter(file, append);
            bw = new BufferedWriter(fw);
            ret=true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileWriteHandle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileWriteHandle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }


    public String writeFileWithDate(String str) {
        entryStr = "\n" + df.format(new Date()) + " " + str;
        writeFile(entryStr);
        return entryStr;
    }

    public void writeFile(String str) {

        try {
            if (fw != null) {
                fw.write(str);
                fw.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(FileWriteHandle.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void close() {
        try {
            if (fw != null) {
                fw.flush();
                fw.close();
                bw.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(FileWriteHandle.class.getName()).log(Level.SEVERE, null, ex);
        }

    }





}
