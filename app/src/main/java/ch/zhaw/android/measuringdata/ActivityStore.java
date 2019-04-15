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
