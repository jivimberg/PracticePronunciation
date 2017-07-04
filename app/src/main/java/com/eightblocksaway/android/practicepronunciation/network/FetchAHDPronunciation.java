package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FetchAHDPronunciation extends FetchCommand<Map<PronunciationTypeFormat,String>>{

    private static final String LOG_TAG = "FetchAHDPronunciation";

    private FetchAHDPronunciation(Uri uri, String phrase) {
        super(uri, phrase);
    }

    public static FetchAHDPronunciation create(String phrase){
        String normalizedPhrase = phrase.toLowerCase();
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(normalizedPhrase)
                .appendPath("pronunciations")
                .appendQueryParameter("useCanonical", "false")
                .appendQueryParameter("api_key", API_KEY)
                .build();
        return new FetchAHDPronunciation(builtUri, phrase);
    }

    @Override
    protected Map<PronunciationTypeFormat, String> parseResult(String json) throws JSONException, EmptyResponseException {
        JSONArray root = new JSONArray(json);
        if(root.length() <= 0){
            throw new EmptyResponseException();
        }

        Map<PronunciationTypeFormat, String> result = new HashMap<>();
        for (int i = 0 ; i < root.length(); i++) {
            JSONObject jsonObject = (JSONObject) root.get(i);
            String type = jsonObject.getString("rawType");

            PronunciationTypeFormat format = PronunciationTypeFormat.fromTypeName(type);
            if(format != null && !result.containsKey(format)) {
                String value = jsonObject.getString("raw");
                result.put(format, value);
                Log.i(LOG_TAG, "Adding pronunciation type " + format + " with value " + result);
            }
        }
        return result;
    }
}
