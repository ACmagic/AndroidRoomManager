package edu.cmu.sv.arm;

import java.io.File;
import java.io.FileInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Environment;
import edu.cmu.sv.arm.StartActivityController.ConfigurationStatus;

public class StartActivityController extends AsyncTask <Void, Void, ConfigurationStatus>{
	private ARM mAppState;
	// Pass files from view instead
	private AsyncTaskCompleteListener<ConfigurationStatus> mTaskCompletedCallback;
	
	public enum ConfigurationStatus {USING_CUSTOM_SETTINGS_FILE, USING_DEFAULT_SETTINGS_FILE,
		READ_MEDIA_ERROR, UNEXPECTED_ERROR}

	public StartActivityController(Application app, AsyncTaskCompleteListener<ConfigurationStatus> callback)
	{
		this.mAppState = (ARM) app;
		this.mTaskCompletedCallback = callback;
	}
	
	public void resetApplicationState(){
		if (getAppState().getRooms() != null) {
        	getAppState().getRooms().clear();
        }
        
        if (getAppState().getNumberAddressedRooms() != null) {
        	getAppState().getNumberAddressedRooms().clear();
        }
        
        if (getAppState().getResourceAddressedRooms() != null) {
        	getAppState().getResourceAddressedRooms().clear();
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

	public File getConfigurationFile(){
		String filePath = Environment.getExternalStorageDirectory() + "/" + "ARM";
		File settingsFile = new File(filePath, "arm_settings.xml");
		return settingsFile;
	}
	
	public boolean parseConfiguration() throws Exception {
		// We can read and write the media ||  We can only read the media
		File settingsFile = getConfigurationFile();
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
							currentRoom.setFullName();
						}
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (tagName.equals("room")) {
						getAppState().addRoom(currentRoom);
						
						if (currentRoom.isDefault()) {
							getAppState().setDefaultRoom(currentRoom);
						}
						
						//addLineToStatus("Parsed " + currentRoom.getFullName() + ".");
					}
					break;
				default:
					break;
				}
				
				eventType = parser.next();
			}
			
			if (getAppState().getDefaultRoom() != null) {
		    	getAppState().setCurrentRoom(getAppState().getDefaultRoom());
		    }
		    else {
		    	getAppState().setCurrentRoom(getAppState().getNumberAddressedRooms().elements().nextElement());
		    	getAppState().setDefaultRoom(getAppState().getCurrentRoom());
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
			getAppState().setTitle(attrValue);
		}							
		else if (attrName.equals("google_calendar_api_key")) {
			getAppState().setGoogleCalendarAPIKey(attrValue);
		}
		else if (attrName.equals("google_account")) {
			getAppState().setGoogleAccountName(attrValue);
		}
		else if (attrName.equals("endpoint")) {
			getAppState().setEndpoint(attrValue);
		}
		else {
			int value = Integer.parseInt(attrValue);
			if (attrName.equals("timeout_minutes")) {
				getAppState().setApplicationTimeoutMinutes(value);
			}
			else if (attrName.equals("timeout_seconds")) {
				getAppState().setApplicationTimeoutSeconds(value);
			}
			else if (attrName.equals("calendar_refresh_delay_minutes")) {
				getAppState().setCalendarRefreshDelayMinutes(value);
			}
			else if (attrName.equals("calendar_refresh_delay_seconds")) {
				getAppState().setCalendarRefreshDelaySeconds(value);
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

	public ARM getAppState() {
		return mAppState;
	}

}


