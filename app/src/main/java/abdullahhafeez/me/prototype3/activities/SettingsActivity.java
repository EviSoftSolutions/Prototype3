package abdullahhafeez.me.prototype3.activities;

import android.preference.PreferenceFragment;
import android.os.Bundle;

import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.activities.AppCompatPreferenceActivity;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Load Settings Fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();

    }


    public static class MainPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

        }

    }

}
