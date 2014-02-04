	package edu.cmu.sv.arm;

import java.util.GregorianCalendar;
import java.util.Vector;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class AndroidRoomManagerMainActivity extends Activity implements AsyncTaskCompleteListener{
	
	//private ARM mAppState;
	private Dialog mLoadingDialog;
	private ActionBar mActionBar;
	
	private CalendarView mCalendarView;
	
	private CalendarFragment mCalendarFragment;
	
	private Handler mApplicationResetHandler;
	private Runnable mApplicationResetter;
	
	private static final int LOADING_DIALOG_TIMEOUT = DateTimeHelpers.SECOND_IN_MILLISECONDS * 30;
	
	private TextView mActionBarTextView;
	private AndroidRoomManagerMainController mController = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        showLoadingDialog();

        // Check arguments!
        mController = new AndroidRoomManagerMainController(this, this, getString(R.string.app_name_extended));

        configurePreferences();
        
        setContentView(R.layout.main);
        
        mCalendarFragment = (CalendarFragment) getFragmentManager().findFragmentById(R.id.calendarFragment);
        
        configureActionBar();
		
        configureCalendarView();
        
        updateRoomNumber();
        
        mController.updateContacts(this);
        
        configureRunnables();
        
        configureButtons();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
       	setupApplicationResetter();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	this.mController.stopResources();;
    	stopApplicationResetter();
    	
    	if (mCalendarFragment != null) {
    		mCalendarFragment.stopEventsUpdater();
    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.actionbar_main, menu);
	    
	    return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.todayButton:
	    	mCalendarView.setDate(DateTimeHelpers.getCurrentDate().getTime());
	    	restartCalendarFragmentEventsUpdater();
	    	return true;
	    case android.R.id.home:
	    	// Intentionally left blank
	    case R.id.resetButton:
	    	updateRoomNumber();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    
    @Override
    public void onUserInteraction() {
    	resetApplicationResetter();
    }
    
    
    
    public void stopApplicationResetter() {
    	if (mApplicationResetHandler != null && mApplicationResetter != null) {
    		mApplicationResetHandler.removeCallbacks(mApplicationResetter);
		}
    }

    // Resets the timeout for the application resetter
    public void resetApplicationResetter() {
    	stopApplicationResetter();    	
    	setupApplicationResetter();
    }
    
    // Adds the application resetter task to the application reset handler
    private void setupApplicationResetter() {
    	//Check!
    	mApplicationResetHandler.postDelayed(mApplicationResetter, PreferenceManager.getDefaultSharedPreferences(
    			this.mController.getApplicationState()).getInt("applicationTimeout", 5) * DateTimeHelpers.MINUTE_IN_MILLISECONDS);
    }
    
    // Updates the room number application-wide
    public void updateRoomNumber() { 
    	int navigationItemIndex = this.mController.getDefaultRoom();
		
		if (mActionBar != null) {
			mActionBar.setSelectedNavigationItem(navigationItemIndex);
			mCalendarView.setDate(DateTimeHelpers.getCurrentDate().getTime());
			mCalendarFragment.resetCalendarScroll();
		}
    }
    
    // Configures the background tasks
    private void configureRunnables() {
    	mController.configureCalendarRunnable();
		mApplicationResetHandler = new Handler();
		mApplicationResetter = new Runnable() {
			public void run() {    	 
				updateRoomNumber();
				mController.updateContacts(getBaseContext());
				setupApplicationResetter();
			}
		};
		setupApplicationResetter();
    }
    
    
    // Configures buttons
    private void configureButtons() {
    	Button reserveRoomButton = (Button) findViewById(R.id.reserveRoomButton);
		reserveRoomButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent reserveRoomActivity = new Intent(getBaseContext(), ReserveRoomActivity.class);
            	reserveRoomActivity.putExtra("quickReservation", false);
        		startActivity(reserveRoomActivity);
            }
        });
		
		Button quickReservationButton = (Button) findViewById(R.id.quickReservationButton);
		quickReservationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent reserveRoomActivity = new Intent(getBaseContext(), ReserveRoomActivity.class);
            	reserveRoomActivity.putExtra("quickReservation", true);
            	
            	reserveRoomActivity.putExtra("selectedRoom", mController.getApplicationState().getRooms().get(mActionBar.getSelectedNavigationIndex()));
        		startActivity(reserveRoomActivity);
            }
        });
    }
    
    // Runs when the preferences have changed
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("roomNumberPref")) {
			updateRoomNumber();
		}
	}
    
    // Configures the preferences of the application
    private void configurePreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        setTitle(mController.getApplicationState().getTitle());
    }
    
    // Configures the action bar
    private void configureActionBar() {
    	mActionBar = getActionBar();
    	mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    	
    	mActionBarTextView = new TextView(this);
    	mActionBarTextView.setText("[ Default: " + mController.getApplicationState().getDefaultRoom().getFullName() + " ]");
    	
    	mActionBarTextView.setGravity(Gravity.CENTER_VERTICAL);
    	mActionBarTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
    	mActionBarTextView.setTypeface(null, Typeface.BOLD);
    	
    	mActionBar.setDisplayShowCustomEnabled(true);
    	mActionBar.setCustomView(mActionBarTextView);
    	
    	ArrayAdapter<CharSequence> aa = new ArrayAdapter<CharSequence>(this, R.layout.spinner_selector_text_view, mController.getApplicationState().getRooms().toArray(new CharSequence[mController.getApplicationState().getRooms().size()]));//ArrayAdapter.createFromResource(this, R.array.rooms, R.layout.spinner_selector_text_view);
    	aa.setDropDownViewResource(R.layout.spinner_dropdown_text_view);
    	
    	SpinnerAdapter spinnerAdapter = aa;
    	
    	ActionBar.OnNavigationListener navigationCallback = new ActionBar.OnNavigationListener() {
    		// Occurs during room changes (non-preferences)
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {				
				mController.getApplicationState().setCurrentRoom(mController.getApplicationState().getNumberAddressedRooms().get(mController.getApplicationState().getRooms().get(itemPosition)));
				
				restartCalendarFragmentEventsUpdater();
				
				return false;
			}
		};
    	
    	mActionBar.setListNavigationCallbacks(spinnerAdapter, navigationCallback);
    }
    
    // Configures the calendar view
    private void configureCalendarView() {
    	mCalendarView = (CalendarView) findViewById(R.id.calendarView);
    	
    	mCalendarView.setOnDateChangeListener(new OnDateChangeListener() {
			public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
				mCalendarFragment.setSelectedDate(new GregorianCalendar(year, month, dayOfMonth).getTime());
			}
		});
    }
    
    // Shows the loading splash screen
    protected void showLoadingDialog() {
    	mLoadingDialog = new Dialog(AndroidRoomManagerMainActivity.this, R.style.loading_screen);
		mLoadingDialog.setContentView(R.layout.loading_screen);
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.show();
		
		// Automatically remove the loading dialog if it has not yet been removed
		final Handler loadingDialogRemover = new Handler();
		loadingDialogRemover.postDelayed(new Runnable() {
			public void run() {
				dismissLoadingDialog();
			}
		}, LOADING_DIALOG_TIMEOUT);
	}
	
    // Dismisses the loading splash screen
	protected void dismissLoadingDialog() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
			mLoadingDialog = null;
		}
	}
	
    public void restartCalendarFragmentEventsUpdater() {
    	mCalendarFragment.restartEventsUpdater();
    }

	public void onTaskCompleted(Object result) {
		
		restartCalendarFragmentEventsUpdater();
		if (result instanceof Vector<?>){
			//Check if this applies for both
	        dismissLoadingDialog();
		}
		
	}
}
