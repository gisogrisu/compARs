/****************************** CompARs ******************************
* TODO: Description, Author, etc
*	Comment Camera-Methods in detail
*	Implement kalman-filter for sensor values:
*		http://interactive-matter.eu/blog/2009/12/18/filtering-sensor-data-with-a-kalman-filter/
*		http://de.wikipedia.org/wiki/Kalman-Filter
* 	Remove menu bar
*********************************************************************/
package de.noah.naime.compars;

import java.io.IOException;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
	// Get Camera preview
	private boolean cameraOn;
	Camera camera;
	SurfaceView cameraPreview;
	SurfaceHolder previewHolder;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set layout view
		setContentView(R.layout.activity_compars);
		
		// Initialise glSurfaceView
		glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurface);
		
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
			
			// Get a translucent surface
			glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
			glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			glSurfaceView.setZOrderOnTop(true);
		    
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

		// Get camera preview
		cameraOn = false;
		cameraPreview = (SurfaceView)findViewById(R.id.cameraPreview);
        previewHolder = cameraPreview.getHolder();
        previewHolder.addCallback(surfaceCallback);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    
	    sensorManager.unregisterListener(this);
 	    
	    if (cameraOn)
	        camera.stopPreview();
	      
	    camera.release();
	    camera=null;
	    cameraOn=false;
	    
	    if (rendererSet) 
	    	glSurfaceView.onPause();
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		
		// Register sensor listeners
		if(accelerationSensor != null)
			sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		if(magneticSensor != null)
			sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		camera=Camera.open();
	    
		if (rendererSet)
			glSurfaceView.onResume();
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
    
    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
    	
    	public void surfaceCreated(SurfaceHolder holder) {
    		try {
    			camera.setPreviewDisplay(previewHolder);	
    		}
    		
    		catch (Throwable t) {
    			Log.e("CompARsActivity", "Exception in setPreviewDisplay()", t);
    		}
    	}
    	
    	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
    		
    		Camera.Parameters parameters=camera.getParameters();
    		
    		Camera.Size size = parameters.getPreferredPreviewSizeForVideo();
    		parameters.setPreviewSize(size.width, size.height);
    		camera.setParameters(parameters);
    		
    		camera.setDisplayOrientation(90);
    		
    		try {
				camera.setPreviewDisplay(holder);
			}
    		
    		catch (IOException e) {
				Log.e("CompARsActivity", "Exception caused by setPreviewDisplay()");
				
				e.printStackTrace();
			}

    		camera.startPreview();
    		cameraOn = true;
	}
    	
    	public void surfaceDestroyed(SurfaceHolder holder) {
    		// not used
    	}
    	
    };
}
