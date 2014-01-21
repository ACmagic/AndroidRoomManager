package edu.cmu.sv.arm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.text.Editable;

enum validationResult {
	VALIDATION_SUCCESS,
	EVENT_TITLE_VALIDATION_FAILURE,
	EVENT_ROOM_VALIDATION_FAILURE,
	EVENT_EMAIL_VALIDATION_FAILURE
}

public class ReserveRoomController extends AsyncTask <Void, Void, Void>{
	private ARM mAppState; 
	private AsyncTaskCompleteListener<Void> mTaskCompletedCallback;
	
	private Calendar mStartDateTimeCalendar;
	private Calendar mEndDateTimeCalendar;
	
	public void setupStartEndCalendars() {
		setmStartDateTimeCalendar(Calendar.getInstance());
		
		getmStartDateTimeCalendar().set(Calendar.MILLISECOND, 0);
		getmStartDateTimeCalendar().set(Calendar.SECOND, 0);
		
		setmEndDateTimeCalendar(Calendar.getInstance());
		
		getmEndDateTimeCalendar().set(Calendar.MILLISECOND, 0);
		getmEndDateTimeCalendar().set(Calendar.SECOND, 0);
	}
	
	public ReserveRoomController(Application app, AsyncTaskCompleteListener<Void> callback)
	{
		this.mAppState = ((ARM) app);
		this.mTaskCompletedCallback = callback;
		setupStartEndCalendars();
	}
	
	public ARM getApplicationState() {
		return mAppState;
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Calendar getmStartDateTimeCalendar() {
		return mStartDateTimeCalendar;
	}

	private void setmStartDateTimeCalendar(Calendar mStartDateTimeCalendar) {
		this.mStartDateTimeCalendar = mStartDateTimeCalendar;
	}

	public Calendar getmEndDateTimeCalendar() {
		return mEndDateTimeCalendar;
	}

	private void setmEndDateTimeCalendar(Calendar mEndDateTimeCalendar) {
		this.mEndDateTimeCalendar = mEndDateTimeCalendar;
	}
	
	public void setupInitialTime(Bundle extras) {		
		Time start = new Time();
        start.setToNow();
		
		if (extras != null && extras.containsKey("eventTime")) {
			Calendar cal = Calendar.getInstance();
			
			// year
			// month
			// day_of_month
			// hour
			// minute
			int [] eventTime = extras.getIntArray("eventTime");
			
			cal.set(Calendar.YEAR, eventTime[0]);
			cal.set(Calendar.MONTH, eventTime[1]);
			cal.set(Calendar.DAY_OF_MONTH, eventTime[2]);
			cal.set(Calendar.HOUR_OF_DAY, eventTime[3]);
			cal.set(Calendar.MINUTE, eventTime[4]);
			
			start.set(cal.getTimeInMillis());
		}
		
        Time inAnHour = new Time();
        inAnHour.set(start.toMillis(true) + DateTimeHelpers.HOUR_IN_MILLISECONDS);
        inAnHour = DateTimeHelpers.nearestFifteenMinutes(inAnHour);
        
        getmStartDateTimeCalendar().setTimeInMillis(start.toMillis(true));
        getmStartDateTimeCalendar().set(Calendar.MILLISECOND, 0);
		getmStartDateTimeCalendar().set(Calendar.SECOND, 0);
		
        getmEndDateTimeCalendar().setTimeInMillis(inAnHour.toMillis(true));
        getmEndDateTimeCalendar().set(Calendar.MILLISECOND, 0);
		getmEndDateTimeCalendar().set(Calendar.SECOND, 0);
	}
	
	public ArrayList<String> getFreeRooms(){
		try {
			FreeBusyRequest request = new FreeBusyRequest();
			
			request.setTimeMin(new DateTime(new Date(getmStartDateTimeCalendar().getTimeInMillis()), TimeZone.getTimeZone("UTC")));
			request.setTimeMax(new DateTime(new Date(getmEndDateTimeCalendar().getTimeInMillis()), TimeZone.getTimeZone("UTC")));
			
			//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mAppState);
			
			ArrayList<FreeBusyRequestItem> fbri_list = new ArrayList<FreeBusyRequestItem>();
			
			for(String room : getApplicationState().getRooms()) {
				fbri_list.add(new FreeBusyRequestItem().setId(getApplicationState().getNumberAddressedRooms().get(room).getResourceAddress()));
			}
			
			request.setItems(fbri_list);
			
			FreeBusyResponse busyTimes = getApplicationState().getCalendar().freebusy().query(request).execute();
			
			ArrayList<String> freeRooms = new ArrayList<String>();
			
			for (Map.Entry<String, FreeBusyCalendar> busyCalendar : busyTimes.getCalendars().entrySet()) {
				String room = busyCalendar.getKey();
				
				if (busyCalendar.getValue().getBusy().size() == 0) {
					freeRooms.add(getApplicationState().getResourceAddressedRooms().get(room).getFullName());
				}
			}
			
			return freeRooms;
		}
		catch (Exception e) {
			// TODO Intentionally left blank, but may need to handle errors later
		}
		return null;
	}
	
	// check if just return (condition) works well to refactor.
	public validationResult isReservationInfoValid(Editable reservationTitle, String selectedRoom, String email){
		if(reservationTitle == null || reservationTitle.toString().length() < 4){
			return validationResult.EVENT_TITLE_VALIDATION_FAILURE;
		}
		else if(selectedRoom == null || selectedRoom.isEmpty()){
			return validationResult.EVENT_ROOM_VALIDATION_FAILURE;
		}
		else if(email == null || email.isEmpty() || !PatternChecker.isValidEmail(email)){
			return validationResult.EVENT_EMAIL_VALIDATION_FAILURE;
		}
		else{
			return validationResult.VALIDATION_SUCCESS;
		}
	}
}
