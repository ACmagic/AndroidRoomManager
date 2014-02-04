package edu.cmu.sv.arm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import edu.cmu.sv.arm.StartActivityController.ConfigurationStatus;

public class StartActivity extends Activity implements AsyncTaskCompleteListener<ConfigurationStatus> {
	private Button mStartButton;
	
	private TextView mStatusTextView;
	private ScrollView mStatusScrollView;
	private StartActivityController mController = null;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
		mController = new StartActivityController(this.getApplication(), this);
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.start);
        setTitle(R.string.app_name_extended);
        mStartButton = ((Button) findViewById(R.id.startButton));
        mStatusTextView = ((TextView) findViewById(R.id.statusTextView));
        mStatusScrollView = ((ScrollView) findViewById(R.id.statusScrollView));
        
        // This button will be displayed if the application cannot be load with a custom configuration.
        mStartButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent armMain = new Intent(getBaseContext(), AndroidRoomManagerMainActivity.class);
	    		startActivity(armMain);
			}
		});
	}
	
	private void displayMessageInStatusView(String line) {
		mStatusTextView.setText(mStatusTextView.getText() + "\n> " + line);
		
		mStatusScrollView.post(new Runnable() {
			public void run() {
				mStatusScrollView.fullScroll(View.FOCUS_DOWN);
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mStartButton.setEnabled(false);
		mStatusTextView.setText("> Setting up application state...");
		mController.resetApplicationState();

		// configure application
		mController.execute();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	// This method is invoked once the configuration file parsing is finished.
	public void onTaskCompleted(ConfigurationStatus application_configuration_status) {
		if(application_configuration_status == ConfigurationStatus.USING_CUSTOM_SETTINGS_FILE){
			mStartButton.setEnabled(false);    			
			displayMessageInStatusView("Application state ready.");
			Intent armMain = new Intent(getBaseContext(), AndroidRoomManagerMainActivity.class);
			startActivity(armMain);
		}
		else if (application_configuration_status == ConfigurationStatus.USING_DEFAULT_SETTINGS_FILE){
		
			// Do we actually want to load with default settings? Is it there any value?
			displayMessageInStatusView("Using default settings file");
			mStartButton.setEnabled(true);  
		}
		else
		{
			// Should we close the app if no configuration is there?
			displayMessageInStatusView("There has been an unexpected error. The application will close... "); //exception msg?
			mStartButton.setEnabled(false);    			
		}
		
	}
}
