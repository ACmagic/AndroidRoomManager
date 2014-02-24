package edu.cmu.sv.arm;

import java.util.Vector;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

public class AndroidRoomManagerMainController extends AsyncTask <Void, Void, Void> implements AsyncTaskCompleteListener {
	private ARM mAppState;
	private AsyncTaskCompleteListener mCallback;
	//private AsyncTaskCompleteListener<Vector<String>> mContactsCallback;
	private CalendarProvider mCalendar = null;
	private ContactsProvider mContactsProvider;
	
	public AndroidRoomManagerMainController(AndroidRoomManagerMainActivity app, String appName) {
		mAppState = ((ARM) app.getApplication());
		mAppState.setMainActivity(app);
		mCallback = app;
		mCalendar = new CalendarProvider(app, this, mAppState, appName);	
	}
	
	public void configureCalendarRunnable(){
		mCalendar.configureRunnable();
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		mCalendar.execute();
		return null;
	}
	
	public int getDefaultRoom(){
		int navigationItemIndex = 0;
		
		Room defaultRoom = this.getApplicationState().getDefaultRoom();
		
		if (defaultRoom != null) {
	    	String defaultRoomName = defaultRoom.getFullName();
			for (int i = 0; i < this.getApplicationState().getRooms().size(); i++) {
				if (this.getApplicationState().getRooms().get(i).equals(defaultRoomName)) {
					navigationItemIndex = i;
					break;
				}
			}
		}
		return navigationItemIndex;
	}
	
	// Update contacts
    public void updateContacts(Context context) {
    	if (mContactsProvider != null) {
    		mContactsProvider.cancel(true);
    		mContactsProvider = null;
    	}
    	
    	mContactsProvider = new ContactsProvider(this);
    	mContactsProvider.execute(context);
    }
    
	
	public void stopResources(){
		this.mCalendar.stopResources();
		
		if (mContactsProvider != null) {
    		mContactsProvider.cancel(true);
    		mContactsProvider = null;
    	}
    	
	}

	public ARM getApplicationState() {
		return mAppState;
	}

	public void onTaskCompleted(Object result) {
		// Listener for contacts
		if (result instanceof Vector<?>){
			mAppState.setContacts((Vector<String>) result);
			mCallback.onTaskCompleted((Vector<String>)result);
		}
		
		//Listener for calendar events
		else if (result instanceof Void){
			
		}
			
		
	}
}
