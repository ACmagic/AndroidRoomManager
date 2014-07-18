package edu.cmu.sv.arm;

import java.util.ArrayList;

public class Room implements Comparable<Room>{
	private boolean mIsDefault;
	private String mBuilding;
	private String mNumber;
	private String mResourceAddress;
	private ArrayList<Device> mDevices;
	private String mCapacity;
	private String mFullName;
	
	public Room() {
		mIsDefault = false;
		mDevices = new ArrayList<Device>();
	}
	
	public void setFullName(){
		mFullName =  "Building " + mBuilding + ", Room " + mNumber;
	}
	public String getFullName() {
		return mFullName;
	}
	
	public boolean isDefault() {
		return mIsDefault;
	}
	
	public void setIsDefault(boolean isDefault) {
		this.mIsDefault = isDefault;
	}
	
	public String getBuilding() {
		return mBuilding;	
	}
	
	public void setBuilding(String building) {
		this.mBuilding = building;
	}
	
	public String getNumber() {
		return mNumber;
	}
	
	public void setNumber(String number) {
		this.mNumber = number;
	}
	
	public String getResourceAddress() {
		return mResourceAddress;
	}
	
	public void setResourceAddress(String resourceAddress) {
		this.mResourceAddress = resourceAddress;
	}
	
	public String getCapacity() {
		return mCapacity;
	}
	
	public void setCapacity(String capacity) {
		this.mCapacity = capacity;
	}
	
	public ArrayList<Device> getDevices() {
		return mDevices;
	}
	
	public void addDevice(Device device) {
		this.mDevices.add(device);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		
		Room objRoom = (Room) obj;
		
		return 	(this.isDefault() == objRoom.isDefault()) &&
				(this.getBuilding() == objRoom.getBuilding()) &&
				(this.getNumber() == objRoom.getNumber()) &&
				(this.getResourceAddress() == objRoom.getResourceAddress());
	}
	
	@Override
	public int hashCode() {
		int hashCode = (this.getBuilding() + this.getNumber()).hashCode();
		
		return hashCode;
	}
	
	 @Override
	    public String toString() {
	       return getFullName();
	    }

	public int compareTo(Room another) {
		return this.getNumber().compareTo(another.getNumber());
	} 
	
}
