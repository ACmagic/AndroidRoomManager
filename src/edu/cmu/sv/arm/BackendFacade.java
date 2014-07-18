package edu.cmu.sv.arm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

public class BackendFacade extends AsyncTask <String, Void, String>{
	
	private String endpoint;
	private AsyncTaskCompleteListener<String[]> mTaskCompletedCallback;
	
	public BackendFacade(String endpoint, AsyncTaskCompleteListener<String[]> callback){
		this.endpoint = endpoint;
		this.mTaskCompletedCallback = callback;
	}
	
	//Check if String as parameter is enough
	public String getResourceInfo(String data){
		return getResourceInfoFromBackend(data);
	}
		
	private String getResourceInfoFromBackend(String data){
      URL url;
      try {
	     url = new URL(endpoint);
	     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	     
	     //dump all the content
	     return getResponseContent(connection);
 
      } catch (MalformedURLException e) {
    	  // Define what to do
    	  e.printStackTrace();
      } catch (IOException e) {
    	  // Define what to do
    	  e.printStackTrace();
      }
      return new String();
	}
	
	private String getResponseContent(HttpURLConnection connection){
		StringBuffer stringBuffer = new StringBuffer();
		if(connection!=null){
			try {			
			   BufferedReader br = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			   String input;
		 
			   while ((input = br.readLine()) != null){
			      stringBuffer.append(input);
			   }
			   br.close();
		 
			} catch (IOException e) {
			   e.printStackTrace();
			   return new String();
			}
		}
		return new String(stringBuffer.toString());
	}

	@Override
	protected String doInBackground(String... arg0) {
		return getResourceInfo("");
	}
	
	@Override
	protected void onPostExecute(String response){
		super.onPostExecute(response);
		String[] responseInfo = new String[2];
		responseInfo[0] = endpoint;
		responseInfo[1] = response;
		this.mTaskCompletedCallback.onTaskCompleted(responseInfo);
	}
}
