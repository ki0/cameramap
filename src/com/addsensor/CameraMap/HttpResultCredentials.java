package com.addsensor.CameraMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by frodriguez on 21/02/2016.
 */
public class HttpResultCredentials extends AsyncTask<Object, Object, JSONObject> {
    private static final String TAG = "HttpResultCredentials";
    private ProgressDialog progress;
    private Context context;
    private Boolean HttpStatus = false;
    private JSONObject jsonResult = null;
    protected Activity activity;
    public Boolean showDialogSpin = true;

    public Boolean getHttpResult(){ return this.HttpStatus; }
    private void setHttpResult(Boolean status){ this.HttpStatus = status; }
    public JSONObject getJsonResult(){ return this.jsonResult; }
    private void setJsonResult(JSONObject result){ this.jsonResult = result; }

    public HttpResultCredentials(Activity activity){
        this.activity = activity;
        context = activity;
        progress = new ProgressDialog(context);
    }

    @Override
    protected JSONObject doInBackground(Object... params) {
        // Por debajo del ProgressBar hacemos el logeo
        Log.d(HttpResultCredentials.TAG, "login: " + params[0] + "// pass: " + params[1] + "// process: " + params[2]);
        JSONObject result = null;
        CameraAPI.getInstance().setUser(params[0].toString());
        CameraAPI.getInstance().setPass(params[1].toString());
        switch (params[2].toString()){
            case "login":
                if (CameraAPI.getInstance().checkLogin() ) {
                    this.setHttpResult(true);
                    Log.d(HttpResultCredentials.TAG, "TODO OK  " + CameraAPI.getInstance().getStatusLogin());
                } else this.setHttpResult(false);
                break;
            case "upload":
                if ( CameraAPI.getInstance().postUpload(params[3].toString(), params[4].toString()) ) {
                   this.setHttpResult(true);
                } else this.setHttpResult(false);
                break;
            case "list":
                try {
                    result = CameraAPI.getInstance().getList(params[3].toString(), params[4].toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (result.has("posts")){
                    this.setJsonResult(result);
                    this.setHttpResult(true);
                    return  result;
                } else this.setHttpResult(false);
                break;
            default:
        }
        return result;
    }

    protected void onPreExecute() {
        if ( showDialogSpin ) {
            // Antes de lo que queremos hacer lanzamos el ProgressBar
            progress.setTitle("In process");
            progress.setMessage("Please wait...");
            progress.setIndeterminate(true);
            progress.setCancelable(false);
            progress.show();
        }
    }

    protected void onPostExecute(JSONObject jObj) {
        // Una vez hecho lo que queriamos quitamos el ProgressBar
        if (progress.isShowing()) progress.dismiss();

        if ( this.getHttpResult() ) {
            this.setHttpResult(CameraAPI.getInstance().getStatusLogin());
        } else {
            Toast.makeText(context, "ERROR: something went wrong!!", Toast.LENGTH_SHORT).show();
        }
    }


}
