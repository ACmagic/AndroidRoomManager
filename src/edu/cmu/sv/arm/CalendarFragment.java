package edu.cmu.sv.arm;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.google.api.services.calendar.model.TimePeriod;

public class CalendarFragment extends Fragment {
	private ARM mAppState;
	
	private Handler mDateTimeMarkersUpdateHandler;
	private Runnable mDateTimeMarkersUpdater;
	
	private Handler mEventsUpdateHandler;
	private Runnable mEventsUpdater;
	
	private View mCalendarView;
	
	private ScrollView mCalendarScrollView;
	
	private LinearLayout mCurrentTimeMarkerLinearLayout;
	
	private View mDayMarkerView;
	
	private View mTouchView;
	
	private TextView mCurrentMonthTextView;
	private TextView mCurrentYearTextView;
	
	private TextView mSundayTextView;
	private TextView mMondayTextView;
	private TextView mTuesdayTextView;
	private TextView mWednesdayTextView;
	private TextView mThursdayTextView;
	private TextView mFridayTextView;
	private TextView mSaturdayTextView;
	
	private TextView mSundayDateTextView;
	private TextView mMondayDateTextView;
	private TextView mTuesdayDateTextView;
	private TextView mWednesdayDateTextView;
	private TextView mThursdayDateTextView;
	private TextView mFridayDateTextView;
	private TextView mSaturdayDateTextView;
	
	private RelativeLayout mSundayRelativeLayout;
	private RelativeLayout mMondayRelativeLayout;
	private RelativeLayout mTuesdayRelativeLayout;
	private RelativeLayout mWednesdayRelativeLayout;
	private RelativeLayout mThursdayRelativeLayout;
	private RelativeLayout mFridayRelativeLayout;
	private RelativeLayout mSaturdayRelativeLayout;
	
	private RelativeLayout mSundayHeaderRelativeLayout;
	private RelativeLayout mMondayHeaderRelativeLayout;
	private RelativeLayout mTuesdayHeaderRelativeLayout;
	private RelativeLayout mWednesdayHeaderRelativeLayout;
	private RelativeLayout mThursdayHeaderRelativeLayout;
	private RelativeLayout mFridayHeaderRelativeLayout;
	private RelativeLayout mSaturdayHeaderRelativeLayout;
	
	private Date mCurrentDate;
	private Date mShownSundayDate;
	private Date mSelectedDate;
	
	private int mTouchedY;
	
	private LinearLayout mRefreshLinearLayout;
	private View mRefreshView;
	
	private int mRefreshingCount;
	
	private WeeklyCalendarEventRetrieverTask mWCERT;
	
	private Animation mFadeOut;
	private Animation mFadeIn;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mCalendarView = inflater.inflate(R.layout.calendar, container, false);
		
		mAppState = ((ARM) getActivity().getApplication());
		
		mCalendarScrollView = (ScrollView) mCalendarView.findViewById(R.id.calendarScrollView);
		
		mCurrentTimeMarkerLinearLayout = (LinearLayout) mCalendarView.findViewById(R.id.currentTimeMarkerLinearLayout);
		
		mDayMarkerView = mCalendarView.findViewById(R.id.dayMarkerView);
		
		mCurrentMonthTextView = (TextView) mCalendarView.findViewById(R.id.currentMonthTextView);
		mCurrentYearTextView = (TextView) mCalendarView.findViewById(R.id.currentYearTextView);
		
		mSundayTextView = (TextView) mCalendarView.findViewById(R.id.sundayTextView);
		mMondayTextView = (TextView) mCalendarView.findViewById(R.id.mondayTextView);
		mTuesdayTextView = (TextView) mCalendarView.findViewById(R.id.tuesdayTextView);
		mWednesdayTextView = (TextView) mCalendarView.findViewById(R.id.wednesdayTextView);
		mThursdayTextView = (TextView) mCalendarView.findViewById(R.id.thursdayTextView);
		mFridayTextView = (TextView) mCalendarView.findViewById(R.id.fridayTextView);
		mSaturdayTextView = (TextView) mCalendarView.findViewById(R.id.saturdayTextView);
		
		mSundayDateTextView = (TextView) mCalendarView.findViewById(R.id.sundayDateTextView);
		mMondayDateTextView = (TextView) mCalendarView.findViewById(R.id.mondayDateTextView);
		mTuesdayDateTextView = (TextView) mCalendarView.findViewById(R.id.tuesdayDateTextView);
		mWednesdayDateTextView = (TextView) mCalendarView.findViewById(R.id.wednesdayDateTextView);
		mThursdayDateTextView = (TextView) mCalendarView.findViewById(R.id.thursdayDateTextView);
		mFridayDateTextView = (TextView) mCalendarView.findViewById(R.id.fridayDateTextView);
		mSaturdayDateTextView = (TextView) mCalendarView.findViewById(R.id.saturdayDateTextView);
		
		mSundayRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.sundayRelativeLayout);
		mMondayRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.mondayRelativeLayout);
		mTuesdayRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.tuesdayRelativeLayout);
		mWednesdayRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.wednesdayRelativeLayout);
		mThursdayRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.thursdayRelativeLayout);
		mFridayRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.fridayRelativeLayout);
		mSaturdayRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.saturdayRelativeLayout);
		
		mSundayHeaderRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.sundayHeaderRelativeLayout);
		mMondayHeaderRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.mondayHeaderRelativeLayout);
		mTuesdayHeaderRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.tuesdayHeaderRelativeLayout);
		mWednesdayHeaderRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.wednesdayHeaderRelativeLayout);
		mThursdayHeaderRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.thursdayHeaderRelativeLayout);
		mFridayHeaderRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.fridayHeaderRelativeLayout);
		mSaturdayHeaderRelativeLayout = (RelativeLayout) mCalendarView.findViewById(R.id.saturdayHeaderRelativeLayout);
		
		mCurrentDate = DateTimeHelpers.getCurrentDate();
		mShownSundayDate = DateTimeHelpers.getDateOfLastSunday(mCurrentDate);
		mSelectedDate = DateTimeHelpers.getCurrentDate();
		
		mRefreshLinearLayout = (LinearLayout) mCalendarView.findViewById(R.id.refreshLinearLayout);
		mRefreshView = (View) mCalendarView.findViewById(R.id.refreshView);
		mRefreshingCount = 0;
		
		resetCalendarScroll();
		
		updateEverything();
		
		mDateTimeMarkersUpdateHandler = new Handler();
		
		mDateTimeMarkersUpdater = new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				Date oldCurrentDate = mCurrentDate;
				mCurrentDate = DateTimeHelpers.getCurrentDate();
		    	 
				if (mCurrentDate.getDate() != oldCurrentDate.getDate()) {
					updateDateMarker();
				}
		    	 
				updateTimeMarker();
		    	 
				mDateTimeMarkersUpdateHandler.postDelayed(mDateTimeMarkersUpdater, DateTimeHelpers.getMillisecondsUntilNextMinute());
			}
		};
		
		//startDateTimeMarkersUpdater();
		
		mEventsUpdateHandler = new Handler();
		
		mEventsUpdater = new Runnable() {
			public void run() {
				refreshEvents();
		    	 
				mEventsUpdateHandler.postDelayed(mEventsUpdater, DateTimeHelpers.getMillisecondsUntilNextMinute() + (PreferenceManager.getDefaultSharedPreferences(mAppState).getInt("calendarRefreshDelay", 5) - 1) * DateTimeHelpers.MINUTE_IN_MILLISECONDS);
			}
		};

		mTouchView = new View(getActivity());
		
		mTouchView.setBackgroundResource(R.color.cmu_red);
		
		setupLongClicks();
		
		mFadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
		mFadeOut.setDuration(200);
		
		mFadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
		mFadeIn.setDuration(200);
		
		return mCalendarView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mRefreshLinearLayout.setVisibility(View.INVISIBLE);
		mRefreshView.setVisibility(View.INVISIBLE);
		mRefreshingCount = 0;
		
		startDateTimeMarkersUpdater();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (mWCERT != null) {
			mWCERT.cancel(true);
			mWCERT = null;
		}
		
		stopDateTimeMarkersUpdater();
	}
	
	public void setupLongClicks() {
		mTouchedY = 0;
		
		mSundayRelativeLayout.setLongClickable(true);
		mMondayRelativeLayout.setLongClickable(true);
		mTuesdayRelativeLayout.setLongClickable(true);
		mWednesdayRelativeLayout.setLongClickable(true);
		mThursdayRelativeLayout.setLongClickable(true);
		mFridayRelativeLayout.setLongClickable(true);
		mSaturdayRelativeLayout.setLongClickable(true);
		
        
		OnTouchListener touchYrecorder = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					mTouchedY = (int) event.getY();
					
					if (mTouchView.getParent() != null) {
						((RelativeLayout) mTouchView.getParent()).removeView(mTouchView);
					}
					
					int touchViewHeight = DateTimeHelpers.HOUR_IN_MINUTES / 2;
					
					if (mTouchedY < DateTimeHelpers.DAY_IN_MINUTES - 30) {
						touchViewHeight = DateTimeHelpers.HOUR_IN_MINUTES;
					}
					
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, touchViewHeight);
					
					lp.setMargins(0, (mTouchedY / (DateTimeHelpers.HOUR_IN_MINUTES / 2)) * (DateTimeHelpers.HOUR_IN_MINUTES / 2), 1, 0);
					
					mTouchView.setLayoutParams(lp);
				}
				return false;
			}
		};
		
        mSundayRelativeLayout.setOnTouchListener(touchYrecorder);
        mMondayRelativeLayout.setOnTouchListener(touchYrecorder);
        mTuesdayRelativeLayout.setOnTouchListener(touchYrecorder);
        mWednesdayRelativeLayout.setOnTouchListener(touchYrecorder);
        mThursdayRelativeLayout.setOnTouchListener(touchYrecorder);
        mFridayRelativeLayout.setOnTouchListener(touchYrecorder);
        mSaturdayRelativeLayout.setOnTouchListener(touchYrecorder);
        
        mSundayRelativeLayout.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				processLongClick(0);
				
				mSundayRelativeLayout.addView(mTouchView);
				
				return true;
			}
		});
        
        mMondayRelativeLayout.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {			
				processLongClick(1);
				
				mMondayRelativeLayout.addView(mTouchView);
				
				return true;
			}
		});
        
        mTuesdayRelativeLayout.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				processLongClick(2);
				
				mTuesdayRelativeLayout.addView(mTouchView);
				
				return true;
			}
		});

		mWednesdayRelativeLayout.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				processLongClick(3);
				
				mWednesdayRelativeLayout.addView(mTouchView);
				
				return true;
			}
		});

		mThursdayRelativeLayout.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				processLongClick(4);
				
				mThursdayRelativeLayout.addView(mTouchView);
				
				return true;
			}
		});

		mFridayRelativeLayout.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				processLongClick(5);
				
				mFridayRelativeLayout.addView(mTouchView);
				
				return true;
			}
		});

		mSaturdayRelativeLayout.setOnLongClickListener(new OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				processLongClick(6);
				
				mSaturdayRelativeLayout.addView(mTouchView);
				
				return true;
			}
		});
	}
	
	private void processLongClick(int dayOffset) {
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(mShownSundayDate);
		
		cal.add(Calendar.DAY_OF_YEAR, dayOffset);
		
		// year
		// month
		// day_of_month
		// hour
		// minute
		int[] eventTime = new int[5];
		
		eventTime[0] = cal.get(Calendar.YEAR);
		eventTime[1] = cal.get(Calendar.MONTH);
		eventTime[2] = cal.get(Calendar.DAY_OF_MONTH);
		eventTime[3] = mTouchedY / DateTimeHelpers.HOUR_IN_MINUTES; // get hour of day by dividing by minutes
		
		if (eventTime[3] * DateTimeHelpers.HOUR_IN_MINUTES + DateTimeHelpers.HOUR_IN_MINUTES / 2 - 1 < mTouchedY) {
			eventTime[4] = 30;
		}
		else {
			eventTime[4] = 0;
		}
		
		Intent reserveRoomActivity = new Intent(getActivity().getBaseContext(), ReserveRoomActivity.class);
    	reserveRoomActivity.putExtra("quickReservation", false);
    	reserveRoomActivity.putExtra("eventTime", eventTime);
    	reserveRoomActivity.putExtra("selectedRoom", mAppState.getRooms().get(mAppState.getMainActivity().getActionBar().getSelectedNavigationIndex()));
		startActivity(reserveRoomActivity);
	}
	
	public void resetCalendarScroll() {
		mCalendarScrollView.post(new Runnable() {
			public void run() {
				int scrollingTo = (DateTimeHelpers.getCurrentHour() - 2) * 60 + 1;
				
				mCalendarScrollView.scrollTo(0, scrollingTo); // scrolls to about two to three hours ago
			}
		});
	}
	
	private void startDateTimeMarkersUpdater() {
		if (mDateTimeMarkersUpdater != null) {
			mDateTimeMarkersUpdater.run();
		}
	}
	
	private void stopDateTimeMarkersUpdater() {
		if (mDateTimeMarkersUpdateHandler != null && mDateTimeMarkersUpdater != null) {
			mDateTimeMarkersUpdateHandler.removeCallbacks(mDateTimeMarkersUpdater);
		}
	}
	
	public void startEventsUpdater() {
		if (mEventsUpdater != null) {
			mEventsUpdater.run();
		}
	}
	
	public void stopEventsUpdater() {
		if (mEventsUpdateHandler != null && mEventsUpdater != null) {
			mEventsUpdateHandler.removeCallbacks(mEventsUpdater);
			
			mRefreshLinearLayout.setVisibility(View.INVISIBLE);
			mRefreshView.setVisibility(View.INVISIBLE);
			mRefreshingCount = 0;
		}
	}
	
	public void restartEventsUpdater() {
		stopEventsUpdater();
		startEventsUpdater();
	}
	
	private void updateDateTexts() {
		long time = mShownSundayDate.getTime();
		
		Time t = new Time();
		t.set(time);
		mSundayDateTextView.setText(Integer.toString(t.monthDay));

        time += DateTimeHelpers.DAY_IN_MILLISECONDS;
        t.set(time);
        mMondayDateTextView.setText(Integer.toString(t.monthDay));
        
        time += DateTimeHelpers.DAY_IN_MILLISECONDS;
        t.set(time);
        mTuesdayDateTextView.setText(Integer.toString(t.monthDay));
        
        time += DateTimeHelpers.DAY_IN_MILLISECONDS;
        t.set(time);
        mWednesdayDateTextView.setText(Integer.toString(t.monthDay));
        
        time += DateTimeHelpers.DAY_IN_MILLISECONDS;
        t.set(time);
        mThursdayDateTextView.setText(Integer.toString(t.monthDay));
        
        time += DateTimeHelpers.DAY_IN_MILLISECONDS;
        t.set(time);
        mFridayDateTextView.setText(Integer.toString(t.monthDay));
        
        time += DateTimeHelpers.DAY_IN_MILLISECONDS;
        t.set(time);
        mSaturdayDateTextView.setText(Integer.toString(t.monthDay));
        mCurrentYearTextView.setText(Integer.toString(t.year));
        
        String currentMonth = "";
        
        switch (t.month) {
        case Calendar.JANUARY:
        	currentMonth = getString(R.string.jan);
        	break;
        case Calendar.FEBRUARY:
        	currentMonth = getString(R.string.feb);
        	break;
        case Calendar.MARCH:
        	currentMonth = getString(R.string.mar);
        	break;
        case Calendar.APRIL:
        	currentMonth = getString(R.string.apr);
        	break;
        case Calendar.MAY:
        	currentMonth = getString(R.string.may);
        	break;
        case Calendar.JUNE:
        	currentMonth = getString(R.string.jun);
        	break;
        case Calendar.JULY:
        	currentMonth = getString(R.string.jul);
        	break;
        case Calendar.AUGUST:
        	currentMonth = getString(R.string.aug);
        	break;
        case Calendar.SEPTEMBER:
        	currentMonth = getString(R.string.sep);
        	break;
        case Calendar.OCTOBER:
        	currentMonth = getString(R.string.oct);
        	break;
        case Calendar.NOVEMBER:
        	currentMonth = getString(R.string.nov);
        	break;
        case Calendar.DECEMBER:
        	currentMonth = getString(R.string.dec);
        	break;
        }
        
        mCurrentMonthTextView.setText(currentMonth);
	}
	
	private boolean isCurrentWeek() {
		return DateTimeHelpers.isWithinTheWeek(mShownSundayDate, mCurrentDate);
	}
	
	@SuppressWarnings("deprecation")
	private void updateDateMarker() {
		if (isCurrentWeek()) {
			switch(mCurrentDate.getDay() + 1) {
			case Calendar.SUNDAY:
				mSaturdayRelativeLayout.setBackgroundResource(0);
				mSaturdayHeaderRelativeLayout.setBackgroundResource(0);
				mSaturdayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mSaturdayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mSundayRelativeLayout.setBackgroundResource(R.color.pale_blue);
				mSundayHeaderRelativeLayout.setBackgroundResource(R.drawable.calendar_header_day_gradient_shape);
				mSundayDateTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				mSundayTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				break;
			case Calendar.MONDAY:
				mSundayRelativeLayout.setBackgroundResource(0);
				mSundayHeaderRelativeLayout.setBackgroundResource(0);
				mSundayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mSundayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mMondayRelativeLayout.setBackgroundResource(R.color.pale_blue);
				mMondayHeaderRelativeLayout.setBackgroundResource(R.drawable.calendar_header_day_gradient_shape);
				mMondayDateTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				mMondayTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				break;
			case Calendar.TUESDAY:
				mMondayRelativeLayout.setBackgroundResource(0);
				mMondayHeaderRelativeLayout.setBackgroundResource(0);
				mMondayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mMondayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mTuesdayRelativeLayout.setBackgroundResource(R.color.pale_blue);
				mTuesdayHeaderRelativeLayout.setBackgroundResource(R.drawable.calendar_header_day_gradient_shape);
				mTuesdayDateTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				mTuesdayTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				break;
			case Calendar.WEDNESDAY:
				mTuesdayRelativeLayout.setBackgroundResource(0);
				mTuesdayHeaderRelativeLayout.setBackgroundResource(0);
				mTuesdayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mTuesdayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mWednesdayRelativeLayout.setBackgroundResource(R.color.pale_blue);
				mWednesdayHeaderRelativeLayout.setBackgroundResource(R.drawable.calendar_header_day_gradient_shape);
				mWednesdayDateTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				mWednesdayTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				break;
			case Calendar.THURSDAY:
				mWednesdayRelativeLayout.setBackgroundResource(0);
				mWednesdayHeaderRelativeLayout.setBackgroundResource(0);
				mWednesdayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mWednesdayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mThursdayRelativeLayout.setBackgroundResource(R.color.pale_blue);
				mThursdayHeaderRelativeLayout.setBackgroundResource(R.drawable.calendar_header_day_gradient_shape);
				mThursdayDateTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				mThursdayTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				break;
			case Calendar.FRIDAY:
				mThursdayRelativeLayout.setBackgroundResource(0);
				mThursdayHeaderRelativeLayout.setBackgroundResource(0);
				mThursdayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mThursdayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mFridayRelativeLayout.setBackgroundResource(R.color.pale_blue);
				mFridayHeaderRelativeLayout.setBackgroundResource(R.drawable.calendar_header_day_gradient_shape);
				mFridayDateTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				mFridayTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				break;
			case Calendar.SATURDAY:
				mFridayRelativeLayout.setBackgroundResource(0);
				mFridayHeaderRelativeLayout.setBackgroundResource(0);
				mFridayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mFridayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
				mSaturdayRelativeLayout.setBackgroundResource(R.color.pale_blue);
				mSaturdayHeaderRelativeLayout.setBackgroundResource(R.drawable.calendar_header_day_gradient_shape);
				mSaturdayDateTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				mSaturdayTextView.setTextColor(getResources().getColor(R.color.darker_gray));
				break;
			}
		}
		else {
			mSundayRelativeLayout.setBackgroundResource(0);
			mSundayHeaderRelativeLayout.setBackgroundResource(0);
			mSundayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mSundayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mMondayRelativeLayout.setBackgroundResource(0);
			mMondayHeaderRelativeLayout.setBackgroundResource(0);
			mMondayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mMondayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mTuesdayRelativeLayout.setBackgroundResource(0);
			mTuesdayHeaderRelativeLayout.setBackgroundResource(0);
			mTuesdayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mTuesdayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mWednesdayRelativeLayout.setBackgroundResource(0);
			mWednesdayHeaderRelativeLayout.setBackgroundResource(0);
			mWednesdayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mWednesdayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mThursdayRelativeLayout.setBackgroundResource(0);
			mThursdayHeaderRelativeLayout.setBackgroundResource(0);
			mThursdayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mThursdayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mFridayRelativeLayout.setBackgroundResource(0);
			mFridayHeaderRelativeLayout.setBackgroundResource(0);
			mFridayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mFridayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mSaturdayRelativeLayout.setBackgroundResource(0);
			mSaturdayHeaderRelativeLayout.setBackgroundResource(0);
			mSaturdayDateTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
			mSaturdayTextView.setTextColor(getResources().getColor(R.color.medium_dark_gray));
		}
	}
	
	private void updateTimeMarker() {
		if (isCurrentWeek()) {
			mCurrentTimeMarkerLinearLayout.setVisibility(android.view.View.VISIBLE);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, DateTimeHelpers.getMinutesSinceStartOfDay(), 0, 0);
			mCurrentTimeMarkerLinearLayout.setLayoutParams(lp);
		}
		else {
			mCurrentTimeMarkerLinearLayout.setVisibility(android.view.View.INVISIBLE);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void updateDayMarker() {
		if (mDayMarkerView.getParent() != null) {
			((RelativeLayout) mDayMarkerView.getParent()).removeView(mDayMarkerView);
		}
		
		switch(mSelectedDate.getDay() + 1) {
		case Calendar.SUNDAY:
			mSundayHeaderRelativeLayout.addView(mDayMarkerView);
			break;
		case Calendar.MONDAY:
			mMondayHeaderRelativeLayout.addView(mDayMarkerView);
			break;
		case Calendar.TUESDAY:
			mTuesdayHeaderRelativeLayout.addView(mDayMarkerView);
			break;
		case Calendar.WEDNESDAY:
			mWednesdayHeaderRelativeLayout.addView(mDayMarkerView);
			break;
		case Calendar.THURSDAY:
			mThursdayHeaderRelativeLayout.addView(mDayMarkerView);
			break;
		case Calendar.FRIDAY:
			mFridayHeaderRelativeLayout.addView(mDayMarkerView);
			break;
		case Calendar.SATURDAY:
			mSaturdayHeaderRelativeLayout.addView(mDayMarkerView);
			break;
		}
	}
	
	private void updateEverything() {
		updateDateTexts();
		updateDateMarker();
		updateTimeMarker();
		updateDayMarker();
		restartEventsUpdater();
	}
	
	@SuppressWarnings("deprecation")
	public void setSelectedDate(Date selectedDate) {
		mSelectedDate = selectedDate;
		
		Date sundayOfSelectedDate = DateTimeHelpers.getDateOfLastSunday(mSelectedDate);
		
		if (sundayOfSelectedDate.getDate() != mShownSundayDate.getDate()) {
			mShownSundayDate = sundayOfSelectedDate;
			
			updateEverything();
		}
		else {
			updateDayMarker();
		}
		
	}
	
	// Removes all views (events) from every day's layout, except the calendar zebras (hour markers)
	private void clearEvents() {
		mSundayRelativeLayout.removeViews(1, mSundayRelativeLayout.getChildCount() - 1);
		mMondayRelativeLayout.removeViews(1, mMondayRelativeLayout.getChildCount() - 1);
		mTuesdayRelativeLayout.removeViews(1, mTuesdayRelativeLayout.getChildCount() - 1);
		mWednesdayRelativeLayout.removeViews(1, mWednesdayRelativeLayout.getChildCount() - 1);
		mThursdayRelativeLayout.removeViews(1, mThursdayRelativeLayout.getChildCount() - 1);
		mFridayRelativeLayout.removeViews(1, mFridayRelativeLayout.getChildCount() - 1);
		mSaturdayRelativeLayout.removeViews(1, mSaturdayRelativeLayout.getChildCount() - 1);
	}
	
	private void placeEvent(Calendar start, Calendar end) {
		CalendarEventView calendarEventView = new CalendarEventView(mAppState);
		
		int startTimeMinutesSinceStartOfday = DateTimeHelpers.getMinutesSinceStartOfDay(start.getTimeInMillis());
		int endTimeMinutesSinceStartOfDay = DateTimeHelpers.getMinutesSinceStartOfDay(end.getTimeInMillis());
		
		if (endTimeMinutesSinceStartOfDay == 0 && end.getTimeInMillis() > start.getTimeInMillis()) {
			endTimeMinutesSinceStartOfDay += DateTimeHelpers.DAY_IN_MINUTES;
		}
			
		int duration = endTimeMinutesSinceStartOfDay - startTimeMinutesSinceStartOfday;
		
		if (duration < 44) {
			calendarEventView.setTitleText("");
		}
		
		if (duration < 18) {
			calendarEventView.setTimeText(" ");
		}
		else {
			Time time = new Time();
			time.set(start.getTimeInMillis());
	    	String startTimeString = time.format("%I:%M %p");
	    	
	    	time.set(end.getTimeInMillis());
	    	String endTimeString = time.format("%I:%M %p");
			
			calendarEventView.setTimeText(startTimeString + " - " + endTimeString);
		}
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, duration);
		params.setMargins(0, startTimeMinutesSinceStartOfday, 0, 0);
		
		calendarEventView.setLayoutParams(params);
		
		switch (start.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			mSundayRelativeLayout.addView(calendarEventView);
			break;
		case Calendar.MONDAY:
			mMondayRelativeLayout.addView(calendarEventView);
			break;
		case Calendar.TUESDAY:
			mTuesdayRelativeLayout.addView(calendarEventView);
			break;
		case Calendar.WEDNESDAY:
			mWednesdayRelativeLayout.addView(calendarEventView);
			break;
		case Calendar.THURSDAY:
			mThursdayRelativeLayout.addView(calendarEventView);
			break;
		case Calendar.FRIDAY:
			mFridayRelativeLayout.addView(calendarEventView);
			break;
		case Calendar.SATURDAY:
			mSaturdayRelativeLayout.addView(calendarEventView);
			break;
		}
	}
	
	private void addEvent(Calendar start, Calendar end) {
		// if event starts and ends within the same day
		if (start.get(Calendar.YEAR) == end.get(Calendar.YEAR) && start.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR)) {
			placeEvent(start, end);
		}
		else {
			// first day
			Calendar newEnd = Calendar.getInstance();
			newEnd.setTimeInMillis(start.getTimeInMillis());
			newEnd.set(Calendar.HOUR, 0);
			newEnd.set(Calendar.MINUTE, 0);
			newEnd.set(Calendar.AM_PM, Calendar.AM);
			newEnd.add(Calendar.DAY_OF_YEAR, 1);
			placeEvent(start, newEnd);
			
			// middle	
			Calendar middleStart = Calendar.getInstance();
			middleStart.setTimeInMillis(start.getTimeInMillis());
			middleStart.set(Calendar.HOUR, 0);
			middleStart.set(Calendar.MINUTE, 0);
			middleStart.set(Calendar.AM_PM, Calendar.AM);
			
			Calendar middleEnd = Calendar.getInstance();
			middleEnd.setTimeInMillis(middleStart.getTimeInMillis());
			middleEnd.set(Calendar.HOUR, 0);
			middleEnd.set(Calendar.MINUTE, 0);
			middleEnd.set(Calendar.AM_PM, Calendar.AM);
			middleEnd.add(Calendar.DAY_OF_YEAR, 1);
			
			long differenceInDays = (end.getTimeInMillis() - start.getTimeInMillis()) / DateTimeHelpers.DAY_IN_MILLISECONDS;
			
			for (int i = 1; i < differenceInDays; i++) {
				middleStart.add(Calendar.DAY_OF_YEAR, 1);
				middleEnd.add(Calendar.DAY_OF_YEAR, 1);
				
				placeEvent(middleStart, middleEnd);
			}
			
			// last day
			Calendar newStart = Calendar.getInstance();
			newStart.setTimeInMillis(end.getTimeInMillis());
			newStart.set(Calendar.HOUR, 0);
			newStart.set(Calendar.MINUTE, 0);
			newStart.set(Calendar.AM_PM, Calendar.AM);
			placeEvent(newStart, end);
		}
	}
	
	private void addEvents(FreeBusyResponse busyTimes) {
		clearEvents();
		
		if (busyTimes != null) {
			for (Map.Entry<String, FreeBusyCalendar> busyCalendar : busyTimes.getCalendars().entrySet()) {
				for (TimePeriod busy : busyCalendar.getValue().getBusy()) {
					Calendar cStart = Calendar.getInstance();
					cStart.setTimeInMillis(busy.getStart().getValue());
					
					Calendar cEnd = Calendar.getInstance();
					cEnd.setTimeInMillis(busy.getEnd().getValue());
					
					addEvent(cStart, cEnd);
				}
			}
		}
	}
	
	private void refreshEvents() {
		if (mAppState.getCalendar() != null) {
			if (mWCERT != null) {
				mWCERT.cancel(true);
				mWCERT = null;
			}
			
			mWCERT = new WeeklyCalendarEventRetrieverTask();
			
			Calendar cal = Calendar.getInstance();
			
			cal.setTime(mShownSundayDate);
			
			String startDate = DateTimeHelpers.yemodaToString(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			
			cal.add(Calendar.DAY_OF_MONTH, 6);
			
			String endDate = DateTimeHelpers.yemodaToString(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			
			if (mRefreshingCount == 0) {
				mRefreshLinearLayout.setVisibility(View.VISIBLE);
				mRefreshLinearLayout.startAnimation(mFadeIn);
				mRefreshView.setVisibility(View.VISIBLE);
				mRefreshView.startAnimation(mFadeIn);
			}
			mRefreshingCount++;
			mWCERT.execute(mAppState.getCurrentRoom().getFullName(), startDate, endDate);
		}
	}
	
	private class WeeklyCalendarEventRetrieverTask extends AsyncTask<String, Void, FreeBusyResponse> {
		// attempts to retrieve events for a given room's calendar
    	// Arguments:
    	// [0] - room number
    	// [1] - start date
    	// [2] - end date
		@Override
		protected FreeBusyResponse doInBackground(String... params) {
			//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mAppState);
			
			Room room = mAppState.getNumberAddressedRooms().get(params[0]);
			
			String roomCalendarAddress = room.getResourceAddress();
			String startingDate = params[1];
			String endingDate = params[2];
			
			FreeBusyResponse busyTimes = null;
			
			try {
				FreeBusyRequest request = new FreeBusyRequest();
				
				request.setTimeMin(new DateTime(DateTimeHelpers.dateTimeToDateObj(startingDate, "00:00 AM"), TimeZone.getTimeZone("UTC")));
				request.setTimeMax(new DateTime(DateTimeHelpers.dateTimeToDateObj(endingDate, "11:59 PM"), TimeZone.getTimeZone("UTC")));
				
				request.setItems(Arrays.asList(new FreeBusyRequestItem().setId(roomCalendarAddress)));
				
				busyTimes = mAppState.getCalendar().freebusy().query(request).execute();
			} catch (Exception e) {
				// TODO Handle errors
			}
			
			return busyTimes;
		}
		
		@Override
		protected void onPostExecute(FreeBusyResponse result) {
			addEvents(result);
			mRefreshingCount--;
			
			if (mRefreshingCount <= 0) {
				mRefreshLinearLayout.setVisibility(View.INVISIBLE);
				mRefreshLinearLayout.startAnimation(mFadeOut);
				mRefreshView.setVisibility(View.INVISIBLE);
				mRefreshView.startAnimation(mFadeOut);
			}
		}
	}
}
