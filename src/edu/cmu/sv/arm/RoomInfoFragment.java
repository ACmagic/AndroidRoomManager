package edu.cmu.sv.arm;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.lang.Character;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class RoomInfoFragment extends Fragment implements AsyncTaskCompleteListener<String[]>{
	private ARM mAppState;
	private GridView mGridView;
	private RoomInfoAdapter mRoomInfoAdapter;
	private Hashtable<String, Integer> sensorIcons = new Hashtable<String, Integer>();
	protected ProgressDialog mProgressDialog;

	public RoomInfoFragment(){
		sensorIcons.put("fireImpXAccelerometer", R.drawable.icon_accelerometer);
		sensorIcons.put("fireImpYAccelerometer", R.drawable.icon_accelerometer);
		sensorIcons.put("fireImpZAccelerometer", R.drawable.icon_accelerometer);
		sensorIcons.put("fireImpMotion", R.drawable.icon_motion);
		sensorIcons.put("fireImpDigitalTemperature", R.drawable.icon_temperature);
		sensorIcons.put("fireImpLight", R.drawable.icon_light);
		sensorIcons.put("fireImpPressure", R.drawable.icon_pressure);
		sensorIcons.put("fireImpHumidity", R.drawable.icon_humidity);		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mAppState = ((ARM) getActivity().getApplication());
		View roomInfoView = inflater.inflate(R.layout.room_info, container, false);
		
		// http://www.rogcg.com/blog/2013/11/01/gridview-with-auto-resized-images-on-android
	    mGridView = (GridView) roomInfoView.findViewById(R.id.gridview);
	    mRoomInfoAdapter = new RoomInfoAdapter(roomInfoView.getContext());
	    mGridView.setAdapter(mRoomInfoAdapter);
	    
	    mGridView.setOnItemClickListener(new OnItemClickListener() {
	    	public void onItemClick(AdapterView parent, View v, int position, long id) {
	    		Sensor currentSensor = (Sensor) parent.getItemAtPosition(position);
	    		mProgressDialog = ProgressDialog.show(getActivity(), "", "Obtaining sensor readings...", true, true);
	    		String endpoint = buildEndpoint(currentSensor);
				BackendFacade backend = new BackendFacade(endpoint, RoomInfoFragment.this);
				backend.execute();
	    	}
	    	    
	    	});

		return roomInfoView;
	}
	
	private String buildEndpoint(Sensor currentSensor) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Date startDate;
		Date endDate;
		try {
			startDate = dateFormat.parse("01-01-2014");
			endDate = dateFormat.parse("01-01-2014");
		} catch (ParseException e) {
			//Setting to current date and one month interval
			startDate = new Date();
			Calendar c = Calendar.getInstance(); 
			c.setTime(startDate); 
			c.add(Calendar.MONTH, -3);
			endDate = c.getTime();
		}
		long startDateInEpoch = startDate.getTime()/ 1000L;
		long endDateInEpoch = endDate.getTime()/ 1000L;
		String endpoint =  mAppState.getEndpoint() + "getSensorReadingInRange/"+  currentSensor.getName() + "/"+ startDateInEpoch + "/" + endDateInEpoch + "/json";
		return endpoint;
	}
	
	@Override
	public void onResume() {
		super.onResume();			
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	// Used for resetting the camera fragment
	public void reset(boolean resetRoomInfo) {		
		if (resetRoomInfo) {
			String endpoint =  mAppState.getEndpoint() + "latestReadingFromDevicesByGeofence/room"+  mAppState.getCurrentRoom().getNumber() + "/json";
			BackendFacade backend = new BackendFacade(endpoint, this);
			backend.execute();
		}		
	}
	
	//Temp function to get Sensor type until API is ready. 
	private String getSensorTypeFromSensorName(String sensorName){
		char[] sensorNameChars = sensorName.toCharArray();
		for (int i = 0; i < sensorNameChars.length; i++) {
		    if (Character.isDigit(sensorNameChars[i])) {
		    	return sensorName.substring(0, i);
		    }
		}
		return sensorName;
	}
	
	private int getIconForSensor(String sensor){
		int sensorIcon = R.drawable.icon_sensor; //Default value just in case
		String sensorType = getSensorTypeFromSensorName(sensor);
		if(this.sensorIcons.containsKey(sensorType)){
			sensorIcon = this.sensorIcons.get(sensorType);
		}
		return sensorIcon;
	}

	public void onTaskCompleted(String[] result) {
		List<Sensor> sensors = new ArrayList<Sensor>();
		List<SensorReading> readings = new ArrayList<SensorReading>();
		JsonParser jsonParser = new JsonParser();
		if (! result[1].equals("")){
			JsonElement parsedResult = jsonParser.parse(result[1]);
			if(parsedResult !=null){
				JsonArray sensorsData = (JsonArray)parsedResult;
				for (JsonElement sensor: sensorsData ){
					String value = sensor.getAsJsonObject().get("value").getAsString();
					String timeStamp = sensor.getAsJsonObject().get("timeStamp").getAsString();
					SensorReading reading = new SensorReading(timeStamp, value);
					readings.add(reading);
					
					String name = sensor.getAsJsonObject().get("sensorName").getAsString();
					int icon = getIconForSensor(sensor.getAsJsonObject().get("sensorName").getAsString());					
					sensors.add(new Sensor(name, icon, value));
				}
				if(result[0].contains("latestReadingFromDevicesByGeofence")){
					this.mRoomInfoAdapter.setRoomSensorsInfo(sensors);
					this.mRoomInfoAdapter.notifyDataSetChanged();
				}
				else if(result[0].contains("getSensorReadingInRange")){
					DialogFragment dialog = new SensorsGraphicsDialog();
					String readingsAsJson = new Gson().toJson(readings);
					Bundle args = new Bundle();
					args.putString("Readings", readingsAsJson);
					if(this.mProgressDialog !=null){
						this.mProgressDialog.dismiss();
					}
					dialog.setArguments(args);
					dialog.show(getFragmentManager(), "SensorsGraphicsDialog");
				}
			}
		}
	}
	
		
	private class RoomInfoAdapter extends BaseAdapter
    {
        private List<Sensor> sensors = new ArrayList<Sensor>();
        private LayoutInflater mInflater;

        public RoomInfoAdapter(Context context)
        {
            mInflater = LayoutInflater.from(context);
        }
        
        public void setRoomSensorsInfo(List<Sensor> sensors){
        	this.sensors = sensors;
        }
        
        public void clearSensorInfo(){
        	this.sensors.clear();
        }

        public int getCount() {
            return sensors.size();
        }

        public Object getItem(int i)
        {
            return sensors.get(i);
        }

        public long getItemId(int i)
        {
            return sensors.get(i).getDrawableId();
        }

        public View getView(int i, View view, ViewGroup viewGroup)
        {
            View sensorsView = view;
            ImageView sensorImage;
            TextView sensorValue;

            if(sensorsView == null)
            {
               sensorsView = mInflater.inflate(R.layout.room_info_item, viewGroup, false);
               sensorsView.setTag(R.id.picture, sensorsView.findViewById(R.id.picture));
               sensorsView.setTag(R.id.text, sensorsView.findViewById(R.id.text));
            }

            sensorImage = (ImageView)sensorsView.getTag(R.id.picture);
            sensorValue = (TextView)sensorsView.getTag(R.id.text);

            Sensor item = (Sensor)getItem(i);

            sensorImage.setImageResource(item.getDrawableId());
            sensorValue.setText(item.getValue());

            return sensorsView;
        }
    }
}
