import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;

import android.os.Environment;
import edu.cmu.sv.arm.StartActivityController;
import edu.cmu.sv.arm.StartActivityController.ConfigurationStatus;

@RunWith(RobolectricTestRunner.class)
@PrepareForTest(StartActivityController.class)
public class StartActivityControllerTest extends TestCase{
	
	@Test
	public void returnsMediaErrorOnErrorReadingConfigurationFile() throws Exception{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
		StartActivityController startActivityController = PowerMockito.spy(new StartActivityController(null,
				null));
	    PowerMockito.doReturn(false).when(startActivityController, "parseConfiguration");
    	assertEquals(ConfigurationStatus.READ_MEDIA_ERROR, startActivityController.configureApplication());
	}
	
	@Test
	public void returnsMediaErrorOnMediaUnmounted(){
		StartActivityController startActivityController = new StartActivityController(null,
				null);
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);
    	assertEquals(ConfigurationStatus.READ_MEDIA_ERROR, startActivityController.configureApplication());
	}
	
	@Test
	public void returnsUnexpectedErrorOnExceptionParsingFile() throws Exception{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
		StartActivityController startActivityController = PowerMockito.spy(new StartActivityController(null,
				null));
	    PowerMockito.doThrow(new Exception()).when(startActivityController, "parseConfiguration");
    	assertEquals(ConfigurationStatus.UNEXPECTED_ERROR, startActivityController.configureApplication());
	}
	
	@Test
	public void returnsSuccessMsgOnSuccessfullyReadingConfigurationFile() throws Exception{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
		StartActivityController startActivityController = PowerMockito.spy(new StartActivityController(null,
				null));
	    PowerMockito.doReturn(true).when(startActivityController, "parseConfiguration");
		assertEquals(ConfigurationStatus.USING_CUSTOM_SETTINGS_FILE, startActivityController.configureApplication());
	}
	
	@Test
	public void returnsSuccessMsgOnSuccessfullyReadingConfigurationFileAndReadOnlyMediaMounted() throws Exception{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED_READ_ONLY);
		StartActivityController startActivityController = PowerMockito.spy(new StartActivityController(null,
				null));
	    PowerMockito.doReturn(true).when(startActivityController, "parseConfiguration");
		assertEquals(ConfigurationStatus.USING_CUSTOM_SETTINGS_FILE, startActivityController.configureApplication());
	}
}
