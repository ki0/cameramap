package com.addsensor.CameraMap;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class Camera extends Activity{
	
	static final private int QUIT1 = Menu.FIRST;
	static final private int ABOUT = Menu.FIRST + 1;
	
	@Override
	public boolean onCreateOptionsMenu (Menu menu){
		super.onCreateOptionsMenu(menu);
		
		// Creamos y a�adimos los nuevos menus
		MenuItem mQuit1 = menu.add (0, QUIT1, Menu.NONE, R.string.mQuit);
		MenuItem mAbout = menu.add (0, ABOUT, Menu.NONE, R.string.mAbout);
		
		// A�adimos shortcuts
		mQuit1.setShortcut('7', 'q');
		mAbout.setShortcut('9', 'a');
		
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem iMenu) {
		super.onOptionsItemSelected(iMenu);
		
		// Segun la opcion seleccionada
		switch(iMenu.getItemId()) {
		case(QUIT1):
			finish();
			return true;
		case(ABOUT):
			finish();
			return true;
		}
		return false;		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Creamos el boton
        // Le a�adimos la funcion a ejecutar cuando se clickea
        Button login = (Button)this.findViewById(R.id.loginB);
        login.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View arg0){
        		
        		// Creamos la accion que ocurrira cuando se clickee el boton         		
                Intent startLogin = new Intent (Camera.this, Login.class);
         		startActivity(startLogin);
         		finish();
        	}
        });
        
        Button create = (Button)this.findViewById(R.id.createB);
        create.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View arg0){
        		
        		// Creamos y lanzamos la Activity para crear la cuenta de usuario
        		Intent startcAccount = new Intent(Camera.this, CreateAccount.class);
        		startActivity(startcAccount);
        		finish();
        	}
        });
    }
}