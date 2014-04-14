package edu.cmu.sv.arm;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;


public class RoomInfoFragment extends Fragment implements AsyncTaskCompleteListener<String>{
	private ARM mAppState;
	private GridView mGridView;
	private RoomInfoAdapter mRoomInfoAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mAppState = ((ARM) getActivity().getApplication());
		View roomInfoView = inflater.inflate(R.layout.room_info, container, false);
		
		// http://www.rogcg.com/blog/2013/11/01/gridview-with-auto-resized-images-on-android
	    mGridView = (GridView) roomInfoView.findViewById(R.id.gridview);
	    mRoomInfoAdapter = new RoomInfoAdapter(roomInfoView.getContext());
	    mGridView.setAdapter(mRoomInfoAdapter);

		return roomInfoView;
	}
	
	@Override
	public void onResume() {
		super.onResume();			
		//mBackend.execute(mAppState.getCurrentRoom().getFullName());
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	// Used for resetting the camera fragment
	public void reset(boolean resetRoomInfo) {		
		if (resetRoomInfo) {
			//String endpoint = mAppState.getEndpoint() + "room"+ mAppState.getCurrentRoom().getNumber() + "/json";
			String endpoint = "http://einstein.sv.cmu.edu:9000/latestReadingFromDevicesByGeofence/room"+  mAppState.getCurrentRoom().getNumber() + "/json";
			BackendFacade backend = new BackendFacade(endpoint, this);
			backend.execute();
		}		
	}

	public void onTaskCompleted(String result) {
		List<Sensor> sensors = new ArrayList<Sensor>();
		sensors.add(new Sensor(this.mAppState.getCurrentRoom().getNumber(), R.drawable.icon_ok));
		if (! result.equals("")){
			JsonParser jsonParser = new JsonParser();
			JsonArray sensorsInRoom = (JsonArray)jsonParser.parse(result);
			for (JsonElement sensor: sensorsInRoom ){
				sensors.add(new Sensor(sensor.getAsJsonObject().get("value").getAsString(), R.drawable.icon_ok));
			}
		}
		this.mRoomInfoAdapter.setRoomSensorsInfo(sensors);
		this.mRoomInfoAdapter.notifyDataSetChanged();
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
            return sensors.get(i).drawableId;
        }

        public View getView(int i, View view, ViewGroup viewGroup)
        {
            View sensorsView = view;
            ImageView sensorImage;
            TextView sensorName;

            if(sensorsView == null)
            {
               sensorsView = mInflater.inflate(R.layout.room_info_item, viewGroup, false);
               sensorsView.setTag(R.id.picture, sensorsView.findViewById(R.id.picture));
               sensorsView.setTag(R.id.text, sensorsView.findViewById(R.id.text));
            }

            sensorImage = (ImageView)sensorsView.getTag(R.id.picture);
            sensorName = (TextView)sensorsView.getTag(R.id.text);

            Sensor item = (Sensor)getItem(i);

            sensorImage.setImageResource(item.drawableId);
            sensorName.setText(item.name);

            return sensorsView;
        }
    }
	
	private class Sensor
	{
	    final String name;
	    final int drawableId;
	
	    public Sensor(String name, int drawableId)
	    {
	        this.name = name;
	        this.drawableId = drawableId;
	    }
	}
}
