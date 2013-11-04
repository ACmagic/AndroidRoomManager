package edu.cmu.sv.arm;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class GuestFragment extends Fragment {
	private ARM mAppState;
	
	private AutoCompleteTextView mHostEmailAutoCompleteTextView;
	private AutoCompleteTextView mGuestEmailAutoCompleteTextView;
	private Button mAddGuestButton;
	private TableRow mTableRow3;
	private LinearLayout mGuestsLinearLayout;
	
	private ArrayList<String> mGuests;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View guestView = inflater.inflate(R.layout.guest, container, false);
		
		mAppState = ((ARM) getActivity().getApplication());
		
		mHostEmailAutoCompleteTextView = (AutoCompleteTextView) guestView.findViewById(R.id.hostEmailAutoCompleteTextView);
		mGuestEmailAutoCompleteTextView = (AutoCompleteTextView) guestView.findViewById(R.id.guestEmailAutoCompleteTextView);
		mAddGuestButton = (Button) guestView.findViewById(R.id.addGuestButton);
		mTableRow3 = (TableRow) guestView.findViewById(R.id.tableRow3);
		mGuestsLinearLayout = (LinearLayout) guestView.findViewById(R.id.guestsLinearLayout);
		
		mGuests = new ArrayList<String>();
		
		setupAddGuestButton();
		
		setupAutoComplete();
		
		return guestView;
	}
	
	public String getHostEmail() {
		return mHostEmailAutoCompleteTextView.getText().toString();
	}
	
	public ArrayList<String> getGuests() {
		return mGuests;
	}
	
	private void updateGuestListVisibility() {
		if (mGuests.size() == 0) {
    		mTableRow3.setVisibility(android.view.View.INVISIBLE);
    	}
		else if (mGuests.size() > 0) {
    		mTableRow3.setVisibility(android.view.View.VISIBLE);
    	}
	}
	
	private void addGuest(String guest) {
		mGuests.add(guest);
		
		View guestListRow = getActivity().getLayoutInflater().inflate(R.layout.guest_list_row, null);
		
		TextView email = (TextView) guestListRow.findViewById(R.id.emailTextView);
		email.setText(guest);
		
		guestListRow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				v.setBackgroundResource(R.color.pale_blue);
				
				LinearLayout guestListRow = (LinearLayout) v;
				
				String email = ((TextView) guestListRow.findViewById(R.id.emailTextView)).getText().toString();
				
				// Remove guest from guests array
				for (int i = 0; i < mGuests.size(); i++) {
					if (mGuests.get(i).equals(email)) {
						mGuests.remove(i);
					}
				}
				
				// Remove guest from visible guest list
				((LinearLayout) v.getParent()).removeView(v);
				
				updateGuestListVisibility();
			}
		});
		
		mGuestsLinearLayout.addView(guestListRow);
		
		updateGuestListVisibility();
	}
	
	private void setupAddGuestButton() {
		mAddGuestButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (mGuestEmailAutoCompleteTextView.getText().toString().isEmpty()) {
        			// If no text entry, ignore user's attempt to press button
        			
					return;
        		}
        		else {
        			if (!PatternChecker.isValidEmail(mGuestEmailAutoCompleteTextView.getText().toString())) {
        				// Show error if the e-mail is invalid
        				
        				AlertDialogHelper.buildAlertDialog(getActivity(), getString(R.string.error), getString(R.string.not_a_valid_email), getString(R.string.ok));
					}
					else if (mGuests.contains(mGuestEmailAutoCompleteTextView.getText().toString())) {
						// Show error if the e-mail is already on the guest list
						
						AlertDialogHelper.buildAlertDialog(getActivity(), getString(R.string.error), getString(R.string.already_a_guest), getString(R.string.ok));

				    	mGuestEmailAutoCompleteTextView.setText("");
					}
					else {
						// Add the guest to the guest list
						
						addGuest(mGuestEmailAutoCompleteTextView.getText().toString());
						mGuestEmailAutoCompleteTextView.setText("");
					}
        		}
        	}
		});
	}
	
	public void setupAutoComplete() {
		if (mAppState.getContacts() != null) {
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_dropdown_item_1line, mAppState.getContacts().toArray(new String[mAppState.getContacts().size()]));
	        
	        mHostEmailAutoCompleteTextView.setAdapter(adapter);
	        mGuestEmailAutoCompleteTextView.setAdapter(adapter);
        }
	}

	public void setFocusOnHostEmail() {
		mHostEmailAutoCompleteTextView.requestFocus();		
	}
	
	public boolean hasFocusHostEmail() {
		return mHostEmailAutoCompleteTextView.hasFocus();
	}
	
	public void setFocusOnGuestEmail() {
		mGuestEmailAutoCompleteTextView.requestFocus();
	}
}