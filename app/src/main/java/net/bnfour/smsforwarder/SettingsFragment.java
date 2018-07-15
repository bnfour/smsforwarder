package net.bnfour.smsforwarder;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.content.SharedPreferences;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        updateAllSummaries();
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager()
                .getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updateAllSummaries();
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceManager()
                .getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummaryByKey(key);
    }

    private void updateAllSummaries() {
        updatePhoneNumber();
        updateListType();
        updateFilterList();
    }

    private void updateSummaryByKey(String key) {
        switch (key) {
            case "phone":
                updatePhoneNumber();
                break;
            case "list_type":
                updateListType();
                break;
            case  "filter_list":
                updateFilterList();
                break;
                default:
                    return;
        }
    }

    // also disables other settings if phone number is not set
    private void updatePhoneNumber() {
        final String phoneKey = "phone";

        Preference phonePreference = findPreference(phoneKey);
        Preference enabledPreference = findPreference("enabled");

        String number = getPreferenceManager()
                .getSharedPreferences().getString(phoneKey, "");

        boolean valid = !number.equals("");
        phonePreference.setSummary(valid ? number : getString(R.string.phone_not_set));
        enabledPreference.setEnabled(valid);
    }

    private void updateListType() {
        final String listTypeKey = "list_type";

        Preference listType = findPreference(listTypeKey);

        int type = Integer.parseInt(getPreferenceManager()
                .getSharedPreferences().getString(listTypeKey, "0"));
        String summary = type == 0 ? getString(R.string.filter_type_summary_black) :
                getString(R.string.filter_type_summary_white);

        listType.setSummary(summary);
    }

    private void updateFilterList() {
        final String filterKey = "filter_list";

        Preference filterList = findPreference(filterKey);

        String value = getPreferenceManager()
                .getSharedPreferences().getString(filterKey, "");
        // split of empty line gives one empty line, but there are no entries
        int count = value.equals("") ? 0 : value.split(";").length;

        String summary = getString(R.string.filter_list_entries_number) + String.valueOf(count);
        filterList.setSummary(summary);
    }
}
