import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import edu.cmu.sv.arm.AndroidRoomManagerMainActivity;
import edu.cmu.sv.arm.R;
		
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    @Test
    public void shouldHaveAppName() throws Exception {
        String hello = new AndroidRoomManagerMainActivity().getResources().getString(R.string.app_name);
        assertThat(hello, equalTo("ARM"));
    }
}

