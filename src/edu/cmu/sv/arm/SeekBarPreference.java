package edu.cmu.sv.arm;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private Context mContext;
	
	private int mMinimumValue;
	private int mMaximumValue;
	private int mDefaultValue;
	private int mActualValue;
	
	private int mStepSize;
	
	private String mUnits;
	
	private SeekBar mSeekBar;
	private TextView mMinimumTextView;
	private TextView mMaximumTextView;
	private TextView mCurrentTextView;

	// Constructor
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		String android = "http://schemas.android.com/apk/res/android";
		String educmusvarm = "http://schemas.android.com/apk/res/edu.cmu.sv.arm"; 
		
		mMinimumValue = attrs.getAttributeIntValue(educmusvarm, "minimumValue", 0);
		mMaximumValue = attrs.getAttributeIntValue(educmusvarm, "maximumValue", 100);
		mDefaultValue = attrs.getAttributeIntValue(android, "defaultValue", mMinimumValue);
		
		mStepSize = attrs.getAttributeIntValue(educmusvarm, "stepSize", 1);
		
		mUnits = attrs.getAttributeValue(educmusvarm, "units");
		
		if (mUnits == null) {
			mUnits = mContext.getString(R.string.units);
		}
	}
	
	@Override
	public View onCreateDialogView() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View dialogView = inflater.inflate(R.layout.seek_bar_preference_dialog, null);
		
		mSeekBar = (SeekBar) dialogView.findViewById(R.id.seekBar);
		mMinimumTextView = (TextView) dialogView.findViewById(R.id.minimumTextView);
		mMaximumTextView = (TextView) dialogView.findViewById(R.id.maximumTextView);
		mCurrentTextView = (TextView) dialogView.findViewById(R.id.currentTextView);
		
		mSeekBar.setOnSeekBarChangeListener(this);
		
		mSeekBar.setMax(getAdjustedMaximumValue());
		
		if (shouldPersist()) {
			mActualValue = getPersistedInt(mDefaultValue);
		}
		
		mSeekBar.setProgress(getAdjustedValue());
		
		mMinimumTextView.setText(Integer.toString(mMinimumValue));
		mMaximumTextView.setText(Integer.toString(mMaximumValue));

		setCurrentText();
		
		return dialogView;
	}
	
	@Override 
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(getAdjustedMaximumValue());
		mSeekBar.setProgress(getAdjustedValue());
	}
	
	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		
		if (restore) {
			mActualValue = shouldPersist() ? getPersistedInt(mDefaultValue) : 0;
		}
		else { 
			mActualValue = (Integer) defaultValue;
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mActualValue = getUnadjustedValue();
		
		setCurrentText();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		int progress = seekBar.getProgress();
		int rounded = (progress + mStepSize / 2) / mStepSize * mStepSize;
		
		if (rounded > seekBar.getMax()) {
			rounded = seekBar.getMax();
		}
		
		if (rounded != progress) {
			seekBar.setProgress(rounded);
		}
		
		if (shouldPersist()) {
			persistInt(mActualValue);			
		}
		
		callChangeListener(new Integer(mActualValue));
	}
	
	// Returns the adjusted maximum value
	private int getAdjustedMaximumValue() {
		return mMaximumValue - mMinimumValue;
	}
	
	// Returns the adjusted current value
	private int getAdjustedValue() {
		return mActualValue - mMinimumValue;
	}
	
	// Returns the unadjusted current value
	private int getUnadjustedValue() {
		return mSeekBar.getProgress() + mMinimumValue;
	}
	
	// Sets the current value text
	private void setCurrentText() {
		mCurrentTextView.setText(Integer.toString(mActualValue) + " " + mUnits);
	}
}
