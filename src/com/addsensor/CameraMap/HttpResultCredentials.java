package com.addsensor.CameraMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by frodriguez on 21/02/2016.
 */
public class HttpResultCredentials extends AsyncTask<String, String, Void> {
    private static final String TAG = "HttpResultCredentials";
    private ProgressDialog progress;
    private Context context;
    private Boolean HttpStatus = false;
    protected Activity activity;
    public final CameraAPI api = CameraAPI.getInstance();

    public Boolean getHttpResult(){ return this.HttpStatus; }
    private void setHttpResult(Boolean status){ this.HttpStatus = status; }

    public HttpResultCredentials(Activity activity){
        this.activity = activity;
        context = activity;
        progress = new ProgressDialog(context);
    }

    @Override
    protected Void doInBackground(String... params) {
        // Por debajo del ProgressBar hacemos el logeo
        Log.d(HttpResultCredentials.TAG, "login: " + params[0] + "// pass: " + params[1]);

        api.setUser(params[0]);
        api.setPass(params[1]);
        if ( api.checkLogin() ) {
            api.setStatusLogin(true);
        } else api.setStatusLogin(false);
        Log.d(HttpResultCredentials.TAG, "TODO OK  " + api.getStatusLogin());
        return null;
    }

    protected void onPreExecute() {
        // Antes de lo que queremos hacer lanzamos el ProgressBar
        progress.setTitle("In process");
        progress.setMessage("Please wait...");
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
    }

    protected void onPostExecute(Void Result) {
        // Una vez hecho lo que queriamos quitamos el ProgressBar
        if (progress.isShowing()) progress.dismiss();

        if ( api.getStatusLogin() ) {

            this.setHttpResult(api.getStatusLogin());

        } else {
            Toast.makeText(context, "login ko, lechon", Toast.LENGTH_SHORT).show();
        }
    }
}
