package edu.cmu.sv.arm;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;

public class ReserveRoomActivity extends Activity implements AsyncTaskCompleteListener<Boolean> {	
	private TextView mTitleBarTextView;
	private EditText mTitleEditText;
	private Button mStartingDateButton;
	private Button mStartingTimeButton;
	private Button mEndingDateButton;
	private Button mEndingTimeButton;
	private EditText mDescriptionEditText;
	private Spinner mLocationSpinner;
	private GuestFragment mGuestFragment;
	private RoomInfoFragment mRoomInfoFragment; 
	
	static final int START_DATE_DIALOG_ID = 0;
	static final int START_TIME_DIALOG_ID = 1;
	static final int END_DATE_DIALOG_ID = 2;
	static final int END_TIME_DIALOG_ID = 3;
	
	private String mSelectedRoom;
	
	private ArrayList<Room> mAvailableRooms;
	
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
		//mNoAvailableRoomsTextView = (TextView) findViewById(R.id.noAvailableRoomsTextView);
		mLocationSpinner = (Spinner) findViewById(R.id.locationRadioGroup);
		mGuestFragment = (GuestFragment) getFragmentManager().findFragmentById(R.id.guestFragment);
		mRoomInfoFragment = (RoomInfoFragment) getFragmentManager().findFragmentById(R.id.roomInfoFragment);
				
		setupEditTextFocus();
		
		this.mController.setupInitialTime(getIntent().getExtras());
		
		setDateTimeButtonsAndTitleText();
		
		setupDateTimeButtonsOnClickListeners();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mAvailableRooms = new ArrayList<Room>();
		
		checkForQuickReservation();
		
		updateAvailableRooms();
		
		mRoomInfoFragment.reset(true);
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
	
	private class AvailableRoomsTask extends AsyncTask<Void, Void, ArrayList<Room>> {
		private ProgressDialog mDialog;
		
		@Override
		protected ArrayList<Room> doInBackground(Void... params) {
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
		protected void onPostExecute(ArrayList<Room> result) {
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
					mLocationSpinner.setAdapter(null);

//		        	if (mAvailableRooms.size() == 0) {
//		        		mNoAvailableRoomsTextView.setVisibility(android.view.View.VISIBLE);
//		        	}
//		        	else {
//		        		mNoAvailableRoomsTextView.setVisibility(android.view.View.INVISIBLE);
//		        	
//		        	}

//		        	// Create an ArrayAdapter using the string array and a default spinner layout
		        	ArrayAdapter<Room> adapter = new ArrayAdapter<Room>(ReserveRoomActivity.this.getBaseContext(),
		        		     android.R.layout.simple_spinner_item, mAvailableRooms); 
		        			
		        	// Specify the layout to use when the list of choices appears
		        	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		        	// Apply the adapter to the spinner
		        	mLocationSpinner.setAdapter(adapter);
		        	
		        	mLocationSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
					    
					    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					    	mSelectedRoom = mAvailableRooms.get(position).getFullName();
					    	mController.getApplicationState().setCurrentRoom( mAvailableRooms.get(position));
					    	mRoomInfoFragment.reset(true);
					    }

					    
					    public void onNothingSelected(AdapterView<?> parentView) {
					        // your code here
					    }

					});
					
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
				mLocationSpinner.setSelection(radioSelection);
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
