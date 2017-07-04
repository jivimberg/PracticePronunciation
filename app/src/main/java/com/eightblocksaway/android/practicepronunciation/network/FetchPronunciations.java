package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import com.eightblocksaway.android.practicepronunciation.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class FetchPronunciations extends FetchCommand<String>{

    private static final String LOG_TAG = "FetchPronunciations";
    private static final String BASE_URI = "https://1od-api.oxforddictionaries.com/api/v1/entries/en/";

    private FetchPronunciations(Uri uri, String phrase, Map<String, String> headers) {
        super(uri, phrase, headers);
    }

    public static FetchPronunciations create(String phrase){
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", " application/json");
        headers.put("app_id", BuildConfig.OXFORD_APP_ID);
        headers.put("app_key", BuildConfig.OXFORD_APP_KEY);

        String normalizedPhrase = phrase.toLowerCase();
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(normalizedPhrase)
                .appendPath("pronunciations")
                .build();
        return new FetchPronunciations(builtUri, phrase, headers);
    }

    @Override
    protected String parseResult(String json) throws JSONException, EmptyResponseException {
        JSONObject root = new JSONObject(json);
        if(root.length() <= 0){
            throw new EmptyResponseException();
        }

        String result = root.getJSONArray("results")
                .getJSONObject(0)
                .getJSONArray("lexicalEntries")
                .getJSONObject(0)
                .getJSONArray("pronunciations")
                .getJSONObject(0)
                .getString("phoneticSpelling");

        Log.i(LOG_TAG, "Returning IPA pronunciation " + result);
        return result;
    }
}
