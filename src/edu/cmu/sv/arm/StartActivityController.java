package edu.cmu.sv.arm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Application;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import edu.cmu.sv.arm.StartActivityController.ConfigurationStatus;

public class StartActivityController extends AsyncTask <Void, Void, ConfigurationStatus>{
	private ARM mAppState;
	// Pass files from view instead
	private Application mApplication; 
	private AsyncTaskCompleteListener<ConfigurationStatus> mTaskCompletedCallback;
	
	public enum ConfigurationStatus {USING_CUSTOM_SETTINGS_FILE, USING_DEFAULT_SETTINGS_FILE,
		READ_MEDIA_ERROR, UNEXPECTED_ERROR}

	public StartActivityController(Application app, AsyncTaskCompleteListener<ConfigurationStatus> callback)
	{
		this.mApplication = app;
		this.mAppState = ((ARM) app);
		this.mTaskCompletedCallback = callback;
	}
	
	public void resetApplicationState(){
		if (mAppState.getRooms() != null) {
        	mAppState.getRooms().clear();
        }
        
        if (mAppState.getNumberAddressedRooms() != null) {
        	mAppState.getNumberAddressedRooms().clear();
        }
        
        if (mAppState.getResourceAddressedRooms() != null) {
        	mAppState.getResourceAddressedRooms().clear();
        }
	}
	
	public ConfigurationStatus configureApplication(){
		try {
			// Read configuration file
			String externalStorageState = Environment.getExternalStorageState();
	    	
	    	if (Environment.MEDIA_MOUNTED.equals(externalStorageState) || 
	    			Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState)) {
	    		if(parseConfiguration() == true){
					return ConfigurationStatus.USING_CUSTOM_SETTINGS_FILE;
				}
				else
				{
					return ConfigurationStatus.READ_MEDIA_ERROR;
				}
	    	}
	    	else
	    	{
	    		return ConfigurationStatus.READ_MEDIA_ERROR;
	    	}
		}		
	     catch (Exception e) {
			return ConfigurationStatus.UNEXPECTED_ERROR;
		}
	}
	
	public boolean parseConfiguration() throws Exception {
		// We can read and write the media ||  We can only read the media
		String filePath = Environment.getExternalStorageDirectory() + "/" + "ARM";
		
		File settingsFile = new File(filePath, "arm_settings.xml");
		if (settingsFile.exists()){
		
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser(); 
			parser.setInput(new FileInputStream(settingsFile), "UTF-8");
			
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
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					
					if (tagName.equals("room")) {
						mAppState.addRoom(currentRoom);
						
						if (currentRoom.isDefault()) {
							mAppState.setDefaultRoom(currentRoom);
						}
						
						//addLineToStatus("Parsed " + currentRoom.getFullName() + ".");
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
			return true;
		}
		else
		{
			return false;
		}
		
	}

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
		else if (attrName.equals("capacity")){
			currentRoom.setCapacity(attrValue);
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
	protected ConfigurationStatus doInBackground(Void... arg0) {
		return this.configureApplication();
	}
	
	@Override
	protected void onPostExecute(ConfigurationStatus configurationStatus){
		super.onPostExecute(configurationStatus);
		this.mTaskCompletedCallback.onTaskCompleted(configurationStatus);
	}
	
	
}


