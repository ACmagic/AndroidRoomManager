import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Robolectric;

import android.os.AsyncTask;
import edu.cmu.sv.arm.BackendFacade;

//@RunWith(PowerMockRunner.class)
@RunWith(RobolectricTestRunner.class)
@PrepareForTest(BackendFacade.class)
public class BackendFacadeTests extends TestCase{	
	
	//private BackendFacade sdaspFacade;
	//private AsyncTask<String, Void, String> sdaspFacade = new BackendFacade("test", null);
	
	@Test
	public void testGetReturnsEmptyStringOnConnectionError() throws Exception{
		Robolectric.getBackgroundScheduler().pause();
		Robolectric.getUiThreadScheduler().pause();
		AsyncTask<String, Void, String> sdaspFacade = new BackendFacade("test", null);

		Robolectric.addPendingHttpResponse(200, "OK");
		
		//sdaspFacade = PowerMockito.spy(new BackendFacade("test", null));
		
		//Mock java.net.URL to return a null connection
//		URL url = PowerMockito.mock(URL.class);
//        PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
//                .withArguments(Mockito.anyString()).thenReturn(url);
//		PowerMockito.when(url.openConnection()).thenReturn(null);
//		
//		PowerMockito.doReturn("").when(sdaspFacade, "onPostExecute", "");
		
		sdaspFacade.execute("Room data");
		
//		PowerMockito.verifyPrivate(sdaspFacade).invoke("onPostExecute", "");
	}
	
//	@Test
//	public void testGetReturnsEmptyStringOnExceptionWithURL() throws Exception{
//		sdaspFacade = PowerMockito.spy(new BackendFacade("test", null));
//		
//		//Mock java.net.URL
//		PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
//         .withArguments(Mockito.anyString()).thenThrow(new MalformedURLException("testing!"));
//		PowerMockito.doReturn("").when(sdaspFacade, "onPostExecute", "");
//		sdaspFacade.execute("Room data");
//		PowerMockito.verifyPrivate(sdaspFacade).invoke("onPostExecute", "");
//	}
//	
//	@Test
//	public void testGetReturnsStringResponseOnConnectionSucess() throws Exception{	
//		sdaspFacade = PowerMockito.spy(new BackendFacade("test", null));
//		
//		//Mock java.net.URL
//		URL url = PowerMockito.mock(URL.class);
//		PowerMockito.whenNew(URL.class).withParameterTypes(String.class)
//         .withArguments(Mockito.anyString()).thenReturn(url);
//		
//		//Mock net.ssl.HttpsURLConnection;
//		HttpsURLConnection urlConnectionMock = PowerMockito.mock(HttpsURLConnection.class);
//        PowerMockito.whenNew(HttpsURLConnection.class).withParameterTypes(URL.class)
//        .withArguments(url).thenReturn(urlConnectionMock);
//        
//        //Mock backend response content
//        String sampleResponse = "Test";
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(sampleResponse.getBytes());
//		PowerMockito.when(url.openConnection()).thenReturn(urlConnectionMock);
//        PowerMockito.when(urlConnectionMock.getInputStream()).thenReturn(inputStream);
//        
//		PowerMockito.doReturn("").when(sdaspFacade, "onPostExecute", sampleResponse);
//
//        sdaspFacade.execute("Room data");
//		PowerMockito.verifyPrivate(sdaspFacade).invoke("onPostExecute", sampleResponse);
//	}
}
