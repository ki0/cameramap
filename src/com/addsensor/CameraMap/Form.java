package com.addsensor.CameraMap;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

//import android.util.Log;

public class Form extends Activity {
	int estado, tipo, vigilancia;
	static final private int GET_CODE = 0;
	private Uri targetResource = Media.INTERNAL_CONTENT_URI;
	private Uri currImgUri;
	private String selectedImgPath;
	private Button bExplorer;
	private Button bUpload;
	private static final String TAG = "FormActivity";
	EditText eLocation, eComen;
	String comentario, address;

	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form);

		// Inicializacion de los botones.
		bExplorer = (Button) this.findViewById(R.id.explorar);
		bUpload = (Button) this.findViewById(R.id.upload);

		// Inicializaci�n de los spinners.
		Spinner sTipo = (Spinner) findViewById(R.id.tipo);
		Spinner sVigilancia = (Spinner) findViewById(R.id.aviso);
		Spinner sEstado = (Spinner) findViewById(R.id.estado);

		// Inicializacion de los cajas para introducir texto.
		eLocation = (EditText) findViewById(R.id.eLocation);
		eComen = (EditText) findViewById(R.id.eComen);

		// Inicializacion del adaptador que mostrara las opciones del spinner, al ser pulsado.
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cMarkers, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sTipo.setAdapter(adapter);

		// Funcion que establece la opcion seleccionada en los spinners.
		sTipo.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				// Devuele la opcion que hayamos seleccionado
				tipo = arg0.getSelectedItemPosition();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.cVigilancia, android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sVigilancia.setAdapter(adapter1);

		sVigilancia.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				vigilancia = arg0.getSelectedItemPosition();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.cEstado, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sEstado.setAdapter(adapter2);

		sEstado.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				estado = arg0.getSelectedItemPosition();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub			
			}
		});

		// Función que establece un listener en el botón para que cuando hagamos un click haga una llamada
		// a la galería de Android.
		bExplorer.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent aCameraGallery = new Intent(Intent.ACTION_GET_CONTENT, Form.this.targetResource);
				aCameraGallery.setType("image/*");
				startActivityForResult(Intent.createChooser(aCameraGallery, "Camera Map: Select Picture"), GET_CODE);
			}
		});

		//Función que hace un http post contra el servidor de CameraMap, pasandole todos los parámetros
		// del formulario.
		bUpload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				address = eLocation.getText().toString();
				comentario = eComen.getText().toString();
				if (selectedImgPath == null) {
					Toast.makeText(Form.this, "*** ERROR: NO IMAGE SELECTED ***", Toast.LENGTH_SHORT).show();
					return;
				}
				if (address.matches("")){
					Toast.makeText(Form.this, "*** ERROR: NO ADDRESS ***", Toast.LENGTH_SHORT).show();
					return;
				}
				final HttpResultCredentials http = new HttpResultCredentials(Form.this);
				http.execute(CameraAPI.getInstance().getUser(), CameraAPI.getInstance().getPass(), "upload", getFromJSON(), selectedImgPath);
				final Handler mHandler = new Handler();
				final Runnable mUpdateResults = new Runnable() {
					public void run() {
					}
				};
				// Este thread espera hasta que el login ok, entonces guardamos en la bd.
				new Thread() {
					public void run() {
						while (!http.getStatus().equals(AsyncTask.Status.FINISHED)) {
							try {
								sleep(3);
								if (http.getHttpResult()) {
									Intent map = new Intent(Form.this, CameraMap.class);
									Bundle b = new Bundle();
									address = eLocation.getText().toString();
									comentario = eComen.getText().toString();
									b.putString("add", address);
									b.putString("comen", comentario);
									b.putInt("tipo", getCategoriesID(tipo));
									b.putInt("vigila", getVigilanceID(vigilancia));
									b.putInt("estado", getStateID(estado));
									map.putExtras(b);
									setResult(0, map);
									finish();
								}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						mHandler.post(mUpdateResults);
					}
				}.start();
			}
		});
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	private String getFromJSON () {
		int categoryID = getCategoriesID(tipo);
		int stateID = getStateID(estado);
		int vigilanceID = getVigilanceID(vigilancia);
		Address addss = getAddress(address);
		JSONObject json = new JSONObject();
		try {
			json.put("title", eLocation.getText().toString());
			json.put("categories", categoryID);
			json.put("estado", stateID);
			json.put("alerta", vigilanceID);
			json.put("content", "[geo_mashup_save_location lat=\"" + addss.getLatitude() + "\"" + " lng=\"" + addss.getLongitude() + "\"" + "]" + "\n" + eComen.getText().toString() + "\n");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d(Form.TAG, "JSON: " + json.toString());
		return json.toString();
	}

	private int getCategoriesID(int which){
		switch (which){
			case 0:
				return 4;
			case 1:
				return 1;
			case 2:
				return 2;
			case 3:
				return 3;
		}
		return 0;
	}

	private int getStateID(int which){
		switch (which){
			case 0:
				return 6;
			case 1:
				return 5;
		}
		return 0;
	}

	private int getVigilanceID(int which){
		switch (which){
			case 0:
				return 7;
			case 1:
				return 8;
		}
		return 0;
	}

	private Address getAddress(String address) {
		Geocoder gc = new Geocoder(this, Locale.getDefault());
		List<Address> addresses;
		Address x = null;
		try {
			addresses = gc.getFromLocationName(address, 5);
			x = addresses.get(0);
		} catch (IOException e) {
			Log.d(Form.TAG, "ERROR NO LOCATIONS");
		}
		return x;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

			if (requestCode == GET_CODE) {
				// Tomamos la Uri de la imagen seleccionada
				currImgUri = data.getData();
				Log.d(Form.TAG, "Current Uri: " + currImgUri);
				try {
					selectedImgPath = getPath(getApplicationContext(), currImgUri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				Log.d(Form.TAG, "Image Path: " + selectedImgPath);
				if (selectedImgPath != null) {
					bExplorer.setText(selectedImgPath.subSequence(selectedImgPath.lastIndexOf("/"), selectedImgPath.length()));
				} else
					Toast.makeText(Form.this, "*** ERROR: NO PATH ***", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static String getPath(Context context, Uri uri) throws URISyntaxException {
		String selection = null;
		String[] selectionArgs = null;
		// Uri is different in versions after KITKAT (Android 4.4), we need to
		if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				return Environment.getExternalStorageDirectory() + "/" + split[1];
			} else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				uri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
			} else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				if ("image".equals(type)) {
					uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				selection = "_id=?";
				selectionArgs = new String[]{
						split[1]
				};
			}
		}
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = {
					MediaStore.Images.Media.DATA
			};
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver()
						.query(uri, projection, selection, selectionArgs, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Form Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.addsensor.CameraMap/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Form Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.addsensor.CameraMap/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}
}
