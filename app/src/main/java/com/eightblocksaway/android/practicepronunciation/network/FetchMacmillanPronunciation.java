package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.Map;

public class FetchMacmillanPronunciation extends FetchCommand<String>{

    private static final String LOG_TAG = "FetchPronunciation";
    protected static final String BASE_URI = "https://www.macmillandictionary.com/api/v1/dictionaries/american/search/first/";

    private FetchMacmillanPronunciation(Uri uri, String phrase, Map<String, String> headers) {
        super(uri, phrase, headers);
    }

    public static FetchMacmillanPronunciation create(String phrase){
        Map<String, String> headers = new HashMap<>();
        headers.put("accessKey", "gVebJsNpi2sjavYgtH6oyCfjUKm3mtlEtpIdNI7neVoIMGE4qsZGRUIYiypxjUFZ");

        String normalizedPhrase = phrase.toLowerCase();
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendQueryParameter("q", normalizedPhrase)
                .appendQueryParameter("format", "xml")
                .build();
        return new FetchMacmillanPronunciation(builtUri, phrase, headers);
    }

    @Override
    protected String doParseResult(String json) throws JSONException {
        //TODO check about overriding parseResult altogether because we are receiving jsonObject instead of jsonArray
        String result = json;
        Log.i(LOG_TAG, "Returning pronunciation " + result);
        return result;
    }
}
