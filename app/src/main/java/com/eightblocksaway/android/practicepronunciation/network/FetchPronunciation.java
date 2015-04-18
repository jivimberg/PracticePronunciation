package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FetchPronunciation extends FetchCommand<String>{

    private static final String LOG_TAG = "FetchPronunciation";

    private FetchPronunciation(Uri uri, String phrase) {
        super(uri, phrase);
    }

    public static FetchPronunciation create(String phrase){
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(phrase)
                .appendPath("pronunciations")
                .appendQueryParameter("useCanonical", "false")
                .appendQueryParameter("typeFormat", "ahd")
                .appendQueryParameter("limit", "1")
                .appendQueryParameter("api_key", "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5")
                .build();
        return new FetchPronunciation(builtUri, phrase);
    }

    @Override
    protected String parseResult(String json) {
        try{
            JSONArray root = new JSONArray(json);
            JSONObject jsonObject = (JSONObject) root.get(0);
            String result = jsonObject.getString("raw");
            Log.i(LOG_TAG, "Returning pronunciation " + result);
            return result;
        }catch (JSONException e){
            Log.e(LOG_TAG, "Couldn't parse response: " + json);
            return "";
        }
    }
}
