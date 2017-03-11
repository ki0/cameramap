package com.addsensor.CameraMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Login extends Activity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        final EditText login = (EditText) findViewById(R.id.login);
        final EditText pass = (EditText) findViewById(R.id.pass);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        final Button bLogin = (Button) findViewById(R.id.loginButton);
        final CameraAdapterDB db = new CameraAdapterDB(this);



        login.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(Login.TAG, "Evento capturado");
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    //if ( pass.isFocused() ) {
                    //pass.requestFocus();
                    Log.d(Login.TAG, "Estamos en PASS");
                    // Tenemos algo escrito en el edittext de login, enter y rellenamos pass
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            db.open();
                            if (login.length() > 0) {
                                Log.d(Login.TAG, "Login mayor que cero");
                                Cursor c = db.getPass(login.getText().toString());
                                // Cojemos el cursor (fila en este caso), y si hay algo...
                                if (c.getCount() != 0) {
                                    int clogin = c.getColumnIndex("login");
                                    int cpass = c.getColumnIndex("pass");
                                    if (c.getString(clogin).equals(login.getText().toString())) {
                                        Log.d(Login.TAG, "Rellenamos PASS");
                                        pass.setText(c.getString(cpass));
                                        return true;
                                    }
                                }
                            }
                            db.close();
                            return false;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                final HttpResultCredentials http = new HttpResultCredentials(Login.this);
                http.execute(login.getText().toString(), pass.getText().toString(), "login");
                final Handler mHandler = new Handler();
                final Runnable mUpdateResults = new Runnable() {
                    public void run() {
                        updateResultsInUi();
                    }
                };
                // Este thread espera hasta que el login ok, entonces guardamos en la bd.
                new Thread() {
                    public void run() {
                        while (!http.getStatus().equals(AsyncTask.Status.FINISHED)) {
                            try {
                                sleep(3);
                                if (http.getHttpResult()) {

                                    Intent startCameraMap = new Intent(Login.this, CameraMap.class);
                                    startActivity(startCameraMap);
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

            // Cuando tengamos el resultado del login miramos si lo introducimos en la BD
            private void updateResultsInUi() {
                // TODO Auto-generated method stub
                db.open();

                if (checkBox.isChecked()) {
                    Log.d(Login.TAG, "Checkbox activado");
                        if (!db.loginExists(login.getText().toString())) {
                        Log.d(Login.TAG, "Insertamos en la BD: " + login.getText().toString() + "// pass: " + pass.getText().toString());
                        db.insert(login.getText().toString(), pass.getText().toString());
                    }
                }
                db.close();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}




