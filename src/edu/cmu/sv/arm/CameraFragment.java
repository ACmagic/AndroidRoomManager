package edu.cmu.sv.arm;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CameraFragment extends Fragment {
	private ARM mAppState;
	
	private ImageView mCameraImageView;
	private TextView mNoCamerasTextView;
	
	private Vector<String> mCameraURLs;
	private int mOnCamera;

	private Handler mUpdateHandler;
	private Runnable mUpdater;
	
	private CameraBitmapTask mCurrentGetBitmap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View cameraView = inflater.inflate(R.layout.camera, container, false);
		
		mAppState = ((ARM) getActivity().getApplication());

		mCameraImageView = (ImageView) cameraView.findViewById(R.id.cameraImageView);
		mNoCamerasTextView = (TextView) cameraView.findViewById(R.id.noCamerasTextView);
		
		mCameraURLs = new Vector<String>();
		
		updateCameraURLs();
		mOnCamera = 0;
		
		mCameraImageView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				cycleCamera();
		    }
		});

		mUpdateHandler = new Handler();

		mUpdater = new Runnable() {
			public void run() {
				if (mCurrentGetBitmap != null) {
					mCurrentGetBitmap.cancel(true);
					mCurrentGetBitmap = null;
				}
				mCurrentGetBitmap = new CameraBitmapTask();

				try {
					mCurrentGetBitmap.execute();
				} catch (Exception e) {
					// OK to do nothing here (there simply will not be an update)
				}
				
				if (mAppState.getCurrentRoom().getImages().size() > mOnCamera) {
					RoomImage ri = mAppState.getCurrentRoom().getImages().get(mOnCamera);
					mUpdateHandler.postDelayed(mUpdater, ri.getRefreshDelayMilliseconds() + ri.getRefreshDelaySeconds() * DateTimeHelpers.SECOND_IN_MILLISECONDS + ri.getRefreshDelayMinutes() * DateTimeHelpers.MINUTE_IN_MILLISECONDS);
				}
			}
		};
		
		updateCameraDisplayArea();

		return cameraView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		startUpdater();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		stopUpdater();
		
		if (mCurrentGetBitmap != null) {
			mCurrentGetBitmap.cancel(true);
			mCurrentGetBitmap = null;
		}
	}
	
	// Cycles the current camera to the next one
	public void cycleCamera() {
		int oldOnCamera = mOnCamera;
		
		if (mOnCamera + 1 < mAppState.getCurrentRoom().getImages().size()) {
			mOnCamera++;
		}
		else {
			mOnCamera = 0;
		}
		
		if (oldOnCamera != mOnCamera) {
			restartUpdater();
		}
	}

	// Starts the updater for the camera image
	public void startUpdater() {
		if (mUpdater != null) {
			mUpdater.run();
		}
	}

	// Stops the updater for the camera image
	public void stopUpdater() {
		if (mUpdateHandler != null && mUpdater != null) {
			mUpdateHandler.removeCallbacks(mUpdater);
		}
	}
	
	// Restarts the updater for the camera image
	public void restartUpdater() {
		stopUpdater();
		startUpdater();
	}
	
	// Used for resetting the camera fragment
	public void reset(boolean resetCamera) {
		updateCameraURLs();
		
		if (resetCamera) {
			mOnCamera = 0;
		}
		
		updateCameraDisplayArea();
	}
	
	// Used for updating the camera display area (if there are no cameras, will display a message that states so and stop the camera updater; otherwise, hide the message and start the updater)
	private void updateCameraDisplayArea() {
		if (mAppState.getCurrentRoom().getImages().size() == 0) {
			stopUpdater();
			
			mCameraImageView.setVisibility(android.view.View.GONE);
			mNoCamerasTextView.setVisibility(android.view.View.VISIBLE);
		}
		else {
			restartUpdater();
			
			mNoCamerasTextView.setVisibility(android.view.View.GONE);
		}
	}

	// Gets the URLs for static "live" camera images of a given room (String room must be in the format "Room [#]")
	private void updateCameraURLs() {
		mCameraURLs.clear();
		
		Room room = mAppState.getCurrentRoom();

		if (room == null) {
			return;
		}

		/*String http_room_camera = "http://room";
		http_room_camera += room.split(" ")[1];
		http_room_camera += "-camera";

		String http_room_camera_url_image = "";

		int cam_count = 0;

		do {
			http_room_camera_url_image = http_room_camera;
			http_room_camera_url_image += Integer.toString(cam_count + 1);
			http_room_camera_url_image += ".sv.cmu.edu/oneshotimage.jpg";

			UrlCheckerTask checkURL = new UrlCheckerTask();

			checkURL.execute(http_room_camera_url_image);

			Boolean checker = false;

			try {
				checker = checkURL.get();
			} catch (Exception e) {

			}

			if (checker) {
				mCameraURLs.add(http_room_camera_url_image);
			}

			cam_count++;
		} while (cam_count < PreferenceManager.getDefaultSharedPreferences(mAppState).getInt("maximumNumberOfCamerasPerRoom", 2));*/
		
		
	}

	private class CameraBitmapTask extends AsyncTask<Void, Void, Bitmap> {
		// Retrieves a bitmap from the current camera URL
		@Override
		protected Bitmap doInBackground(Void... noargs) {
			try {
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection con = (HttpURLConnection) new URL(mAppState.getCurrentRoom().getImages().get(mOnCamera).getURL()).openConnection();
				InputStream is = con.getInputStream();

				BufferedInputStream bis = new BufferedInputStream(is);

				Bitmap cameraBitmap = BitmapFactory.decodeStream(bis);

				bis.close();
				is.close();

				return cameraBitmap;
			} catch (Exception e) {
				return null;
			}
		}

		// Sets the bitmap into the camera image view
		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				mCameraImageView.setImageBitmap(result);
				
				if (mAppState.getCurrentRoom().getImages().size() > 0) {
					mCameraImageView.setVisibility(android.view.View.VISIBLE);
				}
			}
		}
	}

	/*private class UrlCheckerTask extends AsyncTask<String, Void, Boolean> {
		// Tests whether accessing a particular URL will return an HTTP_OK response (status 200)
		@Override
		protected Boolean doInBackground(String... urls) {
			try {
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection con = (HttpURLConnection) new URL(urls[0])
						.openConnection();
				con.setRequestMethod("HEAD");
				con.setConnectTimeout(333); // if can't get URL within 0.333
											// seconds, just ignore the camera
				return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
			} catch (Exception e) {
				return false;
			}
		}
	}*/
}
