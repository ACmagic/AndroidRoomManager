import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import junit.framework.TestCase;

import org.apache.maven.artifact.ant.shaded.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;

import android.os.Bundle;
import android.os.Environment;
import edu.cmu.sv.arm.ARM;
import edu.cmu.sv.arm.StartActivity;
import edu.cmu.sv.arm.StartActivityController;
import edu.cmu.sv.arm.StartActivityController.ConfigurationStatus;

@RunWith(RobolectricTestRunner.class)
@PrepareForTest(StartActivityController.class)
public class StartActivityControllerTest extends TestCase{
	
	@Test
	public void testReturnsMediaErrorOnErrorReadingConfigurationFile() throws Exception{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
		StartActivityController startActivityController = PowerMockito.spy(new StartActivityController(null,
				null));
	    PowerMockito.doReturn(false).when(startActivityController,"parseConfiguration");
    	assertEquals(ConfigurationStatus.READ_MEDIA_ERROR, startActivityController.configureApplication());
	}
	
	@Test
	public void testReturnsMediaErrorOnMediaUnmounted(){
		StartActivityController startActivityController = new StartActivityController(null,
				null);
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);
    	assertEquals(ConfigurationStatus.READ_MEDIA_ERROR, startActivityController.configureApplication());
	}
	
	@Test
	public void testReturnsUnexpectedErrorOnExceptionParsingFile() throws Exception{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
		StartActivityController startActivityController = PowerMockito.spy(new StartActivityController(null,
				null));
	    //PowerMockito.doThrow(new Exception()).when(startActivityController, "parseConfiguration");
		PowerMockito.doThrow(new Exception()).when(startActivityController, "parseConfiguration");
    	assertEquals(ConfigurationStatus.UNEXPECTED_ERROR, startActivityController.configureApplication());
	}
	
	@Test
	public void testReturnsSuccessMsgOnSuccessfullyReadingConfigurationFile() throws Exception{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
		StartActivityController startActivityController = PowerMockito.spy(new StartActivityController(null,
				null));
	    PowerMockito.doReturn(true).when(startActivityController, "parseConfiguration");
		assertEquals(ConfigurationStatus.USING_CUSTOM_SETTINGS_FILE, startActivityController.configureApplication());
	}
	
	@Test
	public void testReturnsSuccessMsgOnSuccessfullyReadingConfigurationFileAndReadOnlyMediaMounted() throws Exception{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED_READ_ONLY);
		StartActivityController startActivityController = PowerMockito.spy(new StartActivityController(null,
				null));
	    PowerMockito.doReturn(true).when(startActivityController, "parseConfiguration");
		assertEquals(ConfigurationStatus.USING_CUSTOM_SETTINGS_FILE, startActivityController.configureApplication());
	}
	
	@Rule
	  public TemporaryFolder tempfolder = new TemporaryFolder();


	@Test
	public void testSetEndpointToARMOnSuccessParsingConfigurationFile() throws Exception{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
		
		Bundle savedBundle = new Bundle();
		StartActivity startActivity = Robolectric.buildActivity( StartActivity.class ).create( savedBundle ).start().resume().get();
		StartActivityController startActivityController = PowerMockito.spy(new StartActivityController(startActivity.getApplication(),
				null));	
		
		//Creating temp file
		File tempFile = tempfolder.newFile("arm_settings.xml");
	    BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
	    String sampleEndpoint = "test";
	    String sampleXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
	    +"<!-- Create /ARM/arm_settings.xml file on device's storage and use the format below for the settings --> \n"
	    +"<!-- Rooms are not required to have images, but all other options are required -->\n"
	    +"<settings>\n"
	        +"<application\n"
	            +"title=\"Android Room Manager test\"\n"
	            +"endpoint = \""+ sampleEndpoint +"\"\n"
	            +"google_account=\"account_email@address.com\"\n" 
	            +"google_calendar_api_key=\"abcdefg000GoogleAPIKey000hijklmnop\"\n"
	            +"timeout_minutes=\"4\"\n"
	            +"timeout_seconds=\"30\"\n"
	            +"calendar_refresh_delay_minutes=\"1\"\n"
	            +"calendar_refresh_delay_seconds=\"30\" />\n"
	        +"<building number=\"1\">\n"
	            +"<room number=\"100\"\n" 
	              +"resource_address=\"google_resource_address_100@resource.calendar.google.com\">\n"
	                +"<image url=\"http://url_to_image.com/image.jpg\"\n"
	                     +"refresh_delay_minutes=\"0\"\n"
	                     +"refresh_delay_seconds=\"0\"\n"
	                     +"refresh_delay_milliseconds=\"500\" />\n"
	            +"</room>\n"
	        +"</building>\n"
	    +"</settings>\n";
        out.write(sampleXml);
        out.close();
	    PowerMockito.doReturn(tempFile).when(startActivityController, "getConfigurationFile");
	    startActivityController.configureApplication();
	    assertEquals(sampleEndpoint, startActivityController.getAppState().getEndpoint());    
	}
}
