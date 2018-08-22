package com.example.blescanner;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    public Integer mVlaue;

    public SettingsFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            pickPreferenceObject(getPreferenceScreen().getPreference(i));
        }
        Preference pref = findPreference("rssi_val");
        pref.setSummary(String.valueOf(pref.getSharedPreferences().getInt("rssi_val",50)));




    }
    public void setvalue(Integer value){
        mVlaue=value;

    }

    private void pickPreferenceObject(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory cat = (PreferenceCategory) p;
            for (int i = 0; i < cat.getPreferenceCount(); i++) {
                pickPreferenceObject(cat.getPreference(i));
            }
        } else {
            initSummary(p);
        }
    }

    private void initSummary(Preference p) {

        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getValue());
        }



    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("exibit_list")) {
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));

        }

        if (key.equals("rssi_val")) {
           // System.out.println("sdsdsdsdsdsd");
            Preference pref = findPreference(key);
            pref.setSummary(String.valueOf(sharedPreferences.getInt(key,0)));

        }
        if (key.equals("server_IP")) {
            // System.out.println("sdsdsdsdsdsd");
            Preference pref = findPreference(key);
            pref.setSummary(String.valueOf(sharedPreferences.getString(key,"")));

        }


    }


}
