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
