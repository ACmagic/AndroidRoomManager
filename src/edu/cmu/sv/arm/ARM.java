package edu.cmu.sv.arm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import android.app.Application;

import com.google.api.services.calendar.Calendar;

public class ARM extends Application {
	private String mAuthToken = null;
	private Calendar mCalendar = null;
	private Vector<String> mContacts = null;
	private Room mCurrentRoom = null;
	private AndroidRoomManagerMainActivity mMainActivity = null;
	
	
	private String mGoogleCalendarAPIKey = null;
	private String mGoogleAccountName = null;
	private int mApplicationTimeoutMinutes = 0;
	private int mApplicationTimeoutSeconds = 0;
	private int mCalendarRefreshDelayMinutes = 0;
	private int mCalendarRefreshDelaySeconds = 0;
	
	private Hashtable<String, Room> mNumberAddressedRooms = new Hashtable<String, Room>();
	private Hashtable<String, Room> mResourceAddressedRooms = new Hashtable<String, Room>();
	private ArrayList<Room> mRooms = new ArrayList<Room>();
	
	private Room mDefaultRoom = null;
	
	private String mTitle;
	private String mEndpoint;
	
	private InputStream mArmSettingsInputStream = null;
	
	public String getAuthToken() {
		return mAuthToken;
	}
	
	public void setAuthToken(String authToken) {
		mAuthToken = authToken;
	}
	
	public Calendar getCalendar() {
		return mCalendar;
	}
	
	public void setCalendar(Calendar calendar) {
		mCalendar = calendar;
	}
	
	public Vector<String> getContacts() {
		return mContacts;
	}
	
	public void setContacts(Vector<String> contacts) {
		mContacts = contacts;
	}
	
	public Room getCurrentRoom() {
		return mCurrentRoom;
	}
	
	public void setCurrentRoom(Room currentRoom) {
		mCurrentRoom = currentRoom;
	}
	
	public AndroidRoomManagerMainActivity getMainActivity() {
		return mMainActivity;
	}
	
	public void setMainActivity(AndroidRoomManagerMainActivity mainActivity) {
		mMainActivity = mainActivity;
	}
	
	
	public String getGoogleCalendarAPIKey() {
		return mGoogleCalendarAPIKey;
	}
	
	public void setGoogleCalendarAPIKey(String googleCalendarAPIKey) {
		mGoogleCalendarAPIKey = googleCalendarAPIKey;
	}
	
	public String getGoogleAccountName() {
		return mGoogleAccountName;
	}
	
	public void setGoogleAccountName(String googleAccountName) {
		mGoogleAccountName = googleAccountName;
	}
	
	public int getApplicationTimeoutMinutes() {
		return mApplicationTimeoutMinutes;
	}
	
	public void setApplicationTimeoutMinutes(int applicationTimeoutMinutes) {
		mApplicationTimeoutMinutes = applicationTimeoutMinutes;
	}
	
	public int getApplicationTimeoutSeconds() {
		return mApplicationTimeoutSeconds;
	}
	
	public void setApplicationTimeoutSeconds(int applicationTimeoutSeconds) {
		mApplicationTimeoutSeconds = applicationTimeoutSeconds;
	}
	
	public int getCalendarRefreshDelayMinutes() {
		return mCalendarRefreshDelayMinutes;
	}
	
	public void setCalendarRefreshDelayMinutes(int calendarRefreshDelayMinutes) {
		mCalendarRefreshDelayMinutes = calendarRefreshDelayMinutes;
	}
	
	public int getCalendarRefreshDelaySeconds() {
		return mCalendarRefreshDelaySeconds;
	}
	
	public void setCalendarRefreshDelaySeconds(int calendarRefreshDelaySeconds) {
		mCalendarRefreshDelaySeconds = calendarRefreshDelaySeconds;
	}	
	
	public Hashtable<String, Room> getNumberAddressedRooms() {
		return mNumberAddressedRooms;
	}
	
	public Hashtable<String, Room> getResourceAddressedRooms() {
		return mResourceAddressedRooms;
	}
	
	public void addRoom(Room room) {	
		mNumberAddressedRooms.put(room.getFullName(), room);
		mResourceAddressedRooms.put(room.getResourceAddress(), room);
		
		int index = Collections.binarySearch(mRooms, room);
		
		if (index < 0) {
			mRooms.add(-index - 1, room);
		}
	}
	
	public ArrayList<Room> getRooms() {
		return mRooms;
	}
	
	public Room getDefaultRoom() {
		return mDefaultRoom;
	}
	
	public void setDefaultRoom(Room room) {
		mDefaultRoom = room;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public InputStream getArmSettingsInputStream() {
		return mArmSettingsInputStream;
	}
	
	public void setArmSettingsInputStream(InputStream armSettingsInputStream) {
		mArmSettingsInputStream = armSettingsInputStream;
	}

	public String getEndpoint() {
		return mEndpoint;
	}

	public void setEndpoint(String endpoint) {
		mEndpoint = endpoint;
	}
}
