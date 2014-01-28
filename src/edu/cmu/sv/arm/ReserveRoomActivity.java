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

public class ReserveRoomActivity extends Activity implements AsyncTaskCompleteListener<Boolean> {	
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
	
	private String mSelectedRoom;
	
	private ArrayList<String> mAvailableRooms;
	
	private AvailableRoomsTask mART;
	private ReserveRoomController mController;
	private ProgressDialog mDialog;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mController = new ReserveRoomController (this.getApplication(), this);
		
		setContentView(R.layout.reserve_room);
		
		setTitle(this.mController.getApplicationState().getTitle() + " - " + getString(R.string.reserve_room_label));
		
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
		
		this.mController.setupInitialTime(getIntent().getExtras());
		
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
	    		mDialog = new ProgressDialog(ReserveRoomActivity.this);
	    		mDialog.setMessage(getString(R.string.reserving_room_please_wait));
	    		mDialog.show();
	    		
	    		mController.execute(mTitleEditText.getText().toString(), mDescriptionEditText.getText().toString(),
	    				mSelectedRoom, mGuestFragment.getHostEmail(), mGuestFragment.getGuests());
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
					
					oldCalendar.setTimeInMillis(mController.getmStartDateTimeCalendar().getTimeInMillis());
					
					mController.getmStartDateTimeCalendar().set(Calendar.MONDAY, monthOfYear);
					mController.getmStartDateTimeCalendar().set(Calendar.DAY_OF_MONTH, dayOfMonth);
					
					if (oldCalendar.getTimeInMillis() != mController.getmStartDateTimeCalendar().getTimeInMillis()) {
						cleanupDateTimeDialog(START_DATE_DIALOG_ID);
					}
				}
			};
			
			DatePickerDialog startDateDialog = new DatePickerDialog(this, startDateSetListener, mController.getmStartDateTimeCalendar().get(Calendar.YEAR),  mController.getmStartDateTimeCalendar().get(Calendar.MONTH), mController.getmStartDateTimeCalendar().get(Calendar.DAY_OF_MONTH));
			startDateDialog.setTitle(getString(R.string.set_starting_date));
			return startDateDialog;
		case START_TIME_DIALOG_ID:
			OnTimeSetListener startTimeSetListener = new OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar oldCalendar = Calendar.getInstance();
					
					oldCalendar.setTimeInMillis(mController.getmStartDateTimeCalendar().getTimeInMillis());
					
					mController.getmStartDateTimeCalendar().set(Calendar.HOUR_OF_DAY, hourOfDay);
					mController.getmStartDateTimeCalendar().set(Calendar.MINUTE, minute);
					
					if (oldCalendar.getTimeInMillis() != mController.getmStartDateTimeCalendar().getTimeInMillis()) {
						cleanupDateTimeDialog(START_TIME_DIALOG_ID);
					}
				}
			};
			
			TimePickerDialog startTimeDialog = new TimePickerDialog(this, startTimeSetListener, mController.getmStartDateTimeCalendar().get(Calendar.HOUR_OF_DAY), mController.getmStartDateTimeCalendar().get(Calendar.MINUTE), false);
			startTimeDialog.setTitle(getString(R.string.set_starting_time));
			return startTimeDialog;
		case END_DATE_DIALOG_ID:
			OnDateSetListener endDateSetListener = new OnDateSetListener() {
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar oldCalendar = Calendar.getInstance();
					
					oldCalendar.setTimeInMillis(mController.getmEndDateTimeCalendar().getTimeInMillis());
					
					mController.getmEndDateTimeCalendar().set(Calendar.MONDAY, monthOfYear);
					mController.getmEndDateTimeCalendar().set(Calendar.DAY_OF_MONTH, dayOfMonth);
					
					if (oldCalendar.getTimeInMillis() != mController.getmEndDateTimeCalendar().getTimeInMillis()) {
						cleanupDateTimeDialog(END_DATE_DIALOG_ID);
					}
				}
			};
			
			DatePickerDialog endDateDialog = new DatePickerDialog(this, endDateSetListener, mController.getmEndDateTimeCalendar().get(Calendar.YEAR),  mController.getmEndDateTimeCalendar().get(Calendar.MONTH), mController.getmEndDateTimeCalendar().get(Calendar.DAY_OF_MONTH));
			endDateDialog.setTitle(getString(R.string.set_ending_date));
			return endDateDialog;
		case END_TIME_DIALOG_ID:
			OnTimeSetListener endTimeSetListener = new OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar oldCalendar = Calendar.getInstance();
					
					oldCalendar.setTimeInMillis(mController.getmEndDateTimeCalendar().getTimeInMillis());
					
					mController.getmEndDateTimeCalendar().set(Calendar.HOUR_OF_DAY, hourOfDay);
					mController.getmEndDateTimeCalendar().set(Calendar.MINUTE, minute);
					
					if (oldCalendar.getTimeInMillis() != mController.getmEndDateTimeCalendar().getTimeInMillis()) {
						cleanupDateTimeDialog(END_TIME_DIALOG_ID);
					}
				}
			};
			
			TimePickerDialog endTimeDialog = new TimePickerDialog(this, endTimeSetListener, mController.getmEndDateTimeCalendar().get(Calendar.HOUR_OF_DAY), mController.getmEndDateTimeCalendar().get(Calendar.MINUTE), false);
			endTimeDialog.setTitle(getString(R.string.set_ending_time));
			return endTimeDialog;
		}
		
		return null;
	}
	
	@Override
    public void onUserInteraction() {
		this.mController.getApplicationState().getMainActivity().resetApplicationResetter();
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
	
	private void setDateTimeButtonsAndTitleText() {
        String dateFormat = "E, MMM d, y"; // e.g. "Tue, Apr 17, 2012"
        String timeFormat = "hh:mm a"; // e.g. "08:02 PM"
        
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        
        String startingDate = sdf.format(new Date(mController.getmStartDateTimeCalendar().getTimeInMillis()));
        String endingDate = sdf.format(new Date(mController.getmEndDateTimeCalendar().getTimeInMillis()));
        
        sdf = new SimpleDateFormat(timeFormat, Locale.US);
        
        String startingTime = sdf.format(new Date(mController.getmStartDateTimeCalendar().getTimeInMillis()));
        String endingTime = sdf.format(new Date(mController.getmEndDateTimeCalendar().getTimeInMillis()));
        
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
		if (mController.getmStartDateTimeCalendar().getTimeInMillis() > mController.getmEndDateTimeCalendar().getTimeInMillis()) {
			Time inAnHour = new Time();
	        inAnHour.set(mController.getmStartDateTimeCalendar().getTimeInMillis() + DateTimeHelpers.HOUR_IN_MILLISECONDS);
	        inAnHour = DateTimeHelpers.nearestFifteenMinutes(inAnHour);
	        
	        mController.getmEndDateTimeCalendar().setTimeInMillis(inAnHour.toMillis(true));
	        mController.getmEndDateTimeCalendar().set(Calendar.MILLISECOND, 0);
			mController.getmEndDateTimeCalendar().set(Calendar.SECOND, 0);
		}
	}
	
	private boolean isValidInput() {
		boolean isValid = false;
		validationResult eventValidation = mController.isReservationInfoValid(mTitleEditText.getText(),
				mSelectedRoom, mGuestFragment.getHostEmail());
		switch (eventValidation){
			case VALIDATION_SUCCESS:
				isValid = true;
				break;
			case EVENT_TITLE_VALIDATION_FAILURE:
				AlertDialogHelper.buildAlertDialog(ReserveRoomActivity.this, getString(R.string.error), 
						getString(R.string.event_title_error), getString(R.string.ok));
				break;
			case EVENT_ROOM_VALIDATION_FAILURE:
				AlertDialogHelper.buildAlertDialog(ReserveRoomActivity.this, getString(R.string.error), 
						getString(R.string.room_selection_error), getString(R.string.ok));
				break;
			case EVENT_EMAIL_VALIDATION_FAILURE:
				AlertDialogHelper.buildAlertDialog(ReserveRoomActivity.this, getString(R.string.error), 
						getString(R.string.host_email_error), getString(R.string.ok));
				break;
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
			if (mController.getApplicationState().getCalendar() == null) {
				return null;
			}
			return mController.getFreeRooms();
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
	
	private void checkForQuickReservation() {
		Bundle extras = getIntent().getExtras(); 
		
		if(extras != null && extras.containsKey("quickReservation")) {
			boolean quickReservation = extras.getBoolean("quickReservation");
			
			if (quickReservation) {
				setTitle(this.mController.getApplicationState().getTitle() + " - " + getString(R.string.quick_reservation_label));
				mTitleEditText.setText(getString(R.string.quick_reservation));				
				mDescriptionEditText.setText(getString(R.string.reserved_via_arm));
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

	public void onTaskCompleted(Boolean result) {
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
					mController.getApplicationState().getMainActivity().restartCalendarFragmentEventsUpdater();
				}
			});
	    	alert.show();
	    }
	    else {
	    	AlertDialogHelper.buildAlertDialog(ReserveRoomActivity.this, getString(R.string.error), getString(R.string.reservation_failed), getString(R.string.ok));
	    }
	}
}
