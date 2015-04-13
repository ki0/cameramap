package com.addsensor.CameraMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.client.ClientProtocolException;
import java.net.HttpURLConnection;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

//import android.R.string;


public class Login extends Activity { 
	
	private static final String TAG = "LoginActivity";

	@Override
	protected void onCreate( Bundle savedInstanceState ) {	
		super.onCreate( savedInstanceState );
		setContentView( R.layout.login );
		
		final EditText login = (EditText) findViewById( R.id.login );
		final EditText pass = (EditText) findViewById( R.id.pass );
		final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
		final Button bLogin = (Button) findViewById( R.id.loginButton );
		final CameraAdapterDB db = new CameraAdapterDB( this );
		
		login.setOnKeyListener( new OnKeyListener() {
			public boolean onKey( View v, int keyCode, KeyEvent event ) {
				Log.d ( Login.TAG, "Evento capturado" );
				if ( event.getAction() == KeyEvent.ACTION_DOWN ) {
				//if ( pass.isFocused() ) {
					//pass.requestFocus();
					Log.d ( Login.TAG, "Estamos en PASS" );
					// Tenemos algo escrito en el edittext de login, enter y rellenamos pass
					switch ( keyCode ){
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							db.open();
							if ( login.length() > 0 ) {
								Log.d ( Login.TAG, "Login mayor que cero" );
								Cursor c = db.getPass( login.getText().toString() ); 
								// Cojemos el cursor (fila en este caso), y si hay algo...
								if ( c.getCount() != 0 ) {
									 int clogin = c.getColumnIndex("login");
									 int cpass = c.getColumnIndex("pass");
									 if ( c.getString(clogin).equals(login.getText().toString()) ) {
										 Log.d ( Login.TAG, "Rellenamos PASS" );
										 pass.setText( (CharSequence) c.getString(cpass) );
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
		
		bLogin.setOnClickListener( new View.OnClickListener() {
			final AsyncTask<String, String, Void> http = new HttpConnCredentials();
			
			public void onClick( View arg0 ) {
				http.execute( login.getText().toString(), pass.getText().toString() );
				final Handler mHandler = new Handler();
				final Runnable mUpdateResults = new Runnable() {
			        public void run() {
			            updateResultsInUi();
			        }

			    };
				// Este thread espera hasta que el login ok, entonces guardamos en la bd.
				Thread t = new Thread() {
		            public void run() {
		            	while ( !http.getStatus().equals(AsyncTask.Status.FINISHED) ) {
		            		try {
								sleep(1000);
							} catch ( InterruptedException e ) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		            	}
		                mHandler.post(mUpdateResults);
		            }
		        };
		        t.start();
			}
			// Cuando tengamos el resultado del login miramos si lo introducimos en la BD
			private void updateResultsInUi() {
				// TODO Auto-generated method stub
				db.open();
				if ( ((HttpConnCredentials) http).getAnswer() ) {
					if ( checkBox.isChecked() ) {
						Log.d ( Login.TAG, "Checkbox activado" );
						if ( !db.loginExists( login.getText().toString() ) ) {
							Log.d ( Login.TAG, "Insertamos en la BD: " + login.getText().toString() + "// pass: " + pass.getText().toString() );
							db.insert( login.getText().toString(), pass.getText().toString() );
						}
					}
				}
				db.close();
			}
		});
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		super.onActivityResult( requestCode, resultCode, data );
	}
	
	// Subclase encargada de realizar la conexion http y de gestionar el json
	public class HttpConnCredentials extends AsyncTask<String, String, Void> {
		public ProgressDialog progress;
		public boolean ok = false;
		private String user;
		private String pass;
		
		public boolean getAnswer() {
			return ok;
		}
		
		@Override
		protected Void doInBackground(String... params) {
			// Por debajo del ProgressBar hacemos el logeo
			Log.d ( Login.TAG, "login: " + params[0] + "// pass: " + params[1] );
            try {
                if ( checkLogin (params[0], params[1]) ) {
                    this.ok = true;
                    user = params[0];
                    pass = params[1];
                } else this.ok = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d ( Login.TAG, "TODO OK  " + this.ok );
			return null;
		}
		
		protected void onPreExecute() {	
			// Antes de lo que queremos hacer lanzamos el ProgressBar
			progress = new ProgressDialog(Login.this);
			progress.setTitle("Login in process");
			progress.setMessage("Please wait...");
			progress.setIndeterminate(true);
			progress.setCancelable(false);
			progress.show();
		}
		
		protected void onPostExecute(Void Result) {
			// Una vez hecho lo que queriamos quitamos el ProgressBar
			if ( progress.isShowing() ) progress.dismiss();
			
			if (this.ok) {
				
				Toast.makeText( Login.this, "login ok", Toast.LENGTH_SHORT ).show();
				Intent startCameraMap = new Intent (Login.this, CameraMap.class);
				Bundle b = new Bundle();
				b.putString("user", user);
				b.putString("pass", pass);
				startCameraMap.putExtras(b);
				startActivity(startCameraMap);
				finish();
				
			} else {
                Toast.makeText(Login.this, "login ko, lechon", Toast.LENGTH_SHORT).show();
            }
		}
		
		public boolean checkLogin(String login, String pass) throws IOException {
			//{"login_status": {"errno": 0, "errstr": "OK"}}	
			String res = postLogin( login, pass );
			
			try {
				JSONObject lStatus = new JSONObject( res );
			    String username  = lStatus.getString("username");
				Log.d( Login.TAG, "Stop progress bar" );	
				if ( login.equals(username) ) {
					return true;
				}			
				
			} catch ( JSONException e ) {
				e.printStackTrace();	
			}	
			return false;
		}
		
		private String postLogin( final String login, final String pass ) throws IOException {

            String userPassword = login + ":" + pass;
            String encoding = new String(Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT));
			
			// chicos, este resource me viene como int, no veo como hacerlo string
			//HttpPost httppost = new HttpPost( URI.create( R.string.auth_url ) );
            URL url = new URL("http://cameramap.escalared.com/wp-json/users/me");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			try {
                urlConnection.setDoOutput(false);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Authorization", "Basic " + encoding);
                urlConnection.connect();
				
				// Execute HTTP Post Request
				Log.d( Login.TAG, "request:" + url.toString() );
                Log.d( Login.TAG, "request_method:" + urlConnection.getRequestMethod() );
                Log.d( Login.TAG, "response_status:" + urlConnection.getResponseCode() );
                Log.d( Login.TAG, "response_message:" + urlConnection.getResponseMessage() );

				if ( urlConnection.getResponseCode() == 200 ){
					InputStream inputStream = urlConnection.getInputStream();
					String result = convertStreamToString( inputStream );
					Log.d( Login.TAG, "response:" + result );
					return result;
				}

			} catch ( ClientProtocolException e ) {
				Log.v( Login.TAG, "proto:" + e.getMessage() );
				return e.getMessage();
            } catch (IOException e) {
				Log.v( Login.TAG, "IO:" + e.getMessage() );
				return e.getMessage();
			} finally {
                urlConnection.disconnect();
            }
            return "KO";
		}
		private String convertStreamToString( InputStream is ) {
			BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			try {
				while ( (line = reader.readLine()) != null ) {
					stringBuilder.append(line + "\n");
				}
			} catch ( IOException e ) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return stringBuilder.toString();
		}
	}
}




