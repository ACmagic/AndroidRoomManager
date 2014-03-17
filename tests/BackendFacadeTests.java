import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;

import edu.cmu.sv.arm.ARM;
import edu.cmu.sv.arm.BackendFacade;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BackendFacade.class)
public class BackendFacadeTests extends TestCase{	
	private BackendFacade sdaspFacade = new BackendFacade("test");
		
	@Test
	public void testGetReturnsEmptyStringOnConnectionError() throws Exception{
		
		//Mock java.net.URL to return a null connection
		URL url = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
                .withArguments(Mockito.anyString()).thenReturn(url);
		PowerMockito.when(url.openConnection()).thenReturn(null);
		
        sdaspFacade.getResourceInfo("testData");
		assertEquals("", sdaspFacade.getResourceInfo("Room data"));
	}
	
	@Test
	public void testGetReturnsEmptyStringOnExceptionWithURL() throws Exception{
		//Mock java.net.URL
		PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
         .withArguments(Mockito.anyString()).thenThrow(new MalformedURLException("testing!"));
		sdaspFacade.getResourceInfo("testData");
		assertEquals("", sdaspFacade.getResourceInfo("Room data"));
	}
	
	@Test
	public void testGetReturnsStringResponseOnConnectionSucess() throws Exception{	
		//Mock java.net.URL
		URL url = PowerMockito.mock(URL.class);
		PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
         .withArguments(Mockito.anyString()).thenReturn(url);
		
		//Mock net.ssl.HttpsURLConnection;
		HttpsURLConnection urlConnectionMock = PowerMockito.mock(HttpsURLConnection.class);
        PowerMockito.whenNew(HttpsURLConnection.class).withParameterTypes(URL.class)
        .withArguments(url).thenReturn(urlConnectionMock);
        
        //Mock backend response content
        String sampleResponse = "Test";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(sampleResponse.getBytes());
		PowerMockito.when(url.openConnection()).thenReturn(urlConnectionMock);
        PowerMockito.when(urlConnectionMock.getInputStream()).thenReturn(inputStream);
        
		String result = sdaspFacade.getResourceInfo("testData");
		assertEquals(sampleResponse, result);
	}
}
