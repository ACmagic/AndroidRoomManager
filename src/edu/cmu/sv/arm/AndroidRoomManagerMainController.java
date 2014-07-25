package edu.cmu.sv.arm;

import java.util.Vector;

import android.content.Context;
import android.os.AsyncTask;


@SuppressWarnings("rawtypes") // Suppressing warning due to class listening to more than one async task.
public class AndroidRoomManagerMainController extends AsyncTask <Void, Void, Void> implements AsyncTaskCompleteListener {
	private ARM mAppState;
	private AsyncTaskCompleteListener<Vector<String>> mCallback;
	private CalendarProvider mCalendar = null;
	private ContactsProvider mContactsProvider;

	@SuppressWarnings("unchecked")
	public AndroidRoomManagerMainController(AndroidRoomManagerMainActivity app, String appName) {
		mAppState = ((ARM) app.getApplication());
		mAppState.setMainActivity(app);
		mCallback = app;
		mCalendar = new CalendarProvider(app, mAppState, appName);
		updateContacts(app);
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
	@SuppressWarnings("unchecked")
	public void updateContacts(Context context) {
		if (mContactsProvider != null) {
			mContactsProvider.cancel(true);
			mContactsProvider = null;
		}

		mContactsProvider = new ContactsProvider((AsyncTaskCompleteListener<Vector<String>>)this);
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

	@SuppressWarnings("unchecked")
	public void onTaskCompleted(Object result) {
		// Listener for contacts
		if (result instanceof Vector<?>){
			mAppState.setContacts((Vector<String>) result);
			mCallback.onTaskCompleted((Vector<String>)result);
		}
	}
}
