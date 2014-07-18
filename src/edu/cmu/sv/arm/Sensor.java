package edu.cmu.sv.arm;

import java.util.ArrayList;

public class Sensor implements Comparable<Sensor>{
	private String mName;
	private ArrayList<SensorReading> mLatestReadings = new ArrayList<SensorReading>();
	private String mType;
	private String mValue;
	private int mDrawableId;

	public Sensor(String name, int sensorIcon, String value) {
		this.setName(name);
		this.setDrawableId(sensorIcon);
		this.setValue(value);
	}

	public Sensor() {
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}
	
	public void addReading(SensorReading latestReadings){
		this.mLatestReadings.add(latestReadings);
	}
	
	public ArrayList<SensorReading> getLatestReadings(){
		return this.mLatestReadings;
	}

	public String getType() {
		return mType;
	}

	public void setType(String mType) {
		this.mType = mType;
	}
	
	public int compareTo(Sensor another) {
		return this.getName().compareTo(another.getName());
	}

	public int getDrawableId() {
		return mDrawableId;
	}

	public void setDrawableId(int mDrawableId) {
		this.mDrawableId = mDrawableId;
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String mValue) {
		this.mValue = mValue;
	}
}
