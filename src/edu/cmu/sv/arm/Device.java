package edu.cmu.sv.arm;

import java.util.ArrayList;

public class Device {
	private String uri;
	private String userDefinedFields;
	private float longitud;
	private float latitude;
	private float altitude;
	private String representation;
	private String typeName;
	private String manufacturer;
	private String version;
	private String typeUserDefinedFields;
	private ArrayList<Sensor> sensors;
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getUserDefinedFields() {
		return userDefinedFields;
	}
	public void setUserDefinedFields(String userDefinedFields) {
		this.userDefinedFields = userDefinedFields;
	}
	public float getLongitud() {
		return longitud;
	}
	public void setLongitud(float longitud) {
		this.longitud = longitud;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getAltitude() {
		return altitude;
	}
	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}
	public String getRepresentation() {
		return representation;
	}
	public void setRepresentation(String representation) {
		this.representation = representation;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getTypeUserDefinedFields() {
		return typeUserDefinedFields;
	}
	public void setTypeUserDefinedFields(String typeUserDefinedFields) {
		this.typeUserDefinedFields = typeUserDefinedFields;
	}
	public ArrayList<Sensor> getSensors() {
		return sensors;
	}
	public void setSensors(ArrayList<Sensor> sensors) {
		this.sensors = sensors;
	}

}
