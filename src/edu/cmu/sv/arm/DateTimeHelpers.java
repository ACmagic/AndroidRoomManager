package edu.cmu.sv.arm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import android.text.format.Time;

public class DateTimeHelpers {
	public static final int WEEK_IN_MILLISECONDS = 604800000;
	public static final int DAY_IN_MILLISECONDS = 86400000;
	public static final int HOUR_IN_MILLISECONDS = 3600000;
	public static final int MINUTE_IN_MILLISECONDS = 60000;
	public static final int SECOND_IN_MILLISECONDS = 1000;
	public static final int DAY_IN_MINUTES = 1440;
	public static final int HOUR_IN_MINUTES = 60;

	// Converts year, month, and day to string of form "month-day-year"
	public static String yemodaToString(int year, int month, int day) {
		int moplus1 = month + 1;

		String mo = Integer.toString(moplus1);

		if (moplus1 < 10) {
			mo = "0" + mo;
		}

		String da = Integer.toString(day);

		if (day < 10) {
			da = "0" + da;
		}

		String ye = Integer.toString(year);

		return mo + "-" + da + "-" + ye;
	}

	// Converts hour and minute to string of form "hour:minute am/pm"
	public static String homiToString(int hour, int minute) {
		String ho = "12";
		String mi = "00";
		String pmam = "AM";

		if (hour == 0) {
			ho = "12";
			mi = Integer.toString(minute);
			pmam = "AM";
		}
		else if (hour >= 1 && hour <= 11) {
			ho = Integer.toString(hour);
			mi = Integer.toString(minute);
			pmam = "AM";
		}
		else if (hour == 12) {
			ho = "12";
			mi = Integer.toString(minute);
			pmam = "PM";
		}
		else if (hour >= 13 && hour <= 23) {
			ho = Integer.toString(hour - 12);
			mi = Integer.toString(minute);
			pmam = "PM";
		}
		else if (hour == 24) {
			ho = "12";
			mi = Integer.toString(minute);
			pmam = "AM";
		}

		if (hour < 10) {
			ho = "0" + ho;
		}

		if (minute < 10) {
			mi = "0" + mi;
		}

		return ho + ":" + mi + " " + pmam;
	}

	// Tokenizes date into individual integers
	public static int[] tokenizeDate(String date) {
		StringTokenizer st = new StringTokenizer(date, "-/");

		ArrayList<Integer> al = new ArrayList<Integer>();

		while(st.hasMoreTokens()) {
			al.add(Integer.parseInt(st.nextToken()));
		}

		int[] tokenList = new int[3];

		for (int i = 0; i < al.size(); i++) {
			tokenList[i] = al.get(i);
		}

		return tokenList;
	}

	// Tokenizes time into individual integers (PM is 1, AM is 0)
	public static int[] tokenizeTime(String time) {
		String newTime = time.replaceAll(":", " ").replaceAll("PM", "1").replaceAll("AM", "0");	// 11:27 PM will become "11 17 1"

		StringTokenizer st = new StringTokenizer(newTime, " ");

		ArrayList<Integer> al = new ArrayList<Integer>();

		while(st.hasMoreTokens()) {
			al.add(Integer.parseInt(st.nextToken()));
		}

		int[] tokenList = new int[3];

		for (int i = 0; i < al.size(); i++) {
			tokenList[i] = al.get(i);
		}

		return tokenList;
	}

	// Converts a date and time into a date object
	public static Date dateTimeToDateObj(String date, String time) {
		java.util.Calendar cal = java.util.Calendar.getInstance();

		int[] dateTokens;
		dateTokens = tokenizeDate(date);
		cal.set(java.util.Calendar.YEAR, dateTokens[2]);
		cal.set(java.util.Calendar.MONTH, dateTokens[0] - 1);
		cal.set(java.util.Calendar.DATE, dateTokens[1]);

		int[] timeTokens;
		timeTokens = tokenizeTime(time);
		if (timeTokens[0] == 12) {
			timeTokens[0] -= 12;
		}
		cal.set(java.util.Calendar.HOUR, timeTokens[0]);
		cal.set(java.util.Calendar.MINUTE, timeTokens[1]);
		cal.set(java.util.Calendar.SECOND, 0);
		cal.set(java.util.Calendar.MILLISECOND, 0);
		cal.set(java.util.Calendar.AM_PM, timeTokens[2]);

		return new Date(cal.getTimeInMillis());
	}

	// Returns true if the ending date and time is after the starting date and time
	public static boolean isDateTimeAfter(String startDate, String startTime, String endDate, String endTime) {
		Date startD = dateTimeToDateObj(startDate, startTime);
		Date endD = dateTimeToDateObj(endDate, endTime);

		if (startD.getTime() >= endD.getTime()) {
			return false;
		}
		else {
			return true;
		}
	}

	// Returns the date and time that is a given amount of milliseconds after the input date and time
	public static String[] millisecondsAfter(String date, String time, int milliseconds) {
		Date d = dateTimeToDateObj(date, time);

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime(d);

		Time msAfter = new Time();

		msAfter.set(cal.getTimeInMillis() + milliseconds);


		String[] oha = { msAfter.format("%m-%d-%Y"), msAfter.format("%I:%M %p").toUpperCase() }; 

		return oha;
	}

	// Returns the date and time that is one hour after the input date and time, but to the nearest 15 minutes
	public static String[] millisecondsAfterNearestFifteenMinutes(String date, String time, int milliseconds) {
		Date d = dateTimeToDateObj(date, time);

		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.setTime(d);

		Time msAfter = new Time();

		msAfter.set(cal.getTimeInMillis() + milliseconds);

		msAfter = nearestFifteenMinutes(msAfter);


		String[] oha = { msAfter.format("%m-%d-%Y"), msAfter.format("%I:%M %p").toUpperCase() }; 

		return oha;
	}

	public static Time nearestFifteenMinutes(Time t) {
		Time nearestFifteen = t;

		if (t.minute < 8) {
			t.minute = 0;
		}
		else if (t.minute < 23) {
			t.minute = 15;
		}
		else if (t.minute < 38) {
			t.minute = 30;
		}
		else if (t.minute < 53) {
			t.minute = 45;
		}
		else {
			t.minute = 0;
			t.hour++;
		}

		return nearestFifteen;
	}

	public static int getMinutesSinceStartOfDay() {
		Calendar c = Calendar.getInstance();
		int hours = c.get(Calendar.HOUR);
		int ampm = c.get(Calendar.AM_PM);

		if (ampm == Calendar.PM) {
			hours += 12;
		}

		int minutes = c.get(Calendar.MINUTE);

		return hours * 60 + minutes;
	}

	public static int getDayOfTheWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getTime());

		return c.get(Calendar.DAY_OF_WEEK);
	}

	public static int getMinutesSinceStartOfDay(long milliseconds) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(milliseconds);

		int hours = c.get(Calendar.HOUR);
		int ampm = c.get(Calendar.AM_PM);

		if (ampm == Calendar.PM) {
			hours += 12;
		}

		int minutes = c.get(Calendar.MINUTE);

		return hours * 60 + minutes;
	}

	public static Date getDateOfLastSunday(Date d) {		
		Calendar c = Calendar.getInstance();
		c.setTime(d);

		int weekday = c.get(Calendar.DAY_OF_WEEK);

		if (weekday != Calendar.SUNDAY) {  
			int days = (Calendar.SUNDAY - weekday) % 7;
			c.add(Calendar.DAY_OF_YEAR, days);
		}

		c.set(Calendar.HOUR, 0);
		c.set(Calendar.AM_PM, Calendar.AM);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		Date date = c.getTime();

		return date;
	}

	public static Date getDateInAWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		c.add(Calendar.DAY_OF_MONTH, 7);

		Date ret = c.getTime();

		return ret;
	}

	public static Date getDateAWeekAgo(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		c.add(Calendar.DAY_OF_MONTH, -7);

		Date ret = c.getTime();

		return ret;
	}

	public static Date getCurrentDate() {
		Calendar c = Calendar.getInstance();

		Date date = c.getTime(); 

		return date;
	}

	public static int getCurrentHour() {
		Calendar c = Calendar.getInstance();

		return c.get(Calendar.HOUR_OF_DAY);
	}

	// Returns the amount of milliseconds remaining until the next minute
	public static int getMillisecondsUntilNextMinute() {
		Calendar c = Calendar.getInstance();
		long ms = c.getTimeInMillis();
		long nextminute = (ms + (MINUTE_IN_MILLISECONDS - 1)) / MINUTE_IN_MILLISECONDS * MINUTE_IN_MILLISECONDS;

		int result = (int) (nextminute - ms);

		return result;
	}

	// Returns whether the date "now" is within a week of the date "Sunday"
	public static boolean isWithinTheWeek(Date sunday, Date now) {
		long sun = sunday.getTime();
		long today = now.getTime();

		if (today >= sun && today <= sun + WEEK_IN_MILLISECONDS) {
			return true;
		}

		return false;
	}
}
