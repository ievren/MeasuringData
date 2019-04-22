package ch.zhaw.android.measuringdata.ui;

import androidx.appcompat.app.AppCompatActivity;
import ch.zhaw.android.measuringdata.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG ="SettingsActivity" ;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String DEVICE = "saved_device_address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Slidr.attach(this);
    }


    public void loadDevice() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String saved_device = sharedPreferences.getString(DEVICE, "");
        Log.d(TAG, "Stored Device is:"+saved_device);
    }
}
