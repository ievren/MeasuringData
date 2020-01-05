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
 *   Copyright (c) 2015, Nordic Semiconductor
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the name of nRF UART nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *   FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

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
