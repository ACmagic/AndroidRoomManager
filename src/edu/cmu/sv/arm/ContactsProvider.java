package edu.cmu.sv.arm;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

//ContactsTask pulls all of the contacts from the device, not just from the provided account
public class ContactsProvider extends AsyncTask<Context, Void, Vector<String>> {
	private AsyncTaskCompleteListener<Vector<String> > mTaskCompletedCallback;

	public ContactsProvider(AsyncTaskCompleteListener<Vector<String> > listener){
		this.mTaskCompletedCallback = listener;
	}

	@Override
	protected Vector<String> doInBackground(Context... params) {
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { BaseColumns._ID, ContactsContract.Contacts.DISPLAY_NAME };
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

		Vector<String> cntcts = new Vector<String>();
		Cursor contacts = params[0].getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
		while (contacts.moveToNext()) {
			String contactId = contacts.getString(contacts.getColumnIndex(BaseColumns._ID)); 
			Cursor emails = params[0].getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
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
		this.mTaskCompletedCallback.onTaskCompleted(result);
	}
}
