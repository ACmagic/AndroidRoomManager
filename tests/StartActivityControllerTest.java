import static org.junit.Assert.*;

//import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.powermock.api.easymock.PowerMock;
//import org.powermock.core.classloader.annotations.PrepareForTest;

import android.os.Bundle;
import android.os.Environment;
import edu.cmu.sv.arm.ARM;
import edu.cmu.sv.arm.StartActivity;
import edu.cmu.sv.arm.StartActivityController;
import edu.cmu.sv.arm.StartActivityController.ConfigurationStatus;

//@RunWith(RobolectricTestRunner.class)
//@PrepareForTest(StartActivityController.class)
public class StartActivityControllerTest{
	
//	ARM application;
//	private StartActivityController startActivityController = new StartActivityController(application,
//			null);
//	
//	@Test
//	public void returnsMediaErrorOnErrorReadingConfigurationFile(){
//		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);
//    	assertEquals(ConfigurationStatus.READ_MEDIA_ERROR, startActivityController.configureApplication());
//	}
//	
//	@Test
//	public void returnsSuccessMsgOnSuccessfullyReadingConfigurationFile(){
//		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
//		Bundle savedBundle = new Bundle();
//		StartActivity startActivity = Robolectric.buildActivity( StartActivity.class ).create( savedBundle ).start().resume().get();
//		
//		EasyMock.createMockBuilder(StartActivityController.class).addMockedMethod("parseConfiguration").createMock();
//		assertEquals(ConfigurationStatus.READ_MEDIA_ERROR, startActivityController.configureApplication());
//	}
}
