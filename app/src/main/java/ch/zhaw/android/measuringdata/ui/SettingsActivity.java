package ch.zhaw.android.measuringdata.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import ch.zhaw.android.measuringdata.R;
import ch.zhaw.android.measuringdata.utils.MySettingsFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

public class SettingsActivity extends PreferenceActivity {
    private static final String TAG ="SettingsActivity" ;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String DEVICE = "saved_device_address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        loadDevice();
        getFragmentManager().beginTransaction().add(android.R.id.content, new MySettingsFragment()).commit();





        //Swipe to destroy
        Slidr.attach(this);
    }


    public void loadDevice() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String saved_device = sharedPreferences.getString(DEVICE, "");
        Log.d(TAG, "Stored Device is:"+saved_device);
    }
}
