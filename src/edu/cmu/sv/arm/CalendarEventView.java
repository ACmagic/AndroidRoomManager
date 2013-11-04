package edu.cmu.sv.arm;

import edu.cmu.sv.arm.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarEventView extends LinearLayout {
	private String mTimeText;
	private String mTitleText;
	
	private TextView mTimeTextView;
	private TextView mTitleTextView;
	
	public CalendarEventView(Context context) {
		super(context);
		
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.calendar_event, this);
		
		mTimeTextView = (TextView) this.findViewById(R.id.timeTextView);
		mTitleTextView = (TextView) this.findViewById(R.id.titleTextView);
		
		mTimeText = getContext().getString(R.string.no_time);
		mTitleText = getContext().getString(R.string.busy);
		
		mTimeTextView.setText(mTimeText);
		mTitleTextView.setText(mTitleText);
	}
	
	public CalendarEventView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.calendar_event, this);
		
		mTimeTextView = (TextView) this.findViewById(R.id.timeTextView);
		mTitleTextView = (TextView) this.findViewById(R.id.titleTextView);
		
		String educmusvarm = "http://schemas.android.com/apk/res/edu.cmu.sv.arm"; 
		
		mTimeText = attrs.getAttributeValue(educmusvarm, "timeText");
		
		if (mTimeText == null) {
			mTimeText = getContext().getString(R.string.no_time);
		}
		
		mTitleText = attrs.getAttributeValue(educmusvarm, "titleText");
		
		if (mTitleText == null) {
			mTitleText = getContext().getString(R.string.busy);
		}
		
		mTimeTextView.setText(mTimeText);
		mTitleTextView.setText(mTitleText);
	}
	
	public CharSequence getTimeText() {
		return ((TextView) this.findViewById(R.id.timeTextView)).getText();
	}
	
	public void setTimeText(CharSequence time) {
		mTimeText = time.toString();
		
		mTimeTextView.setText(mTimeText);
	}
	
	public CharSequence getTitleText() {
		return ((TextView) this.findViewById(R.id.titleTextView)).getText();
	}
	
	public void setTitleText(CharSequence title) {
		mTitleText = title.toString();
		
		mTitleTextView.setText(mTitleText);
	}
}
