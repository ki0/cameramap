package com.addsensor.CameraMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
//import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Form extends Activity {
	int estado, tipo, vigilancia;
	static final private int GET_CODE = 0;
	private Uri targetResource = Media.INTERNAL_CONTENT_URI;
	private Uri currImgUri;
	private String selectedImgPath;
	private Button bExplorer;
	private Button bVmap;
	private Button bCameraAR;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form);
		
		// Inicializaci—n de los botones.
		bExplorer = (Button)this.findViewById(R.id.explorar);
		bVmap = (Button)this.findViewById(R.id.vMap);
		bCameraAR = (Button)this.findViewById(R.id.camera);
		
		// Inicializaci—n de los spinners.
		Spinner sTipo = (Spinner) findViewById(R.id.tipo);
        Spinner sVigilancia = (Spinner) findViewById(R.id.aviso);
        Spinner sEstado = (Spinner) findViewById(R.id.estado);
        
        // Inicializaci—n de los cajas para introducir texto.
		final EditText eLocation = (EditText) findViewById(R.id.eLocation);
		final EditText eComen = (EditText) findViewById(R.id.eComen);
       
		// Inicializaci—n del adaptador que mostrar‡ las opciones del spinner, al ser pulsado.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource (this, R.array.cMarkers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sTipo.setAdapter(adapter);
        
        // Funci—n que establece la opci—n seleccionada en los spinners.
        sTipo.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				// Devuele la opci—n que hayamos seleccionado
				tipo = (int) arg0.getSelectedItemPosition();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
        });
        
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource (this, R.array.cVigilancia, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sVigilancia.setAdapter(adapter1);
        
        sVigilancia.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				vigilancia = (int) arg0.getSelectedItemPosition();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
        });
        
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource (this, R.array.cEstado, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sEstado.setAdapter(adapter2);
        
        sEstado.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				estado = (int) arg0.getSelectedItemPosition();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub			
			}
        });
        
        // Obtenemos los campos del bundle que se nos pasa desde el mapActivity. Esto se produce
        // para mantener los datos elegidos durante las posibles interacciones entre el mapActivity y el formulario.
        Bundle extras = getIntent().getExtras();
        
        // Establecemos los campos del Bundle en los diferentes objetos del formulario.
		if (extras != null) {
			eLocation.setText(extras.getString("add"));
			eComen.setText(extras.getString("comen"));
			sTipo.setSelection(extras.getInt("tipo"));
			sVigilancia.setSelection(extras.getInt("vigila"));
			sEstado.setSelection(extras.getInt("estado"));
		}
        
		// Funci—n que cuando se pulsa el boton, establece el resultado del formulario para pasarselo al mapActivity. En caso,
		// de que hayamos introducido una direcci—n pues estableceremos los campos del bundle para ser 
		// pasados a la mapActivity, en caso contrario, el resultado es 1, y el mapActivity mostrar‡
		// nuestra posici—n actual.
        bVmap.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		
        		if (eLocation.getText().toString().length() > 0) {
        			Intent map = new Intent(Form.this, CameraMap.class);
        			Bundle b = new Bundle();
        			String address = eLocation.getText().toString();
        			String comentario = eComen.getText().toString();
        			b.putString("add", address);
        			b.putString("comen", comentario);
        			b.putInt("tipo", tipo);
        			b.putInt("vigila", vigilancia);
        			b.putInt("estado", estado);
        			map.putExtras(b);
        			setResult (0, map);
        			finish();
        		} else {
        			Intent map = new Intent(Form.this, CameraMap.class);
        			setResult (1, map);
        			finish();
        		}
        	}
        });

        bExplorer.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		Intent aCameraGallery = new Intent(Intent.ACTION_GET_CONTENT, Form.this.targetResource);
        		aCameraGallery.setType("image/*");
        		startActivityForResult(Intent.createChooser(aCameraGallery, "Camera Map: Select Picture"), GET_CODE);	
        	}
        });
        
        // Funcion que arranca la activity de la galeria cuando pulsamos el boton correspondiente.
        bCameraAR.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		Intent aCameraAR = new Intent(Form.this, CameraAR.class);
        		startActivityForResult(aCameraAR, GET_CODE);
        	}
        });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		if ( resultCode == RESULT_OK ) {
			
			if ( requestCode == GET_CODE ) {
				System.out.println("estoy dentro");
				// Tomamos la Uri de la imagen seleccionada
				currImgUri = data.getData();
				selectedImgPath = getPath(currImgUri);
				if ( selectedImgPath != null ) {
					bExplorer.setText(selectedImgPath.subSequence(selectedImgPath.lastIndexOf("/"), selectedImgPath.length()));
				} else Toast.makeText( Form.this, "*** ERROR: NO PATH ***", Toast.LENGTH_SHORT ).show();
			}
		}
	} 
	
	public String getPath(Uri uri) {
	    String[] projection = { MediaStore.Images.Media.DATA };
	    Cursor cursor = managedQuery(uri, projection, null, null, null);
	    if ( cursor != null ) {
	    	int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
	    } else return null;
	}
}
