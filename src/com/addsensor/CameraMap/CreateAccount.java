package com.addsensor.CameraMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateAccount extends Activity {
	
	private static final String TAG = "CreateAccountActivity";
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.caccount);
		
		// Inicializacion de botones
		final EditText name = (EditText) findViewById( R.id.name );
		final EditText surname = (EditText) findViewById( R.id.surname );
		final EditText login = (EditText) findViewById( R.id.uname );
		final EditText pass = (EditText) findViewById( R.id.pass );
		final EditText rpass = (EditText) findViewById( R.id.rpass );
		final EditText mail = (EditText) findViewById( R.id.email );
		Button bCreateAccount = (Button)findViewById(R.id.caccountButton);
		
		bCreateAccount.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		final AsyncTask<String, String, Void> http = new HttpConnCredentials();
        		
        		if ( (!login.getText().toString().equals("")) & (!pass.toString().equals("")) & (!mail.toString().equals("")) ) {
        			if ( !pass.getText().toString().equalsIgnoreCase(rpass.getText().toString()) ) {
            			Toast.makeText( CreateAccount.this, "Password Error!!", Toast.LENGTH_SHORT ).show();
            			return;
            		}
            		
            		http.execute( login.getText().toString(), pass.getText().toString(), rpass.getText().toString(),mail.getText().toString(), name.getText().toString(), surname.getText().toString() );
        		} else {
        			Toast.makeText( CreateAccount.this, "Fields empty!!", Toast.LENGTH_SHORT ).show();
        		}
        	}
        });
	}
	
	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	 	// TODO Auto-generated method stub
	 	super.onActivityResult(requestCode, resultCode, data);
 	}
	
	// Subclase encargada de realizar la conexi—n http y de gestionar el json
	public class HttpConnCredentials extends AsyncTask<String, String, Void> {
		public ProgressDialog progress;
		public boolean ok = false;
		
		public boolean getAnswer() {
			return ok;
		}
		
		@Override
		protected Void doInBackground(String... params) {
			// Por debajo del ProgressBar hacemos el logeo
			Log.d ( CreateAccount.TAG, "login: " + params[0] + "// pass: " + params[1] + "// mail: " + params[2]);
			if ( checkLogin (params[0], params[1], params[2], params[3], params[4], params[5]) ) this.ok = true;
			else this.ok = false;
			Log.d ( CreateAccount.TAG, "TODO OK  " + this.ok );
			return null;
		}
		
		protected void onPreExecute() {	
			// Antes de lo que queremos hacer lanzamos el ProgressBar
			progress = new ProgressDialog(CreateAccount.this);
			progress.setTitle("Create Account in process");
			progress.setMessage("Please wait...");
			progress.setIndeterminate(true);
			progress.setCancelable(false);
			progress.show();
		}
		
		protected void onPostExecute(Void Result) {
			// Una vez hecho lo que quer’amos quitamos el ProgressBar
			if ( progress.isShowing() ) progress.dismiss();
			
			if (this.ok) {
				
				Toast.makeText( CreateAccount.this, "Create Account success!!", Toast.LENGTH_SHORT ).show();
				Intent startCameraMap = new Intent (CreateAccount.this, Login.class);
				startActivity(startCameraMap);
				finish();
				
			} else Toast.makeText( CreateAccount.this, "Create Account ko, lechon", Toast.LENGTH_SHORT ).show();
			return;
		}
		
		public boolean checkLogin(String login, String pass, String rpass, String mail, String name, String surname) {
			//{"login_status": {"errno": 0, "errstr": "OK"}}	
			String res = postLogin( login, pass, rpass, mail, name, surname );
			
			try {
				Log.d( CreateAccount.TAG, "request:" + res );
				JSONObject lStatus = new JSONObject( res );
				int errno  = lStatus.getJSONObject( "registration_status" ).getInt( "errno" );
				String errorStr = lStatus.getJSONObject( "registration_status" ).getString( "errstr" );
				Log.d( CreateAccount.TAG, "Stop progress bar" );	
				if ( errno == 0 && errorStr.equalsIgnoreCase("OK") ) {
					return true;
				}			
				
			} catch ( JSONException e ) {
				e.printStackTrace();	
			}	
			return false;
		}
		
		private String postLogin( String login, String pass, String rpass, String mail, String name, String surname ) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			
			// chicos, este resource me viene como int, no veo como hacerlo string
			// HttpPost httppost = new HttpPost( URI.create( R.string.auth_url ) );
			String uri = "http://cameramap.addsensor.com:8000/accounts/android_signup/";
			HttpPost httppost = new HttpPost( URI.create(uri) );

			try {
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
				nameValuePairs.add( new BasicNameValuePair("username", login) );
				nameValuePairs.add( new BasicNameValuePair("password1", pass) );
				nameValuePairs.add( new BasicNameValuePair("password2", rpass) );
				nameValuePairs.add( new BasicNameValuePair("email", mail) );
				nameValuePairs.add( new BasicNameValuePair("first_name", name) );
				nameValuePairs.add( new BasicNameValuePair("last_name", surname) );
				httppost.setEntity( new UrlEncodedFormEntity(nameValuePairs) );
				
				// Execute HTTP Post Request
				Log.d( CreateAccount.TAG, "request:" + uri );
				HttpResponse response = httpclient.execute( httppost );
				HttpEntity entity = response.getEntity();
				if( entity != null ){
					InputStream inputStream = entity.getContent();
					String result = convertStreamToString( inputStream );
					Log.d( CreateAccount.TAG, "statusLine:" + response.getStatusLine() );
					Log.d( CreateAccount.TAG, "response:" + result.toString() );
					return new String ( result.toString() );
				}

			} catch ( ClientProtocolException e ) {
				Log.v( CreateAccount.TAG, "proto:" + e.getMessage() );
				return new String( e.getMessage().toString() );
			} catch ( IOException e ) {
				Log.v( CreateAccount.TAG, "IO:" + e.getMessage() );
				return new String( e.getMessage().toString() );
			}
			return "Ok";
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
