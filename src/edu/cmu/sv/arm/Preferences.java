package edu.cmu.sv.arm;

import edu.cmu.sv.arm.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;


public class Preferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    setContentView(R.layout.preferences);
	    
	    setTitle(((ARM) getApplication()).getTitle() + " - " + getString(R.string.preferences_label));
	    
	    final Button closeButton = (Button) findViewById(R.id.preferencesCloseButton);
	    closeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                finish();
            }
        });
	}
	
	@Override
    public void onUserInteraction() {
		ARM appState = ((ARM) getApplication());
		
		appState.getMainActivity().resetApplicationResetter();
    }
}