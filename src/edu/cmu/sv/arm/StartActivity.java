package edu.cmu.sv.arm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import edu.cmu.sv.arm.StartActivityController.ConfigurationStatus;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class StartActivity extends Activity {
	private ARM mAppState;
	private Button mStartButton;
	
	private TextView mStatusTextView;
	private ScrollView mStatusScrollView;
	private StartActivityController mController;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.start);
        setTitle(R.string.app_name_extended);
        mStartButton = ((Button) findViewById(R.id.startButton));
        mStatusTextView = ((TextView) findViewById(R.id.statusTextView));
        mStatusScrollView = ((ScrollView) findViewById(R.id.statusScrollView));
        
        mStartButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent armMain = new Intent(getBaseContext(), AndroidRoomManagerMainActivity.class);
	    		startActivity(armMain);
			}
		});
        mController = new StartActivityController(getApplication());
        
        mAppState = ((ARM) getApplication()); 
        
	}
	
	private void addLineToStatus(String line) {
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
		resetApplicationState();
        configureApplication();
	}

	private void resetApplicationState() {
		mController.resetApplicationState();
	}

	private void configureApplication(){
		ConfigurationStatus application_configuration_status = mController.configureApplication();
    	 switch (application_configuration_status) {
    	 	case READ_MEDIA_ERROR:
	    		addLineToStatus("Unable to access media storage. Please check device settings.");
	    		addLineToStatus("Parsing empty settings...");
	    		addLineToStatus("Application state ready.");
	    		addLineToStatus("You can press the \"Start\" button to start the application with empty settings.");
	    		break;
    		case USING_CUSTOM_SETTINGS_FILE:
				addLineToStatus("Settings file found. Parsing...");
				mStartButton.setEnabled(false);    			
				addLineToStatus("Application state ready.");
				Intent armMain = new Intent(getBaseContext(), AndroidRoomManagerMainActivity.class);
				startActivity(armMain);
				break;
			case USING_DEFAULT_SETTINGS_FILE:
				break;
			case UNEXPECTED_ERROR:
				addLineToStatus("There has been an unexpected error: "); //excpetion msg?
				break;
    	}
        addLineToStatus("Parsing complete.");
		mStartButton.setEnabled(true);
	}
	
	
	
	@Override
	public void onPause() {
		super.onPause();
	}
}
