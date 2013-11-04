package edu.cmu.sv.arm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

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

public class ReserveRoomActivity extends Activity {
	private ARM mAppState;
	
	private TextView mTitleBarTextView;
	private EditText mTitleEditText;
	private Button mStartingDateButton;
	private Button mStartingTimeButton;
	private Button mEndingDateButton;
	private Button mEndingTimeButton;
	private EditText mDescriptionEditText;
	private TextView mNoAvailableRoomsTextView;
	private RadioGroup mLocationRadioGroup;
	private GuestFragment mGuestFragment;
	
	static final int START_DATE_DIALOG_ID = 0;
	static final int START_TIME_DIALOG_ID = 1;
	static final int END_DATE_DIALOG_ID = 2;
	static final int END_TIME_DIALOG_ID = 3;
	
	private Calendar mStartDateTimeCalendar;
	private Calendar mEndDateTimeCalendar;
	
	private String mSelectedRoom;
	
	private ArrayList<String> mAvailableRooms;
	
	private AvailableRoomsTask mART;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAppState = ((ARM) getApplication());
		
		setContentView(R.layout.reserve_room);
		
		setTitle(mAppState.getTitle() + " - " + getString(R.string.reserve_room_label));
		
		// Suppresses keyboard when activity starts
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		mTitleBarTextView = (TextView) findViewById(R.id.titleBarTextView);
		mTitleEditText = (EditText) findViewById(R.id.titleEditText);
		mStartingDateButton = (Button) findViewById(R.id.startingDateButton);
		mStartingTimeButton = (Button) findViewById(R.id.startingTimeButton);
		mEndingDateButton = (Button) findViewById(R.id.endingDateButton);
		mEndingTimeButton = (Button) findViewById(R.id.endingTimeButton);
		mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
		mNoAvailableRoomsTextView = (TextView) findViewById(R.id.noAvailableRoomsTextView);
		mLocationRadioGroup = (RadioGroup) findViewById(R.id.locationRadioGroup);
		mGuestFragment = (GuestFragment) getFragmentManager().findFragmentById(R.id.guestFragment);
		
		setupEditTextFocus();
		
		setupStartEndCalendars();
		
		setupInitialTime();
		
		setDateTimeButtonsAndTitleText();
		
		setupDateTimeButtonsOnClickListeners();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mAvailableRooms = new ArrayList<String>();
		
		checkForQuickReservation();
		
		updateAvailableRooms();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (mART != null) {
			mART.cancel(true);
			mART = null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.actionbar_reserve_room, menu);
	    
	    return true;
	}
	 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.reserveRoomButton:
	    	if (isValidInput()) {
	    		CalendarScheduleTask cst = new CalendarScheduleTask();
	    		
	    		cst.execute();
	    	}
	    	return true;
	    case android.R.id.home:
	    	// Intentionally left blank
	    case R.id.cancelButton:
	    	finish();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
		
		switch(id) {
		case START_DATE_DIALOG_ID:
			OnDateSetListener startDateSetListener = new OnDateSetListener() {
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar oldCalendar = Calendar.getInstance();
					
					oldCalendar.setTimeInMillis(mStartDateTimeCalendar.getTimeInMillis());
					
					mStartDateTimeCalendar.set(Calendar.MONDAY, monthOfYear);
					mStartDateTimeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					
					if (oldCalendar.getTimeInMillis() != mStartDateTimeCalendar.getTimeInMillis()) {
						cleanupDateTimeDialog(START_DATE_DIALOG_ID);
					}
				}
			};
			
			DatePickerDialog startDateDialog = new DatePickerDialog(this, startDateSetListener, mStartDateTimeCalendar.get(Calendar.YEAR),  mStartDateTimeCalendar.get(Calendar.MONTH), mStartDateTimeCalendar.get(Calendar.DAY_OF_MONTH));
			startDateDialog.setTitle(getString(R.string.set_starting_date));
			return startDateDialog;
		case START_TIME_DIALOG_ID:
			OnTimeSetListener startTimeSetListener = new OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar oldCalendar = Calendar.getInstance();
					
					oldCalendar.setTimeInMillis(mStartDateTimeCalendar.getTimeInMillis());
					
					mStartDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					mStartDateTimeCalendar.set(Calendar.MINUTE, minute);
					
					if (oldCalendar.getTimeInMillis() != mStartDateTimeCalendar.getTimeInMillis()) {
						cleanupDateTimeDialog(START_TIME_DIALOG_ID);
					}
				}
			};
			
			TimePickerDialog startTimeDialog = new TimePickerDialog(this, startTimeSetListener, mStartDateTimeCalendar.get(Calendar.HOUR_OF_DAY), mStartDateTimeCalendar.get(Calendar.MINUTE), false);
			startTimeDialog.setTitle(getString(R.string.set_starting_time));
			return startTimeDialog;
		case END_DATE_DIALOG_ID:
			OnDateSetListener endDateSetListener = new OnDateSetListener() {
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar oldCalendar = Calendar.getInstance();
					
					oldCalendar.setTimeInMillis(mEndDateTimeCalendar.getTimeInMillis());
					
					mEndDateTimeCalendar.set(Calendar.MONDAY, monthOfYear);
					mEndDateTimeCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					
					if (oldCalendar.getTimeInMillis() != mEndDateTimeCalendar.getTimeInMillis()) {
						cleanupDateTimeDialog(END_DATE_DIALOG_ID);
					}
				}
			};
			
			DatePickerDialog endDateDialog = new DatePickerDialog(this, endDateSetListener, mEndDateTimeCalendar.get(Calendar.YEAR),  mEndDateTimeCalendar.get(Calendar.MONTH), mEndDateTimeCalendar.get(Calendar.DAY_OF_MONTH));
			endDateDialog.setTitle(getString(R.string.set_ending_date));
			return endDateDialog;
		case END_TIME_DIALOG_ID:
			OnTimeSetListener endTimeSetListener = new OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar oldCalendar = Calendar.getInstance();
					
					oldCalendar.setTimeInMillis(mEndDateTimeCalendar.getTimeInMillis());
					
					mEndDateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					mEndDateTimeCalendar.set(Calendar.MINUTE, minute);
					
					if (oldCalendar.getTimeInMillis() != mEndDateTimeCalendar.getTimeInMillis()) {
						cleanupDateTimeDialog(END_TIME_DIALOG_ID);
					}
				}
			};
			
			TimePickerDialog endTimeDialog = new TimePickerDialog(this, endTimeSetListener, mEndDateTimeCalendar.get(Calendar.HOUR_OF_DAY), mEndDateTimeCalendar.get(Calendar.MINUTE), false);
			endTimeDialog.setTitle(getString(R.string.set_ending_time));
			return endTimeDialog;
		}
		
		return null;
	}
	
	@Override
    public void onUserInteraction() {
		mAppState.getMainActivity().resetApplicationResetter();
    }
	
	private void cleanupDateTimeDialog(int dialog) {
		fixDateTimes();
		
		setDateTimeButtonsAndTitleText();
		
		removeDialog(dialog);
		
		updateAvailableRooms();
	}
	
	private void setupEditTextFocus() {
		mDescriptionEditText.setNextFocusDownId(R.id.hostEmailAutoCompleteTextView);
	}
	
	private void setupStartEndCalendars() {
		mStartDateTimeCalendar = Calendar.getInstance();
		
		mStartDateTimeCalendar.set(Calendar.MILLISECOND, 0);
		mStartDateTimeCalendar.set(Calendar.SECOND, 0);
		
		mEndDateTimeCalendar = Calendar.getInstance();
		
		mEndDateTimeCalendar.set(Calendar.MILLISECOND, 0);
		mEndDateTimeCalendar.set(Calendar.SECOND, 0);
	}
	
	private void setupInitialTime() {
		Bundle extras = getIntent().getExtras();
		
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
        
        mStartDateTimeCalendar.setTimeInMillis(start.toMillis(true));
        mStartDateTimeCalendar.set(Calendar.MILLISECOND, 0);
		mStartDateTimeCalendar.set(Calendar.SECOND, 0);
		
        mEndDateTimeCalendar.setTimeInMillis(inAnHour.toMillis(true));
        mEndDateTimeCalendar.set(Calendar.MILLISECOND, 0);
		mEndDateTimeCalendar.set(Calendar.SECOND, 0);
	}
	
	private void setDateTimeButtonsAndTitleText() {
        String dateFormat = "E, MMM d, y"; // e.g. "Tue, Apr 17, 2012"
        String timeFormat = "hh:mm a"; // e.g. "08:02 PM"
        
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        
        String startingDate = sdf.format(new Date(mStartDateTimeCalendar.getTimeInMillis()));
        String endingDate = sdf.format(new Date(mEndDateTimeCalendar.getTimeInMillis()));
        
        sdf = new SimpleDateFormat(timeFormat, Locale.US);
        
        String startingTime = sdf.format(new Date(mStartDateTimeCalendar.getTimeInMillis()));
        String endingTime = sdf.format(new Date(mEndDateTimeCalendar.getTimeInMillis()));
        
        mStartingDateButton.setText(startingDate);
        mStartingTimeButton.setText(startingTime);
        mEndingDateButton.setText(endingDate);
        mEndingTimeButton.setText(endingTime);
        
        mTitleBarTextView.setText(startingDate + ", " + startingTime + "  -  " + endingDate + ", " + endingTime);
	}
	
	private void setupDateTimeButtonsOnClickListeners() {
		mStartingDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showDialog(START_DATE_DIALOG_ID);
            }
        });
        
        mStartingTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showDialog(START_TIME_DIALOG_ID);
            }
        });
        
        mEndingDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showDialog(END_DATE_DIALOG_ID);
            }
        });
        
        mEndingTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showDialog(END_TIME_DIALOG_ID);
            }
        });
	}
	
	private void fixDateTimes() {
		if (mStartDateTimeCalendar.getTimeInMillis() > mEndDateTimeCalendar.getTimeInMillis()) {
			Time inAnHour = new Time();
	        inAnHour.set(mStartDateTimeCalendar.getTimeInMillis() + DateTimeHelpers.HOUR_IN_MILLISECONDS);
	        inAnHour = DateTimeHelpers.nearestFifteenMinutes(inAnHour);
	        
	        mEndDateTimeCalendar.setTimeInMillis(inAnHour.toMillis(true));
	        mEndDateTimeCalendar.set(Calendar.MILLISECOND, 0);
			mEndDateTimeCalendar.set(Calendar.SECOND, 0);
		}
	}
	
	private boolean isValidInput() {
		boolean isValid = true;
		String error = "";
		
		if (mTitleEditText.getText() == null || mTitleEditText.getText().toString().length() < 4) {
			isValid = false;
			error = getString(R.string.event_title_error);
		}
		
		if (isValid) {
			if (mSelectedRoom == null || mSelectedRoom.isEmpty()) {
				isValid = false;
				error = getString(R.string.room_selection_error);
			}
			
			if (isValid) {
				if (mGuestFragment.getHostEmail() == null || mGuestFragment.getHostEmail().isEmpty() || !PatternChecker.isValidEmail(mGuestFragment.getHostEmail())) {
					isValid = false;
					error = getString(R.string.host_email_error);
				}
			}
		}
		
		if (!isValid) {
			AlertDialogHelper.buildAlertDialog(ReserveRoomActivity.this, getString(R.string.error), error, getString(R.string.ok));
		}
		
		return isValid;
	}
	
	private void updateAvailableRooms() {
		if (mART != null) {
			mART.cancel(true);
			mART = null;
		}
		
		mART = new AvailableRoomsTask();
   	
		mART.execute();
    }
	
	private class AvailableRoomsTask extends AsyncTask<Void, Void, ArrayList<String>> {
		private ProgressDialog mDialog;
		
		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			if (mAppState.getCalendar() == null) {
				return null;
			}
			
			try {
				FreeBusyRequest request = new FreeBusyRequest();
				
				request.setTimeMin(new DateTime(new Date(mStartDateTimeCalendar.getTimeInMillis()), TimeZone.getTimeZone("UTC")));
				request.setTimeMax(new DateTime(new Date(mEndDateTimeCalendar.getTimeInMillis()), TimeZone.getTimeZone("UTC")));
				
				//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mAppState);
				
				ArrayList<FreeBusyRequestItem> fbri_list = new ArrayList<FreeBusyRequestItem>();
				
				for(String room : mAppState.getRooms()) {
					fbri_list.add(new FreeBusyRequestItem().setId(mAppState.getNumberAddressedRooms().get(room).getResourceAddress()));
				}
				
				request.setItems(fbri_list);
				
				FreeBusyResponse busyTimes = mAppState.getCalendar().freebusy().query(request).execute();
				
				ArrayList<String> freeRooms = new ArrayList<String>();
				
				for (Map.Entry<String, FreeBusyCalendar> busyCalendar : busyTimes.getCalendars().entrySet()) {
					String room = busyCalendar.getKey();
					
					if (busyCalendar.getValue().getBusy().size() == 0) {
						freeRooms.add(mAppState.getResourceAddressedRooms().get(room).getFullName());
					}
				}
				
				return freeRooms;
			}
			catch (Exception e) {
				// TODO Intentionally left blank, but may need to handle errors later
			}
			
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			mDialog = new ProgressDialog(ReserveRoomActivity.this);
    		mDialog.setMessage(getString(R.string.obtaining_available_rooms_please_wait));
    		mDialog.show();
		}
		
		@Override
		protected void onPostExecute(ArrayList<String> result) {
			super.onPostExecute(result);
			
			if (mDialog.isShowing()) {
    			mDialog.dismiss();
    		}
			
			mAvailableRooms.clear();
        	if (result != null) {
        		mAvailableRooms.addAll(result);
        		
        		if (!(mAvailableRooms.contains(mSelectedRoom))) {
        			mSelectedRoom = null;
        		}
        	}
        	
        	ReserveRoomActivity.this.runOnUiThread(new Runnable() {
				public void run() {	        	
		        	mLocationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
						public void onCheckedChanged(RadioGroup group, int checkedId) {
							mSelectedRoom = mAvailableRooms.get(checkedId);
						}
					});
		        	
		        	mLocationRadioGroup.removeAllViews();
		        	
		        	if (mAvailableRooms.size() == 0) {
		        		mNoAvailableRoomsTextView.setVisibility(android.view.View.VISIBLE);
		        	}
		        	else {
		        		mNoAvailableRoomsTextView.setVisibility(android.view.View.INVISIBLE);
		        	}
		        	
		        	
		        	LinearLayout.LayoutParams layoutParams = new RadioGroup.LayoutParams(
		                    LayoutParams.WRAP_CONTENT,
		                    LayoutParams.WRAP_CONTENT);
		        	
		        	for (int i = 0; i < mAvailableRooms.size(); i++) {
		        		RadioButton rb = new RadioButton(ReserveRoomActivity.this);
		        		
		        		rb.setText(mAvailableRooms.get(i));
		        		rb.setId(i);
		        		rb.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 18);
		        		
		        		if (mAvailableRooms.get(i).equals(mSelectedRoom)) {
		        			rb.setChecked(true);
		        		}
		        		
		        		mLocationRadioGroup.addView(rb, layoutParams);
		        	}
		        	
		        	selectRoomInReservation();
				}
			});
		}
	}
	
	private class CalendarScheduleTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog mDialog;
		private Event mEvent;
		
		// attempts to schedule a calendar event
    	// Arguments:
    	// [0] - event title
    	// [1] - event starting date
    	// [2] - event starting time
    	// [3] - event ending date
    	// [4] - event ending time
        // [5] - host/creator/organizer
    	// [6] - description
    	// [7] - guests can modify event?
        // [8] - guests can invite others?
        // [9] - guests can see guest list?
    	// [10] - room/location
    	// [11...] - guests
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
	    		if (mAppState.getCalendar() == null) {
	    			throw new Exception();
	    		}
 	    	   	
 	    	   	mEvent = new Event();
 	    	   	
 	    	   	mEvent.setSummary(mTitleEditText.getText().toString());
	    	   	
	    	   	Date startDate = new Date(mStartDateTimeCalendar.getTimeInMillis());
	    	   	
	 	    	Date endDate = new Date(mEndDateTimeCalendar.getTimeInMillis());
	 	    	
	 	    	mEvent.setStart(new EventDateTime().setDateTime(new DateTime(startDate, TimeZone.getTimeZone("UTC"))));
	 	    	mEvent.setEnd(new EventDateTime().setDateTime(new DateTime(endDate, TimeZone.getTimeZone("UTC"))));
 	    	   	
	 	    	// Argument 5 (owner/creator) moved below
	 	    	
 	    	   	mEvent.setDescription(mDescriptionEditText.getText().toString());

 	    	   	mEvent.setGuestsCanModify(true);
 	    	   	mEvent.setGuestsCanInviteOthers(true);
	   			mEvent.setGuestsCanSeeOtherGuests(true);
 	    	   
 	    	   	LinkedList<EventAttendee> attendees = new LinkedList<EventAttendee>();
 	    	   	//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mAppState);
 	    	   	

 	    	   	mEvent.setLocation(mSelectedRoom);
	    	   	attendees.add(new EventAttendee().setEmail(mAppState.getNumberAddressedRooms().get(mSelectedRoom).getResourceAddress()));
 	    	   	
    	    	EventAttendee creator = new EventAttendee();
    	    	creator.setEmail(mGuestFragment.getHostEmail());
    	    	creator.setResponseStatus("needsAction");
    	    	
    	   		attendees.add(creator);
    	   		
    	   		mEvent.setCreator(new EventCreator().setEmail(mGuestFragment.getHostEmail()));
    	   		mEvent.setOrganizer(new EventOrganizer().setEmail(mGuestFragment.getHostEmail()));
 	    	   	
 	    	   	// Add the rest of the attendees
 	    	   	for (int i = 0; i < mGuestFragment.getGuests().size(); i++) {
 	    	   		EventAttendee attendee = new EventAttendee();
 	    	   		attendee.setEmail(mGuestFragment.getGuests().get(i));
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
	 	    	   		createdEvent = mAppState.getCalendar().events().insert("primary", mEvent).execute();
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
    	protected void onPreExecute() {
    		super.onPreExecute();
    		
    		mDialog = new ProgressDialog(ReserveRoomActivity.this);
    		mDialog.setMessage(getString(R.string.reserving_room_please_wait));
    		mDialog.show();
    	}
		
		@Override
    	protected void onPostExecute(Boolean result) {
    		super.onPostExecute(result);
		    
		    if (mDialog.isShowing()) {
    			mDialog.dismiss();
    		}
		    
		    if (result != null && result) {
		    	ScheduledEventVerifier sev = new ScheduledEventVerifier();
		    	
		    	sev.execute(mEvent);
		    }
		    else {
		    	AlertDialogHelper.buildAlertDialog(ReserveRoomActivity.this, getString(R.string.error), getString(R.string.there_has_been_an_exception), getString(R.string.ok));
		    }
    	}
	}
	
	private class ScheduledEventVerifier extends AsyncTask<Event, Void, Boolean> {
		private ProgressDialog mDialog;
		private Event mEvent;
		
		@Override
		protected Boolean doInBackground(Event... params) {
			try {
	    		if (mAppState.getCalendar() == null) {
	    			throw new Exception();
	    		}
	    		
	    		if (params[0] != null) {
	    			mEvent = params[0];
	    		}
	    		else {
					return false;
	    		}
 	    	   	
		    	FreeBusyRequest request = new FreeBusyRequest();
				
				request.setTimeMin(mEvent.getStart().getDateTime());
				request.setTimeMax(mEvent.getEnd().getDateTime());
				
				//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mAppState);
				
				request.setItems(Arrays.asList(
					    new FreeBusyRequestItem().setId(mAppState.getNumberAddressedRooms().get(mEvent.getLocation()).getResourceAddress())));
				
				FreeBusyResponse busyTimes;
				
				busyTimes = mAppState.getCalendar().freebusy().query(request).execute();
				
				ArrayList<String> freeRooms = new ArrayList<String>();
				
				for (Map.Entry<String, FreeBusyCalendar> busyCalendar : busyTimes.getCalendars().entrySet()) {
					String room = busyCalendar.getKey();
					
					if (busyCalendar.getValue().getBusy().size() == 0) {
						freeRooms.add(mAppState.getResourceAddressedRooms().get(room).getFullName());
					}
				}
				
				if (freeRooms.isEmpty()) {
					Event event = null;
					
					try {
						event = mAppState.getCalendar().events().get("primary", mEvent.getId()).execute();
						
						if (event != null && event.getSummary().equals(mEvent.getSummary())) {
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
		
		@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		
    		mDialog = new ProgressDialog(ReserveRoomActivity.this);
    		mDialog.setMessage(getString(R.string.validating_reservation));
    		mDialog.show();
    	}
		
		@Override
    	protected void onPostExecute(Boolean result) {
    		super.onPostExecute(result);
		    
		    if (mDialog.isShowing()) {
    			mDialog.dismiss();
    		}
		    
		    AlertDialog.Builder alert = new AlertDialog.Builder(ReserveRoomActivity.this);
		    
		    if (result == null) {
		    	alert.setTitle(getString(R.string.error));
		    	alert.setMessage(getString(R.string.unable_to_verify_reservation));
		    	alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ReserveRoomActivity.this.finish();
					}
				});
		    	alert.show();
		    }
		    else if (result) {
		    	alert.setTitle(getString(R.string.notice));
		    	alert.setMessage(getString(R.string.room_reserved));
		    	alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ReserveRoomActivity.this.finish();
						mAppState.getMainActivity().restartCalendarFragmentEventsUpdater();
					}
				});
		    	alert.show();
		    }
		    else {
		    	AlertDialogHelper.buildAlertDialog(ReserveRoomActivity.this, getString(R.string.error), getString(R.string.reservation_failed), getString(R.string.ok));
		    }
    	}
	}
	
	private void checkForQuickReservation() {
		Bundle extras = getIntent().getExtras(); 
		
		if(extras != null && extras.containsKey("quickReservation")) {
			boolean quickReservation = extras.getBoolean("quickReservation");
			
			if (quickReservation) {
				//setTitle(getString(R.string.quick_reservation_label));
				setTitle(mAppState.getTitle() + " - " + getString(R.string.quick_reservation_label));
				
				mTitleEditText.setText(getString(R.string.quick_reservation));
				//mTitleEditText.setEnabled(false);
				
				mDescriptionEditText.setText(getString(R.string.reserved_via_arm));
				//mDescriptionEditText.setEnabled(false);
				
				//mStartingDateButton.setEnabled(false);
				//mStartingTimeButton.setEnabled(false);
				
				//mEndingDateButton.setEnabled(false);
				//mEndingTimeButton.setEnabled(false);
				
				mGuestFragment.setFocusOnHostEmail();
			}
		}
	}
	
	private void selectRoomInReservation() {
		Bundle extras = getIntent().getExtras(); 
		
		if(extras != null && extras.containsKey("selectedRoom")) {
			mSelectedRoom = extras.getString("selectedRoom");
			
			int radioSelection = -1;
			
			for (int i = 0; i < mAvailableRooms.size(); i++) {
				if (mAvailableRooms.get(i).equals(mSelectedRoom)) {
					radioSelection = i;
					break;
				}
			}
			
			if (radioSelection != -1) {
				mLocationRadioGroup.check(radioSelection);
			}
			else {
				mSelectedRoom = null;
			}
		}
	}
}
