//http://www.rogcg.com/blog/2013/11/01/gridview-with-auto-resized-images-on-android
package edu.cmu.sv.arm;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoomInfoDetails extends ImageView {

	public RoomInfoDetails(Context context) {
		super(context);
	}
	
	public RoomInfoDetails(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public RoomInfoDetails(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }
}
