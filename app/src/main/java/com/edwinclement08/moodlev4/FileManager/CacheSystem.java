package com.edwinclement08.moodlev4.FileManager;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CacheSystem {
    private class Storage {
        public int getValX() {
            return valX;
        }

        public void setValX(int valX) {
            this.valX = valX;
        }

        int valX;

    }

    private static CacheSystem _instance = null;
    //    private boolean isCacheAvailable = false;
    private Context context;
    private String cacheFilename = "FileCache.gson";
    private boolean DEBUG = true;
    private String TAG = "CacheSystem";
    private Storage _storage;
    private File cacheFile = null;

    private CacheSystem() {
    }

    public boolean checkCache() {

        String[] fileList = context.fileList();

        if (Arrays.asList(fileList).contains(cacheFilename)) {
            if (DEBUG) Log.i(TAG, "checkCache: Previously have a cache file");
            try {
                cacheFile = new File(context.getFilesDir(), cacheFilename);

                FileInputStream fileInputStream = new FileInputStream(cacheFile);

                int size = fileInputStream.available();
                byte[] data = new byte[size];
                int actualReadLength = fileInputStream.read(data);

                if (actualReadLength != size) {
                    Log.e(TAG, "checkCache: Reading wrong number of bytes");
                    return false;
                }

                String str = new String(data, "UTF-8"); // for UTF-8 encoding


                Gson gson = new Gson();
                _storage = gson.fromJson(str, Storage.class);


            } catch (FileNotFoundException e) {
                if (DEBUG) Log.i(TAG, "checkCache: Previously have a cache file");


            } catch (IOException e) {
                Log.e(TAG, "checkCache: IOException", e);
            }


            return true;
        } else {
            if (DEBUG) Log.i(TAG, "checkCache: no cache file");
            cacheFile = new File(context.getFilesDir(), cacheFilename);
            return false;
        }

    }


    public static CacheSystem getInstance() {
        if (_instance == null) {
            _instance = new CacheSystem();
        }
        return _instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
