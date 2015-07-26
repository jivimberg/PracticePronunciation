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
        String normalizedPhrase = phrase.toLowerCase();
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(normalizedPhrase)
                .appendPath("pronunciations")
                .appendQueryParameter("useCanonical", "false")
                .appendQueryParameter("typeFormat", "ahd")
                .appendQueryParameter("limit", "1")
                .appendQueryParameter("api_key", API_KEY)
                .build();
        return new FetchPronunciation(builtUri, phrase);
    }

    @Override
    protected String parseResult(String json) throws JSONException, EmptyResponseException {
        JSONArray root = new JSONArray(json);
        if(root.length() <= 0){
            throw new EmptyResponseException();
        }

        JSONObject jsonObject = (JSONObject) root.get(0);
        String result = jsonObject.getString("raw");
        Log.i(LOG_TAG, "Returning AHD pronunciation " + result);
        return result;
    }
}
