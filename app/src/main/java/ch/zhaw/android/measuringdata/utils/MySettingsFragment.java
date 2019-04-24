package ch.zhaw.android.measuringdata.utils;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import ch.zhaw.android.measuringdata.R;

public class MySettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);
    }
}