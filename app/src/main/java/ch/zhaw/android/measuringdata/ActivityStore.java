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

package ch.zhaw.android.measuringdata;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;

public class ActivityStore {
    private static final String TAG = "ActivityStore";
    static HashMap<String, Activity> activities=new HashMap();

    static public void put(String name,Activity activity){
        //Log.d(TAG,"put:"+name+" activity:"+activity);
        activities.put(name,activity);
    }

    static public Activity get(String name)
    {
        //Log.d(TAG,"put:"+name+" activity:"+activities.get(name));
        return activities.get(name);
    }
}
