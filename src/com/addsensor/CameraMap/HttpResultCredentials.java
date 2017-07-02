package com.addsensor.CameraMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;

/**
 * Created by frodriguez on 21/02/2016.
 */
public class HttpResultCredentials extends AsyncTask<Object, Object, JSONArray> {
    private int errorSwitch;
    private static final String TAG = "HttpResultCredentials";
    private ProgressDialog progress;
    private Context context;
    private Boolean HttpStatus = false;
    private JSONArray jsonResult = null;
    protected Activity activity;
    public Boolean showDialogSpin = true;

    public Boolean getHttpResult(){ return this.HttpStatus; }
    private void setHttpResult(Boolean status){ this.HttpStatus = status; }
    public JSONArray getJsonResult(){ return this.jsonResult; }
    private void setJsonResult(JSONArray result){ this.jsonResult = result; }

    public HttpResultCredentials(Activity activity){
        this.activity = activity;
        context = activity;
        progress = new ProgressDialog(context);
    }

    @Override
    protected JSONArray doInBackground(Object... params) {
        JSONArray result = null;

        switch (params[2].toString()){
            case "login":
                errorSwitch = 1;
                Log.d(HttpResultCredentials.TAG, "login: " + params[0] + "// pass: " + params[1] + "// process: " + params[2]);
                CameraAPI.getInstance().setUser(params[0].toString());
                CameraAPI.getInstance().setPass(params[1].toString());
                if (CameraAPI.getInstance().checkLogin() ) {
                    this.setHttpResult(true);
                    Log.d(HttpResultCredentials.TAG, "TODO OK  " + CameraAPI.getInstance().getStatusLogin());
                } else this.setHttpResult(false);
                break;
            case "upload":
                errorSwitch = 2;
                if ( CameraAPI.getInstance().postUpload(params[3].toString(), params[4].toString()) ) {
                   this.setHttpResult(true);
                } else this.setHttpResult(false);
                break;
            case "list":
                errorSwitch = 3;
                result = CameraAPI.getInstance().getList(params[3].toString(), params[4].toString());
                this.setJsonResult(result);
                this.setHttpResult(true);
                return  result;
            default:
        }
        return result;
    }

    protected void onPreExecute() {
        if ( showDialogSpin ) {
            //Launch progreessBar before do what we want
            progress.setTitle("In process");
            progress.setMessage("Please wait...");
            progress.setIndeterminate(true);
            progress.setCancelable(false);
            progress.show();
        }
    }

    protected void onPostExecute(JSONArray jObj) {
        // Remove progressBar from screen and handle errors
        if (progress.isShowing()) progress.dismiss();

        if ( this.getHttpResult() ) {
            this.setHttpResult(CameraAPI.getInstance().getStatusLogin());
        } else {
            switch (errorSwitch){
                case 1:
                    Toast.makeText(context, "ERROR: User or password are incorrects!!", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(context, "ERROR: Uploading new camera!!", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(context, "ERROR: Getting cameras!!", Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }
    }
}
