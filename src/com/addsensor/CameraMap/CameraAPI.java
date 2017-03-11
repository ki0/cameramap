package com.addsensor.CameraMap;


import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
                String username  = lStatus.getString("slug");
                Log.d(CameraAPI.TAG, "Stop progress bar");
                if ( this.getUser().equals(username) ) {
                    this.setStatusLogin(true);
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
        String encoding = new String(Base64.encodeToString(userPassword.getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));

        // chicos, este resource me viene como int, no veo como hacerlo string
        //HttpPost httppost = new HttpPost( URI.create( R.string.auth_url ) );
        URL url = null;
        try {
            url = new URL("http://cameramap.escalared.com/wp-json/wp/v2/users/me");
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

    protected Boolean postUpload( String data, String imagePath ) {

        String postID = null;
        String title = null;
        String userPassword = this.getUser() + ":" + this.getPass();
        String encoding = new String(Base64.encodeToString(userPassword.getBytes(), Base64.URL_SAFE|Base64.NO_WRAP));

        // chicos, este resource me viene como int, no veo como hacerlo string
        //HttpPost httppost = new HttpPost( URI.create( R.string.auth_url ) );
        URL url = null;
        try {
            url = new URL("http://cameramap.escalared.com/wp-json/wp/v2/posts");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream outputStream;
        InputStream inputStream;

        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);
        try {
            urlConnection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Authorization", "Basic " + encoding);
        try {
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Execute HTTP Post Request
        Log.d(CameraAPI.TAG, "data:" + data);
        Log.d(CameraAPI.TAG, "user:" + this.getUser());
        Log.d(CameraAPI.TAG, "password:" + this.getPass());
        Log.d(CameraAPI.TAG, "request:" + url.toString());

        try {
            outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            outputStream.write(data.getBytes());
            outputStream.close();

            if (urlConnection.getResponseCode() == 201) {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String result = convertStreamToString(inputStream);
                Log.d(CameraAPI.TAG, "response:" + result);
                inputStream.close();
                try {
                    JSONObject lStatus = new JSONObject( result );
                    postID  = lStatus.getString("id");
                    JSONObject getSth = lStatus.getJSONObject("title");
                    title = (String) getSth.get("rendered");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if ( postMedia( encoding, postID, title, imagePath) ){
                    return true;
                }
            }
        } catch (IOException e) {
            Log.v(CameraAPI.TAG, "IO:" + e.getMessage());
            return Boolean.valueOf(e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return false;
    }

    private boolean postMedia( String encoding, String id, String title, String imagePath) {
        HttpURLConnection urlConnection = null;

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";
        int bytesRead = 0, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;

        if ((id == null) || (title == null) || (imagePath == null)){
            return false;
        }

        Log.d(CameraAPI.TAG, "PostID:" + id.toString());
        Log.d(CameraAPI.TAG, "Title:" + title.toString());
        Log.d(CameraAPI.TAG, "Image Path:" + imagePath.toString());

        File sourceFile = new File(imagePath);
        if (! sourceFile.isFile()){
            Log.d(CameraAPI.TAG, "Source File NOT exists:" + imagePath);
            return false;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream( sourceFile );

            URL url = new URL("http://cameramap.escalared.com/wp-json/wp/v2/media");

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Authorization", "Basic " + encoding);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", this.getMimeType(imagePath));
            urlConnection.setRequestProperty("Content-Disposition", "attachment;filename=\"" + sourceFile.getName() + "\";post=" + id + ";title=\"" + title + "\"" + lineEnd);

            DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            Log.d(CameraAPI.TAG, "Bytes Available:" + bytesAvailable);
            Log.d(CameraAPI.TAG, "Buffer Size:" + bufferSize);

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            Log.d(CameraAPI.TAG, "Bytes to read:" + bytesRead);

            while ( bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes(lineEnd);

            // Execute HTTP Post Request
            Log.d(CameraAPI.TAG, "ResposeCode:" + urlConnection.getResponseCode());
            Log.d(CameraAPI.TAG, "Content-Length:" + urlConnection.getContentLength());
            Log.d(CameraAPI.TAG, "Content-Type:" + urlConnection.getContentType());
            Log.d(CameraAPI.TAG, "ResponseMessage:" + urlConnection.getResponseMessage());

            fileInputStream.close();
            dos.flush();
            dos.close();

            if ( urlConnection.getResponseCode() == 201 ) {
                InputStream bis = new BufferedInputStream(urlConnection.getInputStream());
                String result = convertStreamToString(bis);
                Log.d(CameraAPI.TAG, "DataInputStream:" + result);
                bis.close();
                return true;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.valueOf(e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return false;
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

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
