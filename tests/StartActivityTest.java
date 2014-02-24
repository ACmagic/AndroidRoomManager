import static org.junit.Assert.assertEquals;
import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import edu.cmu.sv.arm.R;
import edu.cmu.sv.arm.StartActivityController.ConfigurationStatus;
import edu.cmu.sv.arm.AndroidRoomManagerMainActivity;
import edu.cmu.sv.arm.StartActivity;

@RunWith(RobolectricTestRunner.class)
public class StartActivityTest{

	private StartActivity startActivity;
	
	@Test
	public void shouldLaunchApplicationWhenParsingConfigurationSucceeds() throws Exception {
		Bundle savedBundle = new Bundle();
		startActivity = Robolectric.buildActivity( StartActivity.class ).create( savedBundle ).start().resume().get();
		
		startActivity.onTaskCompleted(ConfigurationStatus.USING_CUSTOM_SETTINGS_FILE);
	    Intent intent = Robolectric.shadowOf(startActivity).peekNextStartedActivity();
	    assertEquals(AndroidRoomManagerMainActivity.class.getCanonicalName(), intent.getComponent().getClassName());
	}
	
	@Test
	public void shouldNotLaunchApplicationWhenParsingConfigurationFails() throws Exception {
		Bundle savedBundle = new Bundle();
		startActivity = Robolectric.buildActivity( StartActivity.class ).create( savedBundle ).start().resume().get();
		
		// DEFAULT SETTINGS
		startActivity.onTaskCompleted(ConfigurationStatus.USING_DEFAULT_SETTINGS_FILE);
		Button startButton = (Button) startActivity.findViewById(R.id.startButton);
	    assertEquals(startButton.isEnabled(), false);
	    
		// READ_MEDIA_ERROR
		startActivity.onTaskCompleted(ConfigurationStatus.READ_MEDIA_ERROR);
		startButton = (Button) startActivity.findViewById(R.id.startButton);
	    assertEquals(startButton.isEnabled(), false);

		// UNEXPECTED_ERROR
		startActivity.onTaskCompleted(ConfigurationStatus.UNEXPECTED_ERROR);
		startButton = (Button) startActivity.findViewById(R.id.startButton);
	    assertEquals(startButton.isEnabled(), false);

	}
	
	

}
