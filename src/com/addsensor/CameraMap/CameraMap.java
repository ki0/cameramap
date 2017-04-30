package com.addsensor.CameraMap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CameraMap extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	HttpResultCredentials http;
	Bundle d;
	GoogleMap map;
	private Location mCurrentLocation;
	private LocationRequest mLocationRequest;
	private GoogleApiClient mGoogleApiClient;
	private static final String TAG = "CameraMapAct";
	static final private int UPLOAD = Menu.FIRST + 2;
	static final private int QUIT1 = Menu.FIRST + 3;
	static final private int GET_CODE = 0;

	// Establece que presionando una tecla hagamos zoom sobre el mapa
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
			case KeyEvent.KEYCODE_3:

				break;
			case KeyEvent.KEYCODE_1:

				break;
		}
		return super.onKeyDown(keyCode, event);
	}

	// Funcion que inicializa los parametros del menu cuando creamos la activity del mapa
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		// Creamos y anadimos los nuevos menus
		MenuItem mQuit1 = menu.add(0, QUIT1, Menu.NONE, R.string.mQuit);
		MenuItem mUpload = menu.add(0, UPLOAD, Menu.NONE, R.string.mUpload);

		// Anadimos shortcuts
		mQuit1.setShortcut('7', 'q');
		mUpload.setShortcut('8', 'u');

		return true;
	}

	// Funcion que ejecuta cada una de las opciones que ofrece el menu dependiendo del item seleccionado
	public boolean onOptionsItemSelected(MenuItem iMenu) {

		super.onOptionsItemSelected(iMenu);

		// Segun la opcion seleccionada pues haremos una funcion u otra.
		switch (iMenu.getItemId()) {
			case UPLOAD:
				if (d == null) {
					iMenu.setIntent(new Intent(this, Form.class));
					startActivityForResult(iMenu.getIntent(), GET_CODE);
					return true;
				} else {
					Intent upload = new Intent(this, Form.class);
					upload.putExtras(d);
					iMenu.setIntent(upload);
					startActivityForResult(iMenu.getIntent(), GET_CODE);
					return true;
				}
			case (QUIT1):
				finish();
				return true;
		}
		return false;
	}

	// Función que devuelve un objeto overlay que será el marcador que se mostrará en el mapa, a partir de un entero.
	private MarkerOptions selectMarkers(int which) {

		switch (which) {
			case 0:
				return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1)));
			case 1:
				return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2)));
			case 2:
				return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3)));
			case 3:
				return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker4)));
			default:
				return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.androidmarker)));
		}
	}

	// Función obtiene la latitud y longuitud de un string pasado como parametro.
	public Location getLocationByAddress(String address) {

		Geocoder gc = new Geocoder(this, Locale.getDefault());
		List<Address> addresses;
		Location locationAddress = new Location("");
		try {
			addresses = gc.getFromLocationName(address, 5);
			Address x = addresses.get(0);
			locationAddress.setLatitude(x.getLatitude());
			locationAddress.setLongitude(x.getLongitude());
		} catch (IOException e) {
			Log.d(CameraMap.TAG, "ERROR NO LOCATIONS");
		}
		return locationAddress;
	}

	// Funcion que inicializa los parametros necesarios en el momento de la creaci�n de la activity 
	// y que llama a las funciones necesarias para un correcto funcionamiento.
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.cmap);
		d = new Bundle();
		d = null;
		mCurrentLocation = null;
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mv);
		mapFragment.getMapAsync(this);
		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(10 * 1000)        // 10 seconds, in milliseconds
				.setFastestInterval(1 * 1000); // 1 second, in milliseconds
		buildGoogleApiClient();
	}

	//
	//Funcion que recoge el resultado del formulario y que actualiza la activity dependiendo
	//los parametros que le hayamos pasado
	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		// Si no hemos devuelto una direccion (address) nos mostrar� nuestra posici�n actual,
		// en caso contrario, actualizaremos el mapa con la direcci�n.
		if ( data != null ){
			d = data.getExtras();
			Log.d(CameraMap.TAG, "Bundle Login: " + d);
			Log.d(CameraMap.TAG, "Result Code: " + requestCode);
			if (resultCode == 0) {
				d = data.getExtras();
				Log.d(CameraMap.TAG, "New Location: " + d.get("add").toString());
				mCurrentLocation = getLocationByAddress(d.getString("add").toString());
				if (mCurrentLocation == null) {
					Toast.makeText(CameraMap.this, "*** ERROR: Address doesn't exit ***", Toast.LENGTH_SHORT).show();
					return;
				}
				handleNewLocation(mCurrentLocation);
			} else {
				d = null;
			}
		}
	}

	/**
	 * Creating google api client object
	 */
	protected synchronized void buildGoogleApiClient() {
		Log.d(CameraMap.TAG, "Created Google API Client");
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.addApi(AppIndex.API)
				.build();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		Log.d(CameraMap.TAG, "Google Map is READY");
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		map = googleMap;
		map.setMyLocationEnabled(true);
	}

	@Override
	public void onConnected(Bundle d) {
		Log.d(CameraMap.TAG, "ONCONNECTED");
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		if ( mCurrentLocation == null ){
			mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
			Log.d(CameraMap.TAG, "Location: " + mCurrentLocation);
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
			LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
			addMarkerToMap(latLng, 4);
			getAroundCameras(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
		}
	}

	private void handleNewLocation(Location newLocation) {
		Log.d(CameraMap.TAG, newLocation.toString());
		LatLng latLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
		getAroundCameras(newLocation.getLatitude(), newLocation.getLongitude());
		// Segun los datos que le hayamos pasado desde el formulario mostramos un marcador u otro.
		addMarkerToMap(latLng, d.getInt("tipo"));
	}

	private void addMarkerToMap(LatLng latLng, Integer selector){
		Log.d(CameraMap.TAG, "Adding ONE MARKER to Map");
		MarkerOptions camera;
		camera = this.selectMarkers(selector);
		map.clear();
		map.addMarker(camera.position(latLng));
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20.0f));
	}

	private void addMarkersToMap(LatLng latLng, Integer selector){
		Log.d(CameraMap.TAG, "Adding MARKERS to Map");
		MarkerOptions camera;
		camera = this.selectMarkers(selector);
		map.addMarker(camera.position(latLng));
	}

	private void getAroundCameras(Double lat, Double lng){
		http = new HttpResultCredentials(CameraMap.this);
		http.showDialogSpin = false;
		http.execute(CameraAPI.getInstance().getUser(), CameraAPI.getInstance().getPass(), "list", lat, lng);

		// Este thread espera hasta que el login ok, entonces guardamos en la bd.
		new Thread() {
			public void run() {
				while (!http.getStatus().equals(AsyncTask.Status.FINISHED)){}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mUpdateResultsInUI();
					}
				});
			}
		}.start();
	}

	private void mUpdateResultsInUI() {
		try {
			JSONObject jsonObject = http.getJsonResult();
			if (jsonObject == null) {
				Log.d(CameraMap.TAG, "JSON es NULL");
				return;
			}
			JSONArray jsonArray = jsonObject.getJSONArray("posts");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject explrObject = jsonArray.getJSONObject(i);
				Log.d(CameraMap.TAG, "Post: " + explrObject.toString());
				Log.d(CameraMap.TAG, "Latitude: " + explrObject.getString("lat").toString() + " Longuitude: " + explrObject.getString("lng").toString());
				LatLng latLng = new LatLng(Double.parseDouble(explrObject.getString("lat")), Double.parseDouble(explrObject.getString("lng")));
				String post_content = explrObject.getString("post_content");
				Pattern pattern = Pattern.compile(".*Type of camera: (.*?)\n");
				Matcher matcher = pattern.matcher(post_content);
				while (matcher.find()) {
					Log.d(CameraMap.TAG, "Matcher: " + matcher.group(1));
				}
				addMarkersToMap(latLng, 1);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
		
	@Override
	public void onConnectionSuspended(int i) {
		Log.d(CameraMap.TAG, "ONCONNECTIONSUSPENDED");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.i(CameraMap.TAG, "Connection failed: ConnectionResult.getErrorCode() = "
				+ connectionResult.getErrorCode());
	}

	@Override
	protected void onResume() {
		Log.d(CameraMap.TAG, "ONRESUME");
		super.onResume();
		//setUpMap();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(CameraMap.TAG, "ONPAUSE");
		if (mGoogleApiClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onLocationChanged(Location newLocation) {
		Log.d(CameraMap.TAG, "LOCATION CHANGED");
		mCurrentLocation = newLocation;
		handleNewLocation(mCurrentLocation);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(CameraMap.TAG, "ONSTART");
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		mGoogleApiClient.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"CameraMap Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.addsensor.CameraMap/http/host/path")
		);
		AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(CameraMap.TAG, "ONSTOP");

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"CameraMap Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.addsensor.CameraMap/http/host/path")
		);
		AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
		mGoogleApiClient.disconnect();
	}
}
