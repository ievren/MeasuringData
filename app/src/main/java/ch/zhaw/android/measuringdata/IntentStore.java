package ch.zhaw.android.measuringdata;



import android.content.Intent;

import java.util.HashMap;

public class IntentStore {

    static HashMap<String, Intent> intents=new HashMap();

    static public void put(String name,Intent intent){
        intents.put(name,intent);
    }

    static public Intent get(String name){
        return intents.get(name);
    }
}
