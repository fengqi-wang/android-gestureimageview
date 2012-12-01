package ch.codepanda.gestureimage;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AboutActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.pref_app_info);
	}
}
