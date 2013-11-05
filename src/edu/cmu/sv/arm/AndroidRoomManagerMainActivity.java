package edu.cmu.sv.arm;

import java.util.GregorianCalendar;
import java.util.Vector;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
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

import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarRequest;

public class AndroidRoomManagerMainActivity extends Activity {
	private ARM mAppState;
	private Dialog mLoadingDialog;
	private ActionBar mActionBar;
	
	private CalendarView mCalendarView;
	
	private CalendarFragment mCalendarFragment;
	
	private Handler mGoogleAuthTokenUpdateHandler;
	private Runnable mGoogleAuthTokenUpdater;
	
	private Handler mApplicationResetHandler;
	private Runnable mApplicationResetter;
	
	private static final int LOADING_DIALOG_TIMEOUT = DateTimeHelpers.SECOND_IN_MILLISECONDS * 30;
	
	private TextView mActionBarTextView;
	
	private CalendarServiceRegistrationTask mCSRT;
	private ContactsTask mCT;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        showLoadingDialog();
        
        mAppState = ((ARM) getApplication());
        
        mAppState.setMainActivity(this);
        
        configurePreferences();
        
        setContentView(R.layout.main);
        
        mCalendarFragment = (CalendarFragment) getFragmentManager().findFragmentById(R.id.calendarFragment);
        
        configureActionBar();
        
        configureCalendarView();
        
        updateRoomNumber();
        
        updateContacts();
        
        configureRunnables();
        
        configureButtons();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	startGoogleAuthTokenUpdater();
    	setupApplicationResetter();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	if (mCSRT != null) {
			mCSRT.cancel(true);
			mCSRT = null;
		}
    	
    	if (mCT != null) {
    		mCT.cancel(true);
    		mCT = null;
    	}
    	
    	stopGoogleAuthTokenUpdater();
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
	    	//resetCameraFragment(true);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    
    @Override
    public void onUserInteraction() {
    	resetApplicationResetter();
    }
    
    // Sets up the preferences authentication dialog
    /*private void setupPreferencesAuthenticationDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);

    	LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
    	final View layout = inflater.inflate(R.layout.preferences_authentication_dialog, null);
    	
    	final CheckBox changePasswordCheckBox = (CheckBox) layout.findViewById(R.id.changePasswordCheckBox);
    	final LinearLayout changePasswordLinearLayout = (LinearLayout) layout.findViewById(R.id.changePasswordLinearLayout);
    	final EditText newPreferencesPasswordEditText = (EditText) layout.findViewById(R.id.newPreferencesPasswordEditText);
    	final EditText confirmedNewPreferencesPasswordEditText = (EditText) layout.findViewById(R.id.confirmedNewPreferencesPasswordEditText);
    	
    	changePasswordCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					changePasswordLinearLayout.setVisibility(View.VISIBLE);
				}
				else {
					newPreferencesPasswordEditText.setText("");
					confirmedNewPreferencesPasswordEditText.setText("");
					changePasswordLinearLayout.setVisibility(View.GONE);
				}
			}
		});

    	builder.setView(layout);
    	builder.setTitle(R.string.preferences_authentication);
    	
    	builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
    	
    	builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
    	
    	final AlertDialog alertDialog = builder.create();
    	
    	alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			public void onShow(DialogInterface dialog) {
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mAppState);
        				String storedPassword = preferences.getString("preferencesPassword", "0000");
        				
        				EditText preferencesPasswordEditText = (EditText) layout.findViewById(R.id.preferencesPasswordEditText);
        				
        				String enteredPassword = "";
        				
        				// Source: http://www.mkyong.com/java/java-sha-hashing-example/
        				try {
        		        	MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        		        	messageDigest.update(preferencesPasswordEditText.getText().toString().getBytes());
        		        	
        		        	byte bytes[] = messageDigest.digest();
        		        	
        		        	StringBuffer stringBuffer = new StringBuffer();
        		        	
        		            for (int i = 0; i < bytes.length; i++) {
        		                	stringBuffer.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        		            }
        		        	
        		        	enteredPassword = stringBuffer.toString();
        		        }
        		        catch (Exception e) {
        		        	// Intentionally left blank
        		        }
        				
        				if (storedPassword.equals(enteredPassword)) {
        					if (changePasswordCheckBox.isChecked()) {
        						String newPassword = newPreferencesPasswordEditText.getText().toString();
        						String newConfirmedPassword = confirmedNewPreferencesPasswordEditText.getText().toString();
        						if (newPassword.isEmpty() && newConfirmedPassword.isEmpty()) {
        							AlertDialog.Builder builder = new AlertDialog.Builder(CMUSVAndroidRoomManager.this);
                					builder.setTitle(getString(R.string.error));
            						builder.setMessage(getString(R.string.no_new_password));
                					builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                						public void onClick(DialogInterface dialog, int which) {
                							
                						}
                					});
                					builder.show();
                					
                					return;
        						}
        						if (newPassword.equals(newConfirmedPassword)) {
        					        try {
        	        		        	MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        	        		        	messageDigest.update(newPassword.toString().getBytes());
        	        		        	
        	        		        	byte bytes[] = messageDigest.digest();
        	        		        	
        	        		        	StringBuffer stringBuffer = new StringBuffer();
        	        		        	
        	        		            for (int i = 0; i < bytes.length; i++) {
        	        		            	stringBuffer.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        	        		            }
        	        		            
        	        		            SharedPreferences.Editor editor = preferences.edit();
        	        		        	
        	        		            editor.putString("preferencesPassword", stringBuffer.toString());
            					        
            					        editor.commit();
        	        		        }
        	        		        catch (Exception e) {
        	        		        	// Intentionally left blank
        	        		        }
        						}
        						else {
        							AlertDialog.Builder builder = new AlertDialog.Builder(CMUSVAndroidRoomManager.this);
                					builder.setTitle(getString(R.string.error));
            						builder.setMessage(getString(R.string.passwords_do_not_match));
                					builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                						public void onClick(DialogInterface dialog, int which) {
                							
                						}
                					});
                					builder.show();
                					
                					return;
        						}
        					}
        					
        					Intent preferencesActivity = new Intent(getBaseContext(), Preferences.class);
        					startActivity(preferencesActivity);
        					alertDialog.dismiss();
        				}
        				else {
        					AlertDialog.Builder builder = new AlertDialog.Builder(CMUSVAndroidRoomManager.this);
        					builder.setTitle(getString(R.string.error));
        					if (preferencesPasswordEditText.getText().length() == 0) {
        						builder.setMessage(getString(R.string.no_password));
        					}
        					else {
        						builder.setMessage(getString(R.string.incorrect_password));
        					}
        					builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
        						public void onClick(DialogInterface dialog, int which) {
        							
        						}
        					});
        					builder.show();
        				}
                    }
                });
			}
		});
    	
    	alertDialog.show();
    }*/
    
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
    	mApplicationResetHandler.postDelayed(mApplicationResetter, PreferenceManager.getDefaultSharedPreferences(mAppState).getInt("applicationTimeout", 5) * DateTimeHelpers.MINUTE_IN_MILLISECONDS);
    }
    
    // Updates the room number application-wide
    public void updateRoomNumber() { 
    	int navigationItemIndex = 0;
    	
    	if (mAppState.getDefaultRoom() != null) {
	    	String defaultRoom = mAppState.getDefaultRoom().getFullName();
			
			for (int i = 0; i < mAppState.getRooms().size(); i++) {
				if (mAppState.getRooms().get(i).equals(defaultRoom)) {
					navigationItemIndex = i;
					break;
				}
			}
		}
		
		if (mActionBar != null) {
			mActionBar.setSelectedNavigationItem(navigationItemIndex);
			//mCalendarView.setDate(DateTimeHelpers.getCurrentDate().getTime() - DateTimeHelpers.DAY_IN_MILLISECONDS);
			mCalendarView.setDate(DateTimeHelpers.getCurrentDate().getTime());
			mCalendarFragment.resetCalendarScroll();
		}
    }
    
    // Configures the background tasks
    private void configureRunnables() {
    	mGoogleAuthTokenUpdateHandler = new Handler();
		
        mGoogleAuthTokenUpdater = new Runnable() {
			public void run() {    	 
				obtainGoogleAuthToken();
		    	 
				mGoogleAuthTokenUpdateHandler.postDelayed(mGoogleAuthTokenUpdater, DateTimeHelpers.getMillisecondsUntilNextMinute() + DateTimeHelpers.MINUTE_IN_MILLISECONDS * 30);
			}
		};
		
		startGoogleAuthTokenUpdater();
		
		mApplicationResetHandler = new Handler();
		
		mApplicationResetter = new Runnable() {
			public void run() {    	 
				updateRoomNumber();
				
				updateContacts();
				
				//resetCameraFragment(true);
		    	
				setupApplicationResetter();
			}
		};
		
		setupApplicationResetter();
    }
    
    // Update contacts
    private void updateContacts() {
    	if (mCT != null) {
    		mCT.cancel(true);
    		mCT = null;
    	}
    	
    	mCT = new ContactsTask();
    	
    	mCT.execute();
    }
    
    // ContactsTask pulls all of the contacts from the device, not just from the provided account
    // This may be an issue (not sure how to pull contacts just for a specified account)
    private class ContactsTask extends AsyncTask<Void, Void, Vector<String>> {
		@Override
		protected Vector<String> doInBackground(Void... params) {
			Uri uri = ContactsContract.Contacts.CONTENT_URI;
	        String[] projection = new String[] { BaseColumns._ID, ContactsContract.Contacts.DISPLAY_NAME };
	        String selection = null;
	        String[] selectionArgs = null;
	        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
	        
	        Vector<String> cntcts = new Vector<String>();
	        
	    	Cursor contacts = getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
	    	
	        while (contacts.moveToNext()) {
	        	String contactId = contacts.getString(contacts.getColumnIndex(BaseColumns._ID)); 
	        	
	        	Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
	        	
	        	while (emails.moveToNext()) { 
	        	   String email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)); 
	        	   
	        	   cntcts.add(email);
	        	}
	        	emails.close();
	        } 
	        contacts.close();
	        
	        return cntcts;
		}
		
		@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    	}
    	
    	@Override
    	protected void onPostExecute(Vector<String> result) {
    		super.onPostExecute(result);
    		
    		mAppState.setContacts(result);
    		
    		dismissLoadingDialog();
    	}
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
            	
            	reserveRoomActivity.putExtra("selectedRoom", mAppState.getRooms().get(mActionBar.getSelectedNavigationIndex()));
        		startActivity(reserveRoomActivity);
            }
        });
    }
    
    // Runs when the preferences have changed
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//		if (key.equals("maximumNumberOfCamerasPerRoom") || key.equals("cameraRefreshDelay")) {
//			resetCameraFragment(false);
//		}
		if (key.equals("roomNumberPref")) {
			updateRoomNumber();
		}
	}
    
    // Configures the preferences of the application
    private void configurePreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        setTitle(mAppState.getTitle());
    }
    
    // Configures the action bar
    private void configureActionBar() {
    	mActionBar = getActionBar();
    	mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    	
    	mActionBarTextView = new TextView(this);
    	mActionBarTextView.setText("[ Default: " + mAppState.getDefaultRoom().getFullName() + " ]");
    	
    	mActionBarTextView.setGravity(Gravity.CENTER_VERTICAL);
    	mActionBarTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
    	mActionBarTextView.setTypeface(null, Typeface.BOLD);
    	
    	mActionBar.setDisplayShowCustomEnabled(true);
    	mActionBar.setCustomView(mActionBarTextView);
    	
    	ArrayAdapter<CharSequence> aa = new ArrayAdapter<CharSequence>(this, R.layout.spinner_selector_text_view, mAppState.getRooms().toArray(new CharSequence[mAppState.getRooms().size()]));//ArrayAdapter.createFromResource(this, R.array.rooms, R.layout.spinner_selector_text_view);
    	aa.setDropDownViewResource(R.layout.spinner_dropdown_text_view);
    	
    	SpinnerAdapter spinnerAdapter = aa;
    	
    	ActionBar.OnNavigationListener navigationCallback = new ActionBar.OnNavigationListener() {
    		// Occurs during room changes (non-preferences)
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {				
				mAppState.setCurrentRoom(mAppState.getNumberAddressedRooms().get(mAppState.getRooms().get(itemPosition)));
				
				//resetCameraFragment(true);
				
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
    
    //Resets the camera fragment
    public void resetCameraFragment(boolean resetCamera) {
		CameraFragment cameraFragment = (CameraFragment) getFragmentManager().findFragmentById(R.id.cameraFragment);
		cameraFragment.reset(resetCamera);
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
	
	private void startGoogleAuthTokenUpdater() {
		if (mGoogleAuthTokenUpdater != null) {
			mGoogleAuthTokenUpdater.run();
		}
	}
	
	private void stopGoogleAuthTokenUpdater() {
		if (mGoogleAuthTokenUpdateHandler != null && mGoogleAuthTokenUpdater != null) {
			mGoogleAuthTokenUpdateHandler.removeCallbacks(mGoogleAuthTokenUpdater);
		}
	}
	
	// Obtains a Google Auth Token
    public void obtainGoogleAuthToken() {
    	AccountManager accountManager = AccountManager.get(AndroidRoomManagerMainActivity.this.getBaseContext());
    	Account[] accounts = accountManager.getAccountsByType("com.google");
	    
	    Account roomManagerAccount = null;
	    
	    for (int i = 0; i < accounts.length; i++) {
	    	if (accounts[i].name.equalsIgnoreCase(mAppState.getGoogleAccountName())) {
	    		roomManagerAccount = accounts[i];
	    		break;
	    	}
	    }
	    
	    if (roomManagerAccount == null) {
	    	// TODO What if appropriate account does not exist?
	    }
	    
	    String authTokenType = "oauth2:https://www.googleapis.com/auth/calendar";
	    
	    
	    if (roomManagerAccount != null) {
		    accountManager.getAuthToken(roomManagerAccount, authTokenType, null, AndroidRoomManagerMainActivity.this, new AccountManagerCallback<Bundle>() {
				public void run(AccountManagerFuture<Bundle> future) {
		    		try {
		    			// Grab an auth token
		    			String authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
		    			
		    			if (authToken == null) {
		    				// TODO Need to throw an exception
		    			}
		    			else {
		    				mAppState.setAuthToken(authToken);
		    				
		    				if (mCSRT != null) {
		    					mCSRT.cancel(true);
		    					mCSRT = null;
		    				}
		    				
		    				mCSRT = new CalendarServiceRegistrationTask();
		    				
		    				mCSRT.execute(authToken);
		    			}
		    		} catch (Exception e) {
		    			AndroidRoomManagerMainActivity.this.runOnUiThread(new Runnable() {
							public void run() {
					        	// TODO What if cannot obtain auth token??
					        }
					    });
		    		}
		        }
		    }, null);
	    
	    }
    }
    
    private class CalendarServiceRegistrationTask extends AsyncTask<String, Void, Calendar> {    	
    	// Attempts to get a calendar service going
    	// Arguments:
    	// [0] - token
    	@Override
		protected Calendar doInBackground(String... args) {
    		try {
    			AccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(args[0]);
 	    	   	
		    	HttpTransport transport = AndroidHttp.newCompatibleTransport();
		    	
		    	Calendar service = Calendar.builder(transport, new JacksonFactory())
		    			.setApplicationName(getString(R.string.app_name_extended))
		    			.setHttpRequestInitializer(accessProtectedResource)
		    			.setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
							public void initialize(JsonHttpRequest request) {
		    			        CalendarRequest calRequest = (CalendarRequest) request;
		    					
		    			        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mAppState);
		    			        
		    					String apiKey = mAppState.getGoogleCalendarAPIKey();//prefs.getString("googleCalendarAPIKey", "AIzaSyD8q4DB7NPcxBCEstCOowsazOZtQ5uzty8");
		    			        
		    			        calRequest.setKey(apiKey);
		    			    }

		    			}).build();
		    	
		    	return service;
    		}
    		catch (Exception e) {
    			// TODO Perhaps may need to handle the exception
    			return null;
    		}
    	}
    	
    	@Override
    	protected void onPostExecute(Calendar result) {
    		super.onPostExecute(result);
    		
    		if (result == null) {
	    		// TODO Handle error
    		}
    		else {
    			mAppState.setCalendar(result);
    			
    			restartCalendarFragmentEventsUpdater();
    		}
    	}
    }
    
    public void restartCalendarFragmentEventsUpdater() {
    	mCalendarFragment.restartEventsUpdater();
    }
}
