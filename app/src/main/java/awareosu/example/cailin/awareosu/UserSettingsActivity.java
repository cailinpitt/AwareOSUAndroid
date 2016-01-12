package awareosu.example.cailin.awareosu;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import com.example.cailin.awareosu.R;

/**
 * Created by Cailin on 1/5/2016.
 */
public class UserSettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new UserSettingsFragment()).commit();
    }
    public static class UserSettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
        }
    }
}
