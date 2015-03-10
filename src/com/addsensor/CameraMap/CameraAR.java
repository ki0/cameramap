package com.addsensor.CameraMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

public class CameraAR extends Activity {
	
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		try{
			super.onCreate( savedInstanceState );
		    CustomCameraView cv = new CustomCameraView( this.getApplicationContext() );	
		    FrameLayout rl = new FrameLayout( this.getApplicationContext() );
		    setContentView( rl );
		    rl.addView( cv );
	   } catch( Exception e ){}
	}
	
	public class CustomCameraView extends SurfaceView {
		public SensorManager sensorMan;
		public float direction;
		Camera camera;
		SurfaceHolder previewHolder;
		SurfaceHolder.Callback surfaceHolderListener = new SurfaceHolder.Callback() {
			public void surfaceCreated( SurfaceHolder holder ) {
				camera = Camera.open();

				try {
					camera.setPreviewDisplay( previewHolder );
				} catch (Throwable t){ }
			}
			public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
		      Parameters params = camera.getParameters();
		      params.setPreviewSize( width, height );
		      params.setPictureFormat( PixelFormat.JPEG );
		              camera.setParameters( params );
		              camera.startPreview();
			}

			public void surfaceDestroyed( SurfaceHolder arg0 ) {
		      camera.stopPreview();
		      camera.release();   
			}
		};
		SensorEventListener listener = new SensorEventListener() {

			public void onAccuracyChanged( Sensor sensor, int accuracy ) {
				// TODO Auto-generated method stub
				
			}

			public void onSensorChanged( SensorEvent event ) {
				// TODO Auto-generated method stub
				float vals[] = event.values;   
			    direction = vals[0];
			}
		};
		
		public CustomCameraView( Context context ) {
			super( context );
			previewHolder = this.getHolder();
	        previewHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
	        previewHolder.addCallback( surfaceHolderListener );   
			sensorMan = (SensorManager) context.getSystemService( Context.SENSOR_SERVICE) ;
			sensorMan.registerListener( listener, sensorMan.getDefaultSensor(SensorManager.SENSOR_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST );
		}
	}
}


