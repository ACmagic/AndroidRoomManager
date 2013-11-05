package edu.cmu.sv.arm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.start);
        
        setTitle(R.string.app_name_extended);
        
        mAppState = ((ARM) getApplication());
        
        mStartButton = ((Button) findViewById(R.id.startButton));
        
        mStartButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent armMain = new Intent(getBaseContext(), AndroidRoomManagerMainActivity.class);
	    		startActivity(armMain);
			}
		});
        
        mStatusTextView = ((TextView) findViewById(R.id.statusTextView));
        mStatusScrollView = ((ScrollView) findViewById(R.id.statusScrollView));
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
		
		if (mAppState.getRooms() != null) {
        	mAppState.getRooms().clear();
        }
        
        if (mAppState.getNumberAddressedRooms() != null) {
        	mAppState.getNumberAddressedRooms().clear();
        }
        
        if (mAppState.getResourceAddressedRooms() != null) {
        	mAppState.getResourceAddressedRooms().clear();
        }
        
        // Read settings file
        // http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
        try {
        	String externalStorageState = Environment.getExternalStorageState();
        	
        	if (Environment.MEDIA_MOUNTED.equals(externalStorageState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState)) {
        		// We can read and write the media ||  We can only read the media
        		String filePath = Environment.getExternalStorageDirectory() + "/" + "ARM";
        		
        		File directory = new File(filePath);
        		File settingsFile = new File(filePath, "arm_settings.xml");
        		File sampleSettingsFile = new File(filePath, "arm_settings_sample.xml");
        			
    			if (settingsFile.exists()) {
    				parseSettingsFile(settingsFile);
    			}
    			else {
    				loadWithDefaultSettings(externalStorageState, directory,
							sampleSettingsFile);
    			}
        	}
        	else {
        		//Cannot read nor write
        		addLineToStatus("Unable to access media storage. Please check device settings.");
        		
        		addLineToStatus("Parsing empty settings...");
        		parseXML(getResources().getAssets().open("arm_settings_empty.xml"));
        		addLineToStatus("Application state ready.");
        		addLineToStatus("You can press the \"Start\" button to start the application with empty settings.");
        	}
        }
        catch (Exception e) {
        	addLineToStatus("There has been an error: " + e.getMessage());
    		
        	try {
        		addLineToStatus("Parsing empty settings...");
        		parseXML(getResources().getAssets().open("arm_settings_empty.xml"));
        		addLineToStatus("Application state ready.");
        		addLineToStatus("You can press the \"Start\" button to start the application with empty settings.");
        	}
        	catch (Exception f) {
        		addLineToStatus("ERROR: Unable to start the application.");
        	}
        }
	}

	private void loadWithDefaultSettings(String externalStorageState,
			File directory, File sampleSettingsFile) throws Exception,
			IOException, FileNotFoundException {
		if (directory.exists() && directory.isDirectory()) {
			loadFromExistingDirectory(externalStorageState, sampleSettingsFile);
		}
		else {
			loadInNewDirectory(externalStorageState, directory,
					sampleSettingsFile);
		}
	}

	private void loadInNewDirectory(String externalStorageState,
			File directory, File sampleSettingsFile) throws IOException,
			FileNotFoundException, Exception {
		if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
			if (directory.mkdirs()) {
				addLineToStatus("Created /ARM/ directory on storage media.");
				addLineToStatus("Creating /ARM/arm_settings_sample.xml file with sample settings...");
				createSampleFileWithDefaults(sampleSettingsFile);
			}
			else {
				addLineToStatus("Unable to create /ARM/ directory. Please check device settings.");
				
				addLineToStatus("Parsing empty settings...");
				parseXML(getResources().getAssets().open("arm_settings_empty.xml"));
				addLineToStatus("Application state ready.");
				addLineToStatus("You can press the \"Start\" button to start the application with empty settings.");
			}
		}
		else {
			addLineToStatus("Unable to create /ARM/ directory. Please check device settings.");
			
			addLineToStatus("Parsing empty settings...");
			parseXML(getResources().getAssets().open("arm_settings_empty.xml"));
			addLineToStatus("Application state ready.");
			addLineToStatus("You can press the \"Start\" button to start the application with empty settings.");
		}
	}

	private void loadFromExistingDirectory(String externalStorageState,
			File sampleSettingsFile) throws Exception, IOException,
			FileNotFoundException {
		if (sampleSettingsFile.exists()) {
			addLineToStatus("Please see the /ARM/arm_settings_sample.xml file.");
			addLineToStatus("Use the sample to create /ARM/arm_settings.xml and fill it with desired settings.");
			
			addLineToStatus("Parsing empty settings...");
			parseXML(getResources().getAssets().open("arm_settings_empty.xml"));
			addLineToStatus("Application state ready.");
			addLineToStatus("You can press the \"Start\" button to start the application with empty settings.");
		}
		else {
			if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
				addLineToStatus("Creating /ARM/arm_settings_sample.xml file with sample settings...");
				
				createSampleFileWithDefaults(sampleSettingsFile);
			}
			else {
				addLineToStatus("Unable to create sample settings file. Please check device settings.");
				
				addLineToStatus("Parsing empty settings...");
				parseXML(getResources().getAssets().open("arm_settings_empty.xml"));
				addLineToStatus("Application state ready.");
				addLineToStatus("You can press the \"Start\" button to start the application with empty settings.");
			}
		}
	}

	private void createSampleFileWithDefaults(File sampleSettingsFile)
			throws IOException, FileNotFoundException {
		InputStream sampleSettingsInputStream = getResources().getAssets().open("arm_settings_sample.xml");
		
		FileOutputStream sampleSettingsFileOutputStream = new FileOutputStream(sampleSettingsFile);
		
		byte[] buffer = new byte[1024];
		int readLength = 0;
		
		while ((readLength = sampleSettingsInputStream.read(buffer)) > 0) {
			sampleSettingsFileOutputStream.write(buffer, 0, readLength);
		}
		
		sampleSettingsFileOutputStream.close();
		
		scanFile(sampleSettingsFile);
	}

	private void scanFile(File sampleSettingsFile) {
		MediaScannerConnection.scanFile(this, new String[] {sampleSettingsFile.getPath()}, null, new OnScanCompletedListener() {
			public void onScanCompleted(String path, Uri uri) {
				runOnUiThread(new Runnable() {
					public void run() {
						addLineToStatus("Created /ARM/arm_settings_sample.xml file on storage media.");
						addLineToStatus("Use the sample to create /ARM/arm_settings.xml and fill it with desired settings.");
						
						addLineToStatus("Parsing empty settings...");
		        		try {
							parseXML(getResources().getAssets().open("arm_settings_empty.xml"));
							addLineToStatus("Application state ready.");
							addLineToStatus("You can press the \"Start\" button to start the application with empty settings.");
						} catch (Exception e) {
							addLineToStatus("There has been an error: " + e.getMessage());
							addLineToStatus("ERROR: Unable to start the application.");
						}
					}
				});
			}
		});
	}

	private void parseSettingsFile(File settingsFile) throws Exception,
			FileNotFoundException {
		addLineToStatus("Settings file found. Parsing...");
		parseXML(new FileInputStream(settingsFile));
		mStartButton.setEnabled(false);
		
		addLineToStatus("Application state ready.");
		Intent armMain = new Intent(getBaseContext(), AndroidRoomManagerMainActivity.class);
		startActivity(armMain);
		
		//addLineToStatus("You can press the \"Start\" button to start the application.");
	}
	
	private void parseXML(InputStream armSettingsInputStream) throws Exception {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = factory.newPullParser(); 
		parser.setInput(armSettingsInputStream, "UTF-8");
		
		int eventType = parser.getEventType();
		
		String currentBuildingNumber = null;
		Room currentRoom = null;
		
		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tagName = null;
			
			switch (eventType) {
			case XmlPullParser.START_TAG:
				tagName = parser.getName().toLowerCase();
				
				int numAttrs = parser.getAttributeCount();
				
				if (tagName.equals("room")) {
					currentRoom = new Room();
					currentRoom.setBuilding(currentBuildingNumber);
				}
				
				for (int i = 0; i < numAttrs; i++) {
					String attrName = parser.getAttributeName(i);
					String attrValue = parser.getAttributeValue(i);
					
					if (tagName.equals("application")) {
						parseApplicationInfo(attrName, attrValue);
					}
					else if (tagName.equals("building")) {
						currentBuildingNumber = parseBuildingInfo(
								currentBuildingNumber, attrName, attrValue);
					}
					else if (tagName.equals("room")) {
						parseRoomInfo(currentRoom, attrName, attrValue);
					}
//					else if (tagName.equals("image")) {
//						parseImageInfo(currentRoom, attrName, attrValue);
//					}
				}
				break;
			case XmlPullParser.END_TAG:
				tagName = parser.getName();
				
				if (tagName.equals("room")) {
					mAppState.addRoom(currentRoom);
					
					if (currentRoom.isDefault()) {
						mAppState.setDefaultRoom(currentRoom);
					}
					
					addLineToStatus("Parsed " + currentRoom.getFullName() + ".");
				}
				break;
			default:
				break;
			}
			
			eventType = parser.next();
		}
		
		if (mAppState.getDefaultRoom() != null) {
	    	mAppState.setCurrentRoom(mAppState.getDefaultRoom());
	    }
	    else {
	    	mAppState.setCurrentRoom(mAppState.getNumberAddressedRooms().elements().nextElement());
	    	mAppState.setDefaultRoom(mAppState.getCurrentRoom());
	    }
	    
		addLineToStatus("Parsing complete.");
	    mStartButton.setEnabled(true);
	}

//	private void parseImageInfo(Room currentRoom, String attrName,
//			String attrValue) {
//		if (attrName.equals("url")) {
//			RoomImage roomImage = new RoomImage(attrValue);
//			currentRoom.addImage(roomImage);
//		}
//		else {
//			RoomImage lastRoomImage = currentRoom.getImages().get(currentRoom.getImages().size() - 1);
//			int value = Integer.parseInt(attrValue);
//			
//			if (attrName.equals("refresh_delay_minutes")) {
//				lastRoomImage.setRefreshDelayMinutes(value);
//			}
//			else if (attrName.equals("refresh_delay_seconds")) {
//				lastRoomImage.setRefreshDelaySeconds(value);
//			}
//			else if (attrName.equals("refresh_delay_milliseconds")) {
//				lastRoomImage.setRefreshDelayMilliseconds(value);
//			}
//		}
//	}

	private void parseRoomInfo(Room currentRoom, String attrName,
			String attrValue) {
		if (attrName.equals("number")) {
			currentRoom.setNumber(attrValue);
		}
		else if (attrName.equals("resource_address")) {
			currentRoom.setResourceAddress(attrValue);
		}
		else if (attrName.equals("default")) {
			currentRoom.setIsDefault(Boolean.parseBoolean(attrValue));
		}
	}

	private String parseBuildingInfo(String currentBuildingNumber,
			String attrName, String attrValue) {
		if (attrName.equals("number")) {
			currentBuildingNumber = attrValue;
		}
		return currentBuildingNumber;
	}

	private void parseApplicationInfo(String attrName, String attrValue) {
		if (attrName.equals("title")) {
			mAppState.setTitle(attrValue);
		}							
		else if (attrName.equals("google_calendar_api_key")) {
			mAppState.setGoogleCalendarAPIKey(attrValue);
		}
		else if (attrName.equals("google_account")) {
			mAppState.setGoogleAccountName(attrValue);
		}
		else {
			int value = Integer.parseInt(attrValue);
			
			if (attrName.equals("timeout_minutes")) {
				mAppState.setApplicationTimeoutMinutes(value);
			}
			else if (attrName.equals("timeout_seconds")) {
				mAppState.setApplicationTimeoutSeconds(value);
			}
			else if (attrName.equals("calendar_refresh_delay_minutes")) {
				mAppState.setCalendarRefreshDelayMinutes(value);
			}
			else if (attrName.equals("calendar_refresh_delay_seconds")) {
				mAppState.setCalendarRefreshDelaySeconds(value);
			}
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
}
