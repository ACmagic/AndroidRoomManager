package edu.cmu.sv.arm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventCreator;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventOrganizer;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;

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

public class ReserveRoomController extends AsyncTask <Object, Void, Boolean>{
	private ARM mAppState; 
	private AsyncTaskCompleteListener<Boolean> mTaskCompletedCallback;
	private Event mEvent;
	
	
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
	
	public ReserveRoomController(Application app, AsyncTaskCompleteListener<Boolean> callback)
	{
		this.mAppState = ((ARM) app);
		this.mTaskCompletedCallback = callback;
		setupStartEndCalendars();
	}
	
	public ARM getApplicationState() {
		return mAppState;
	}
	
	@Override
	protected Boolean doInBackground(Object... params) {
		try {
			if (getApplicationState().getCalendar() == null) {
				throw new Exception();
			}
	    	   	
			mEvent = new Event();
			
			mEvent.setSummary((String)params[0]);
			Date startDate = new Date(getmStartDateTimeCalendar().getTimeInMillis());
			Date endDate = new Date(getmEndDateTimeCalendar().getTimeInMillis());
			mEvent.setStart(new EventDateTime().setDateTime(new DateTime(startDate, TimeZone.getTimeZone("UTC"))));
			mEvent.setEnd(new EventDateTime().setDateTime(new DateTime(endDate, TimeZone.getTimeZone("UTC"))));
			mEvent.setDescription((String)params[1]);
			mEvent.setGuestsCanModify(true);
			mEvent.setGuestsCanInviteOthers(true);
			mEvent.setGuestsCanSeeOtherGuests(true);
			LinkedList<EventAttendee> attendees = new LinkedList<EventAttendee>();
			mEvent.setLocation((String)params[2]);
			attendees.add(new EventAttendee().setEmail(getApplicationState().getNumberAddressedRooms().get(params[2]).getResourceAddress()));
			
			EventAttendee creator = new EventAttendee();
			creator.setEmail((String)params[3]);
			creator.setResponseStatus("needsAction");
			attendees.add(creator);
			
			mEvent.setCreator(new EventCreator().setEmail((String)params[3]));
			mEvent.setOrganizer(new EventOrganizer().setEmail((String)params[3]));
			
			ArrayList<String> guests = (ArrayList<String>) params[4];
			// Add the rest of the attendees
			for (int i = 0; i < guests.size(); i++) {
				EventAttendee attendee = new EventAttendee();
				attendee.setEmail(guests.get(i));
				attendee.setResponseStatus("needsAction");
				
				attendees.add(attendee);
			}
			
			mEvent.setAttendees(attendees);
			
			mEvent.set("sendNotifications", true);
			
			Event createdEvent = new Event();
			createdEvent.clear();
			
			// Attempt to create the event 5 times
			for (int tries = 0; tries < 5; tries++) {
				try {
					createdEvent = getApplicationState().getCalendar().events().insert("primary", mEvent).execute();
					mEvent = createdEvent;
					return true;
				}
				catch (GoogleJsonResponseException e) {
					if (e.getDetails().code == 503) {
						continue;
					}
					break;
				}
			}	
			return false;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
	    
	    if (result != null && result) {
	    	result = verifyScheduledEvent(mEvent);
	    }
		this.mTaskCompletedCallback.onTaskCompleted(result);
	}
	
	public Boolean verifyScheduledEvent(Event event){
		try {
    		if(event == null){
				return false;
    		}
	    	   	
	    	FreeBusyRequest request = new FreeBusyRequest();
			
			request.setTimeMin(event.getStart().getDateTime());
			request.setTimeMax(event.getEnd().getDateTime());
						
			request.setItems(Arrays.asList(
				    new FreeBusyRequestItem().setId(getApplicationState().getNumberAddressedRooms(
				    		).get(event.getLocation()).getResourceAddress())));			
			FreeBusyResponse busyTimes;
			busyTimes = getApplicationState().getCalendar().freebusy().query(request).execute();
			ArrayList<String> freeRooms = new ArrayList<String>();
			
			for (Map.Entry<String, FreeBusyCalendar> busyCalendar : busyTimes.getCalendars().entrySet()) {
				String room = busyCalendar.getKey();
				
				if (busyCalendar.getValue().getBusy().size() == 0) {
					freeRooms.add(getApplicationState().getResourceAddressedRooms().get(room).getFullName());
				}
			}
			
			if (freeRooms.isEmpty()) {
				Event executeEvent = null;
				
				try {
					executeEvent = getApplicationState().getCalendar().events().get(
							"primary", event.getId()).execute();
					
					if (executeEvent != null && executeEvent.getSummary().equals(event.getSummary())) {
						return true;
					}
				}
				catch (Exception e) {
					// OK to do nothing here
				}
			}
			return null;	// TODO: this should be false, but need to make sure that it is actually triggered whenever necessary
    	}
		catch (Exception e) {
			return null;
		}
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
	
	public ArrayList<Room> getFreeRooms(){
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
			
			ArrayList<Room> freeRooms = new ArrayList<Room>();
			
			for (Map.Entry<String, FreeBusyCalendar> busyCalendar : busyTimes.getCalendars().entrySet()) {
				String room = busyCalendar.getKey();
				
				if (busyCalendar.getValue().getBusy().size() == 0) {
					freeRooms.add(getApplicationState().getResourceAddressedRooms().get(room));
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
