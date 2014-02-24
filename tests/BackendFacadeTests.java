import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;

import edu.cmu.sv.arm.BackendFacade;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BackendFacade.class)
//@PrepareForTest({ URL.class })
public class BackendFacadeTests{
	
	private BackendFacade sdaspFacade = new BackendFacade();
	
	@Test
	public void testGetReturnsEmptyStringOnConnectionError() throws Exception{
		System.setProperty("dexmaker.dexcache","/sdcard");
		URL url = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
                .withArguments(Mockito.anyString()).thenReturn(url);
		PowerMockito.when(url.openConnection()).thenReturn(null);
		
        sdaspFacade.getResourceInfo("testData");
		assertEquals("", sdaspFacade.getResourceInfo("Room data"));
	}
	
	@Test
	public void testGetReturnsStringResponseOnConnectionSucess() throws Exception{
		System.setProperty("dexmaker.dexcache","/sdcard");
		
		URL url = PowerMockito.mock(URL.class);
		PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
         .withArguments(Mockito.anyString()).thenReturn(url);
		 
		HttpsURLConnection urlConnectionMock = PowerMockito.mock(HttpsURLConnection.class);
        PowerMockito.whenNew(HttpsURLConnection.class).withParameterTypes(URL.class)
        .withArguments(url).thenReturn(urlConnectionMock);
        
        String sampleResponse = "Test";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(sampleResponse.getBytes());
		PowerMockito.when(url.openConnection()).thenReturn(urlConnectionMock);
        PowerMockito.when(urlConnectionMock.getInputStream()).thenReturn(inputStream);
        
		String result = sdaspFacade.getResourceInfo("testData");
		assertEquals(sampleResponse, result);

	}
}
