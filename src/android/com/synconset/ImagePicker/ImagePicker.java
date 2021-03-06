/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.content.pm.PackageManager;

public class ImagePicker extends CordovaPlugin {
    public static String TAG = "ImagePicker";

    private CallbackContext callbackContext;
    private JSONObject params;
    protected final static String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE };
    public static final int SAVE_TO_ALBUM_SEC = 1;
    public static final int PERMISSION_DENIED_ERROR = 20;
	
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
         this.callbackContext = callbackContext;
         this.params = args.getJSONObject(0);
        if(!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE))
            PermissionHelper.requestPermission(this, SAVE_TO_ALBUM_SEC, Manifest.permission.READ_EXTERNAL_STORAGE);
        else
            this.getPictures();
        return true;
    }
    public boolean getPictures()  throws JSONException {
            Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
            int max = 20;
            int desiredWidth = 0;
            int desiredHeight = 0;
            int quality = 100;
            if (this.params.has("maximumImagesCount")) {
                max = this.params.getInt("maximumImagesCount");
            }
            if (this.params.has("width")) {
                desiredWidth = this.params.getInt("width");
            }
            if (this.params.has("height")) {
                desiredHeight = this.params.getInt("height");
            }
            if (this.params.has("quality")) {
                quality = this.params.getInt("quality");
            }
            intent.putExtra("MAX_IMAGES", max);
            intent.putExtra("WIDTH", desiredWidth);
            intent.putExtra("HEIGHT", desiredHeight);
            intent.putExtra("QUALITY", quality);
            if (this.cordova != null) {
                this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
            }
        return true;
    }
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        Log.e("ImagePicker","on request Permission Result");
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        if (requestCode == SAVE_TO_ALBUM_SEC) {
            this.getPictures();
        }
    }	
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
			JSONArray res = new JSONArray(fileNames);
			this.callbackContext.success(res);
		} else if (resultCode == Activity.RESULT_CANCELED && data != null) {
			String error = data.getStringExtra("ERRORMESSAGE");
			this.callbackContext.error(error);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			JSONArray res = new JSONArray();
			this.callbackContext.success(res);
		} else {
			this.callbackContext.error("No images selected");
		}
    }
}
