package com.addsensor.CameraMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CameraMap extends FragmentActivity implements OnMapReadyCallback {

	LocationManager locationManager;
	Drawable marker0, marker1, marker2, marker3, marker4;
	Bundle d;
    GoogleMap map;
	
	private static final String TAG = "CameraMapAct";
	static final private int STREET = Menu.FIRST;
	static final private int SAT = Menu.FIRST + 1;	
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
	public boolean onCreateOptionsMenu (Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		
		// Creamos y anadimos los nuevos menus
		MenuItem mQuit1 = menu.add (0, QUIT1, Menu.NONE, R.string.mQuit);
		MenuItem mUpload = menu.add (0, UPLOAD, Menu.NONE, R.string.mUpload);
		MenuItem mStreet = menu.add (0, STREET, Menu.NONE, R.string.mStreet);
		MenuItem mSat = menu.add (0, SAT, Menu.NONE, R.string.mSat);

		
		// Anadimos shortcuts
		mQuit1.setShortcut('7', 'q');
		mUpload.setShortcut('8', 'u');
		mStreet.setShortcut('6', 'w');
		mSat.setShortcut('9', 'y');

		return true;
	}
	
	// Funcion que ejecuta cada una de las opciones que ofrece el menu dependiendo del item seleccionado
	public boolean onOptionsItemSelected (MenuItem iMenu) {
		
		super.onOptionsItemSelected(iMenu);
		
		// Segun la opcion seleccionada pues haremos una funcion u otra.
		switch(iMenu.getItemId()) {
		case(STREET):
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    		return true;
		case(SAT):
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			return true;
		case(UPLOAD):
			if (d == null ){
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
		case(QUIT1):
			finish();
			return true;
		}
		return false;	
	}
	
	// Función que devuelve un objeto overlay que será el marcador que se mostrará en el mapa, a partir de un entero.
	public MarkerOptions selectMarkers(int which) {
		
		switch (which) {
		case 0: return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1)));
		case 1: return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2)));
		case 2: return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3)));
		case 3: return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker4)));
		default: return (new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.androidmarker)));
		}
	}
	
	// Función que establece un localizador para el dispositivo, es decir, que segun nos vayamos
	// moviendo la posición del dispositivo cambiar y esta será actualizada en el mapa.
	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location.getLatitude(), location.getLongitude());
		}
		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null, null);
		}
		public void onProviderEnabled(String provider) { }
		public void onStatusChanged(String provider, int status, Bundle extras){ }
	};
	
	// Función que dandole una latitud y longuitud actualiza la localización.
	// También se encarga de mostrar el marcador que hayamos elegido en el formulario. Si no hubiesemos introducido ninguna
	// nueva localizaci�n mostrar� el marcador por defecto con nuestra actual posicion.
	private void updateWithNewLocation(Double geoLat, Double geoLng) {
			
		MarkerOptions hello;
		
		// Segun los datos que le hayamos pasado desde el formulario mostramos un marcador u otro.
		if ( !(d.isEmpty()) ) {
			hello = this.selectMarkers(d.getInt("tipo"));
		} else {
			hello = this.selectMarkers(4);
		}
		map.addMarker(hello.position(new LatLng(geoLng, geoLat)));

	}
	
	// Función obtiene la latitud y longuitud de un string pasado como parametro.
	public void getLocationByAddress (String address) {
		
		Geocoder gc = new Geocoder(this, Locale.getDefault());

		List<Address> locations = null;
		try{
			locations = gc.getFromLocationName (address, 5);
			Address x = locations.get(0);
			updateWithNewLocation(x.getLatitude(), x.getLongitude());
		}catch (IOException e){
			Log.d("location:","ERROR NO LOCATIONS");
		}	
	}
	
	// Funci�n que establece un criterio para el proveedor del servicio de localizacion, normalmente el gps.
	public Criteria setCriteria () {
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}
	
	// Funcion que establece el mejor proveedor posible a partir de un criterio 
	// y que actualiza la posicion de ese proveedor
	public void setProvider() {
		
		String provider = locationManager.getBestProvider(setCriteria(), true);
		Log.d ( CameraMap.TAG, "Provider: " + provider );
		Location location = locationManager.getLastKnownLocation(provider);
		Log.d ( CameraMap.TAG, "Location: " + location );
		if (location != null) {
			
			updateWithNewLocation(location.getLatitude(), location.getLongitude());	
		}
		locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
	}
	
	// Funcion que inicializa los parametros necesarios en el momento de la creaci�n de la activity 
	// y que llama a las funciones necesarias para un correcto funcionamiento.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cmap);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mv);
        mapFragment.getMapAsync(this);

		d = new Bundle();
		//d = null;

		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		setProvider();
	}

    //
    //Funcion que recoge el resultado del formulario y que actualiza la activity dependiendo
    //los parametros que le hayamos pasado
    //
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		d = data.getExtras();
		
		// Si no hemos devuelto una direccion (address) nos mostrar� nuestra posici�n actual,
		// en caso contrario, actualizaremos el mapa con la direcci�n.
		if (resultCode == 0) {	
			d = data.getExtras();
			getLocationByAddress(d.getString("add").toString());
		} else {
			d = null;
			setProvider();
		}
	}

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
    }
}
