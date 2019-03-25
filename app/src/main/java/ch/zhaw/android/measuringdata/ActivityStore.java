package ch.zhaw.android.measuringdata;

import android.app.Activity;

import java.util.HashMap;

public class ActivityStore {

    static HashMap<String, Activity> activities=new HashMap();

    static public void put(String name,Activity activity){
        activities.put(name,activity);
    }

    static public Activity get(String name){
        return activities.get(name);
    }
}
