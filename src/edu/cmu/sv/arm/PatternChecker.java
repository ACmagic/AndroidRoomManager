package edu.cmu.sv.arm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternChecker {
	// Source for isValidEmail function:
	// StackOverflow user Droid: http://stackoverflow.com/users/304961/droid
	// Thread: http://stackoverflow.com/questions/6119722/how-to-check-edittexts-text-is-email-address-or-not
	public static boolean isValidEmail(String email) {
	    boolean isValid = false;

	    String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	    CharSequence inputStr = email;

	    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(inputStr);
	    
	    if (matcher.matches()) {
	        isValid = true;
	    }
	    
	    return isValid;
	}
}
