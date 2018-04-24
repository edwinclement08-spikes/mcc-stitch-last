package com.edwinclement08.moodlev4.FileManager;

import android.os.Environment;
import android.util.Log;


public class ExternalStorageManager {
    private static ExternalStorageManager _shared = null;
    private String TAG = "ExternalStorageManager";
    private Boolean DEBUG = false;


    private ExternalStorageManager() {
        Log.i(TAG, "ExternalStorageManager: Is external storage writable :" + isExternalStorageWritable());
        Log.i(TAG, "ExternalStorageManager: Is external storage readable :" + isExternalStorageReadable());
    }

    public static ExternalStorageManager getManager() {
        if (_shared == null) {
            _shared = new ExternalStorageManager();
        }
        return _shared;
    }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}


