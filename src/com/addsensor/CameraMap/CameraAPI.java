package com.addsensor.CameraMap;


import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public final class CameraAPI {
    private static final String TAG = "CameraAPI";
    private String user;
    private String pass;
    private Boolean status;

    private static CameraAPI api = null;
    public static CameraAPI getInstance() {
        if ( api == null){
            api = new CameraAPI();
        }
        return api;
    }

    public void setPass(String password){
        this.pass = password;
    }

    public String getPass(){ return this.pass; }

    public void setUser(String username){
        this.user = username;
    }

    public String getUser(){
        return this.user;
    }

    public boolean getStatusLogin() { return this.status; }

    public void setStatusLogin(Boolean statusLogin) { this.status = statusLogin; }

    public boolean checkLogin() {

        //{"login_status": {"errno": 0, "errstr": "OK"}}
        String res = null;
        try {
            res = postLogin( getUser(), getPass() );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if ( res != null) {
            try {
                JSONObject lStatus = new JSONObject( res );
                String username  = lStatus.getString("username");
                Log.d(CameraAPI.TAG, "Stop progress bar");
                if ( this.getUser().equals(username) ) {
                    return true;
                }

            } catch ( JSONException e ) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected String postLogin(final String login, final String pass) throws IOException {

        String userPassword = login + ":" + pass;
        String encoding = new String(Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT));

        // chicos, este resource me viene como int, no veo como hacerlo string
        //HttpPost httppost = new HttpPost( URI.create( R.string.auth_url ) );
        URL url = null;
        try {
            url = new URL("http://cameramap.escalared.com/wp-json/users/me");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            urlConnection.setDoOutput(false);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Authorization", "Basic " + encoding);
            urlConnection.connect();

            // Execute HTTP Post Request
            Log.d( CameraAPI.TAG, "request:" + url.toString() );
            Log.d( CameraAPI.TAG, "request_method:" + urlConnection.getRequestMethod() );
            Log.d( CameraAPI.TAG, "response_status:" + urlConnection.getResponseCode() );
            Log.d( CameraAPI.TAG, "response_message:" + urlConnection.getResponseMessage() );

            if ( urlConnection.getResponseCode() == 200 ){
                InputStream inputStream = urlConnection.getInputStream();
                String result = convertStreamToString( inputStream );
                Log.d(CameraAPI.TAG, "response:" + result);
                return result;
            }
        } catch (IOException e) {
            Log.v( CameraAPI.TAG, "IO:" + e.getMessage() );
            return e.getMessage();
        } finally {
            urlConnection.disconnect();
        }
        return null;
    }

    protected String postUpload( String data ) {

        String userPassword = this.getUser() + ":" + this.getPass();
        String encoding = new String(Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT));

        // chicos, este resource me viene como int, no veo como hacerlo string
        //HttpPost httppost = new HttpPost( URI.create( R.string.auth_url ) );
        URL url = null;
        try {
            url = new URL("http://cameramap.escalared.com/wp-json/posts");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Authorization", "Basic " + encoding);
            urlConnection.connect();

            // Execute HTTP Post Request
            Log.d(CameraAPI.TAG, "request:" + url.toString());
            Log.d(CameraAPI.TAG, "request_method:" + urlConnection.getRequestMethod());
            Log.d(CameraAPI.TAG, "response_status:" + urlConnection.getResponseCode());
            Log.d(CameraAPI.TAG, "response_message:" + urlConnection.getResponseMessage());

            outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            outputStream.write(data.getBytes());
            outputStream.flush();

            if (urlConnection.getResponseCode() == 201) {
                inputStream = urlConnection.getInputStream();
                String result = convertStreamToString(inputStream);
                Log.d(CameraAPI.TAG, "response:" + result);
                return result;
            }
        } catch (IOException e) {
            Log.v(CameraAPI.TAG, "IO:" + e.getMessage());
            return e.getMessage();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }
        return null;
    }

    protected String postImage( String data ) {

        String userPassword = this.getUser() + ":" + this.getPass();
        String encoding = new String(Base64.encodeToString(userPassword.getBytes(), Base64.DEFAULT));

        // chicos, este resource me viene como int, no veo como hacerlo string
        //HttpPost httppost = new HttpPost( URI.create( R.string.auth_url ) );
        URL url = null;
        try {
            url = new URL("http://cameramap.escalared.com/wp-json/media");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Authorization", "Basic " + encoding);
            urlConnection.connect();

            // Execute HTTP Post Request
            Log.d(CameraAPI.TAG, "request:" + url.toString());
            Log.d(CameraAPI.TAG, "request_method:" + urlConnection.getRequestMethod());
            Log.d(CameraAPI.TAG, "response_status:" + urlConnection.getResponseCode());
            Log.d(CameraAPI.TAG, "response_message:" + urlConnection.getResponseMessage());

            outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            outputStream.write(data.getBytes());
            outputStream.flush();

            if (urlConnection.getResponseCode() == 201) {
                inputStream = urlConnection.getInputStream();
                String result = convertStreamToString(inputStream);
                Log.d(CameraAPI.TAG, "response:" + result);
                return result;
            }
        } catch (IOException e) {
            Log.v(CameraAPI.TAG, "IO:" + e.getMessage());
            return e.getMessage();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }
        return null;
    }

    protected String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
        StringBuilder stringBuilder = new StringBuilder();
        String line;
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
