package edu.cmu.sv.arm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class BackendFacade {
	
	//Read from configuration
	private String endpoint = "";
	
	//Check if String as parameter is enough
	public String getResourceInfo(String data){
		return getResourceInfoFromBackend(data);
	}
		
	private String getResourceInfoFromBackend(String data){
      URL url;
      try {
	     url = new URL(endpoint);
	     HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
	     
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
	
	private String getResponseContent(HttpsURLConnection connection){
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
		return stringBuffer.toString();
	}
}
