/****************************** CompARs ******************************
* TODO: Description, Author, etc
*	Add camera view
*	Implement kalman-filter for sensor values:
*		http://interactive-matter.eu/blog/2009/12/18/filtering-sensor-data-with-a-kalman-filter/
*		http://de.wikipedia.org/wiki/Kalman-Filter
* 	Remove menu bar
*********************************************************************/
package de.noah.naime.compars;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class CompARsActivity extends Activity implements SensorEventListener {

	// Create Surface
	private GLSurfaceView glSurfaceView;
	private ArrowRenderer arrowRenderer = null;
	private boolean rendererSet = false;
	// Get Orientation
	private SensorManager sensorManager = null;
	private Sensor accelerationSensor = null;
	private Sensor magneticSensor = null;
	private float[] accelerationValues;
	private float[] magneticValues;
	private float[] inclinationMatrix;
	private float[] rotationMatrix;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Initialise glSurfaceView
		glSurfaceView = new GLSurfaceView(this);
		
		// Check if the system supports OpenGL ES 2.0.
		ActivityManager activityManager = 
			(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        
		ConfigurationInfo configurationInfo = activityManager
			.getDeviceConfigurationInfo();
        
		final boolean supportsEs2 =
			configurationInfo.reqGlEsVersion >= 0x20000
			|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
			&& (Build.FINGERPRINT.startsWith("generic")
			  || Build.FINGERPRINT.startsWith("unknown")
			  || Build.MODEL.contains("google_sdk")
			  || Build.MODEL.contains("Emulator")
			  || Build.MODEL.contains("Android SDK built for x86")));
			  
		// Inform the renderer about new sensor events
		arrowRenderer = new ArrowRenderer(this);

		if (supportsEs2) {
			// Request an OpenGL ES 2.0 compatible context.
			glSurfaceView.setEGLContextClientVersion(2);            
		    
			// Assign the renderer.
			glSurfaceView.setRenderer(arrowRenderer);
			
			rendererSet = true;
		}
		
		else {
		    Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
			Toast.LENGTH_LONG).show();
		    
			return;
		}
		
		// Get orientation sensors
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		inclinationMatrix = new float[16];
		rotationMatrix = new float[16];
		
		// Add glSurfaceView to activity and display on screen
		setContentView(glSurfaceView);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    
	    sensorManager.unregisterListener(this);
	    
	    if (rendererSet) {
	    	glSurfaceView.onPause();
	    }
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Register sensor listeners
		if(accelerationSensor != null){
			sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		if(magneticSensor != null){
			sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	    
		if (rendererSet) {
			glSurfaceView.onResume();
		}
	}
	
	// New sensor values are available
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		// Acceleration values
		if (event.sensor == accelerationSensor)
			accelerationValues = lowPass(event.values.clone(), accelerationValues, 0.2f);
		
		// Magnetic field values
		if (event.sensor == magneticSensor)
			magneticValues = lowPass(event.values.clone(), magneticValues, 0.8f);
		
		if (accelerationValues != null && magneticValues != null){
			
			// Calculate rotation matrix
			if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix,
					accelerationValues, magneticValues)) {
	
				// Forward orientation to renderer
				glSurfaceView.queueEvent(new Runnable() {
					
					@Override
					public void run() {
						arrowRenderer.rotateViewMatrix(rotationMatrix);
					}
				});
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	// Simple lowpass filter for sensor values
	protected float[] lowPass(float[] input, float[] output, float alpha){
		if (output == null)
			return input;
		
		for (int i = 0; i < input.length; i++)
			output[i] = output[i] + alpha * (input[i] - output[i]);
		
		return output;
	}
}
