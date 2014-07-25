package edu.cmu.sv.arm;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

//http://www.ankitsrivastava.net/2012/03/a-simple-2d-plot-class-for-android/
public class SensorsGraphicsDialog extends DialogFragment {
	private ArrayList<SensorReading> mReadings;
  
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sensor values");
        
        Bundle args = getArguments();
        this.mReadings = new Gson().fromJson(args.getString("Readings"), new TypeToken<ArrayList<SensorReading>>() {}.getType());
        if(this.mReadings !=null){
	        float[] xvalues = new float[mReadings.size()];
	        float[] yvalues = new float[mReadings.size()];
	        for (int i=0;i<mReadings.size();i++){
	        	xvalues[i] = i+1;
	        	yvalues[i] = Integer.parseInt(mReadings.get(i).getValue());
	        }
	        builder.setView(new ChartView(getActivity(), xvalues, yvalues, 1));
        }
        return builder.create();
    }

}

class ChartView extends View {
	private Paint paint;
	private float[] xvalues;
	private float[] yvalues;
	private float maxx,maxy,minx,miny,locxAxis,locyAxis;
	private int vectorLength;
	private int axes = 1;
	
	public ChartView(Context context, float[] xvalues, float[] yvalues, int axes) {
		super(context);
		this.xvalues=xvalues;
		this.yvalues=yvalues;
		this.axes=axes;
		vectorLength = xvalues.length;
		paint = new Paint();

		getAxes(xvalues, yvalues);
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		float canvasHeight = getHeight();
		float canvasWidth = getWidth();
		int[] xvaluesInPixels = toPixel(canvasWidth, minx, maxx, xvalues); 
		int[] yvaluesInPixels = toPixel(canvasHeight, miny, maxy, yvalues);
		int locxAxisInPixels = toPixelInt(canvasHeight, miny, maxy, locxAxis);
		int locyAxisInPixels = toPixelInt(canvasWidth, minx, maxx, locyAxis);

		paint.setStrokeWidth(2);
		canvas.drawARGB(255, 255, 255, 255);
		for (int i = 0; i < vectorLength-1; i++) {
			paint.setColor(Color.RED);
			canvas.drawLine(xvaluesInPixels[i],canvasHeight-yvaluesInPixels[i],xvaluesInPixels[i+1],canvasHeight-yvaluesInPixels[i+1],paint);
		}
		
		paint.setColor(Color.BLACK);
		canvas.drawLine(0,canvasHeight-locxAxisInPixels,canvasWidth,canvasHeight-locxAxisInPixels,paint);
		canvas.drawLine(locyAxisInPixels,0,locyAxisInPixels,canvasHeight,paint);
		
		//Automatic axes markings, modify n to control the number of axes labels
		if (axes!=0){
			float temp = 0.0f;
			int n=3;
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(20.0f);
			for (int i=1;i<=n;i++){
				temp = Math.round(10*(minx+(i-1)*(maxx-minx)/n))/10;
				canvas.drawText(""+temp, (float)toPixelInt(canvasWidth, minx, maxx, temp),canvasHeight-locxAxisInPixels+20, paint);
				temp = Math.round(10*(miny+(i-1)*(maxy-miny)/n))/10;
				canvas.drawText(""+temp, locyAxisInPixels+20,canvasHeight-(float)toPixelInt(canvasHeight, miny, maxy, temp), paint);
			}
			canvas.drawText(""+maxx, (float)toPixelInt(canvasWidth, minx, maxx, maxx),canvasHeight-locxAxisInPixels+20, paint);
			canvas.drawText(""+maxy, locyAxisInPixels+20,canvasHeight-(float)toPixelInt(canvasHeight, miny, maxy, maxy), paint);
		}
		
		
	}
	
	private int[] toPixel(float pixels, float min, float max, float[] value) {
		
		double[] p = new double[value.length];
		int[] pint = new int[value.length];
		
		for (int i = 0; i < value.length; i++) {
			p[i] = .1*pixels+((value[i]-min)/(max-min))*.8*pixels;
			pint[i] = (int)p[i];
		}
		
		return (pint);
	}
	
	private void getAxes(float[] xvalues, float[] yvalues) {
		
		minx=getMin(xvalues);
		miny=getMin(yvalues);
		maxx=getMax(xvalues);
		maxy=getMax(yvalues);
		
		if (minx>=0)
			locyAxis=minx;
		else if (minx<0 && maxx>=0)
			locyAxis=0;
		else
			locyAxis=maxx;
		
		if (miny>=0)
			locxAxis=miny;
		else if (miny<0 && maxy>=0)
			locxAxis=0;
		else
			locxAxis=maxy;
		
	}
	
	private int toPixelInt(float pixels, float min, float max, float value) {
		
		double p;
		int pint;
		p = .1*pixels+((value-min)/(max-min))*.8*pixels;
		pint = (int)p;
		return (pint);
	}

	private float getMax(float[] v) {
		float largest = v[0];
		for (int i = 0; i < v.length; i++)
			if (v[i] > largest)
				largest = v[i];
		return largest;
	}

	private float getMin(float[] v) {
		float smallest = v[0];
		for (int i = 0; i < v.length; i++)
			if (v[i] < smallest)
				smallest = v[i];
		return smallest;
	}
}