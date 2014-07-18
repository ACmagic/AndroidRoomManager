package edu.cmu.sv.arm;

public class SensorReading {
	private String mTimeStamp;
	private String mValue;
	
	public SensorReading(String timeStamp, String value){
		this.setTimeStamp(timeStamp);
		this.setValue(value);
	}
	
	public String getTimeStamp() {
		return mTimeStamp;
	}
	public void setTimeStamp(String mTimeStamp) {
		this.mTimeStamp = mTimeStamp;
	}
	public String getValue() {
		return mValue;
	}
	public void setValue(String mValue) {
		this.mValue = mValue;
	}
}
