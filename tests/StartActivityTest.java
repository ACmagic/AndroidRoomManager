import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Intent;
import android.os.Bundle;
import edu.cmu.sv.arm.StartActivityController.ConfigurationStatus;
import edu.cmu.sv.arm.AndroidRoomManagerMainActivity;
import edu.cmu.sv.arm.StartActivity;

@RunWith(RobolectricTestRunner.class)
public class StartActivityTest {
	
	private StartActivity startActivity;
	
	@Test
	public void shouldLaunchApplicationWhenParsingConfigurationSucceeds() throws Exception {
		Bundle savedBundle = new Bundle();
		startActivity = Robolectric.buildActivity( StartActivity.class ).create( savedBundle ).start().resume().get();
		
		startActivity.onTaskCompleted(ConfigurationStatus.USING_CUSTOM_SETTINGS_FILE);
	    Intent intent = Robolectric.shadowOf(startActivity).peekNextStartedActivity();
	    assertEquals(AndroidRoomManagerMainActivity.class.getCanonicalName(), intent.getComponent().getClassName());
	}

}
