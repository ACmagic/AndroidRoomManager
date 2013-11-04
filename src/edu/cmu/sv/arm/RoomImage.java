package edu.cmu.sv.arm;

public class RoomImage {
	private String mURL;
	
	private int mRefreshDelayMinutes = 0;
	private int mRefreshDelaySeconds = 0;
	private int mRefreshDelayMilliseconds = 0;
	
	public RoomImage(String URL) {
		mURL = URL;
	}
	
	public String getURL() {
		return mURL;
	}
	
	public void setURL(String URL) {
		this.mURL = URL;
	}

	public int getRefreshDelayMinutes() {
		return mRefreshDelayMinutes;
	}

	public void setRefreshDelayMinutes(int refreshDelayMinutes) {
		this.mRefreshDelayMinutes = refreshDelayMinutes;
	}

	public int getRefreshDelaySeconds() {
		return mRefreshDelaySeconds;
	}

	public void setRefreshDelaySeconds(int refreshDelaySeconds) {
		this.mRefreshDelaySeconds = refreshDelaySeconds;
	}

	public int getRefreshDelayMilliseconds() {
		return mRefreshDelayMilliseconds;
	}

	public void setRefreshDelayMilliseconds(int refreshDelayMilliseconds) {
		this.mRefreshDelayMilliseconds = refreshDelayMilliseconds;
	}
}
