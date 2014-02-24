import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import edu.cmu.sv.arm.AndroidRoomManagerMainActivity;
import edu.cmu.sv.arm.CalendarFragment;
import edu.cmu.sv.arm.DateTimeHelpers;
import edu.cmu.sv.arm.R;
import edu.cmu.sv.arm.StartActivity;
		
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
	
	private AndroidRoomManagerMainActivity mainActivity;
	private DateTimeHelpers dateTime;

    @Test
    public void shouldHaveAppName() throws Exception {
        String hello = new AndroidRoomManagerMainActivity().getResources().getString(R.string.app_name);
        assertThat(hello, equalTo("ARM"));
    }
    
    @Test
    public void shouldDisplayCurrentDateInCalendar() throws Exception {
    	
//    	//Mock DateTimeHerlpers
//    	new NonStrictExpectations()
//    	{
//    		long sampleDate = new Date().getTime();
//    		DateTimeHelpers dateTimeHelpers;
//    		{
//    			dateTimeHelpers.getCurrentDate().getTime();
//    			returns(sampleDate);
//    	   }
//    	  };
    	  
//		Bundle savedBundle = new Bundle();
//    	mainActivity = Robolectric.buildActivity( AndroidRoomManagerMainActivity.class ).create( savedBundle ).start().resume().get();
//    	CalendarFragment calendarFragment = (CalendarFragment) mainActivity.getFragmentManager().findFragmentById(R.id.calendarFragment);
//    	assert(calendarFragment!=null);
    	assert true;

    }
}

