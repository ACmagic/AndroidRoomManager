package edu.cmu.sv.arm;

import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarRequest;


//import edu.cmu.sv.arm.AndroidRoomManagerMainActivity.CalendarServiceRegistrationTask;
//import edu.cmu.sv.arm.AndroidRoomManagerMainActivity.ContactsTask;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

public class CalendarProvider extends AsyncTask <Void, Void, Void>{
	
	private Handler mGoogleAuthTokenUpdateHandler;
	private Runnable mGoogleAuthTokenUpdater;
	private Activity mActivity = null;
	private String mAccountName;
	private ARM mAppState;
	private CalendarServiceRegistrationTask mCSRT;
	private String mAppName;
	
	public CalendarProvider(Activity activity, ARM appState, String appName){
		this.mActivity = activity;
		this.mAppState = appState;
		this.mAccountName = this.mAppState.getGoogleAccountName();
		this.mAppName = appName;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		startGoogleAuthTokenUpdater();
		return null;
	}
	
	// Configures the background tasks
    public void configureRunnable() {
    	mGoogleAuthTokenUpdateHandler = new Handler();
        mGoogleAuthTokenUpdater = new Runnable() {
			public void run() {    	 
				obtainGoogleAuthToken();
				mGoogleAuthTokenUpdateHandler.postDelayed(mGoogleAuthTokenUpdater, DateTimeHelpers.getMillisecondsUntilNextMinute() + DateTimeHelpers.MINUTE_IN_MILLISECONDS * 30);
			}
		};
		startGoogleAuthTokenUpdater();
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
    	AccountManager accountManager = AccountManager.get(this.mActivity.getBaseContext());
    	Account[] accounts = accountManager.getAccountsByType("com.google");
	    
	    Account roomManagerAccount = null;
	    
	    for (int i = 0; i < accounts.length; i++) {
	    	if (accounts[i].name.equalsIgnoreCase(this.mAccountName)) {
	    		roomManagerAccount = accounts[i];
	    		break;
	    	}
	    }
	    
	    if (roomManagerAccount == null) {
	    	// TODO What if appropriate account does not exist?
	    }
	    String authTokenType = "oauth2:https://www.googleapis.com/auth/calendar";
	    if (roomManagerAccount != null) {
		    accountManager.getAuthToken(roomManagerAccount, authTokenType, null, this.mActivity, new AccountManagerCallback<Bundle>() {
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
		    				mCSRT.execute(authToken, mAppName);
		    			}
		    		} catch (Exception e) {
		    			// TODO What if cannot obtain auth token??
					}
		        }
		    }, null);
	    }
    }
    
    private class CalendarServiceRegistrationTask extends AsyncTask<String, Void, Calendar> {    	
    	// Attempts to get a calendar service going
    	// Arguments:
    	// [0] - token
    	//[1] - appName
    	@Override
		protected Calendar doInBackground(String... args) {
    		try {
    			AccessProtectedResource accessProtectedResource = new GoogleAccessProtectedResource(args[0]);
		    	HttpTransport transport = AndroidHttp.newCompatibleTransport();
		    	Calendar service = Calendar.builder(transport, new JacksonFactory())
		    			.setApplicationName(args[1])
		    			.setHttpRequestInitializer(accessProtectedResource)
		    			.setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
							public void initialize(JsonHttpRequest request) {
		    			        CalendarRequest calRequest = (CalendarRequest) request;		    			        
		    					String apiKey = mAppState.getGoogleCalendarAPIKey();
		    			        calRequest.setKey(apiKey);
		    			    }
		    			}).build();
		    	return service;
    		}
    		catch (Exception e) {
    			// TODO 
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
    		}
    	}
    }
    
    public void stopResources(){
    	if (mCSRT != null) {
			mCSRT.cancel(true);
			mCSRT = null;
		}
    	stopGoogleAuthTokenUpdater();
    }
}
