package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import com.eightblocksaway.android.practicepronunciation.data.DataUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FetchDefinition extends FetchCommand<String>{

    private static final String LOG_TAG = "FetchPronunciation";

    private FetchDefinition(Uri uri, String phrase) {
        super(uri, phrase);
    }

    public static FetchDefinition create(String phrase){
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(phrase)
                .appendPath("definitions")
                .appendQueryParameter("useCanonical", "false")
                .appendQueryParameter("limit", "5")
                .appendQueryParameter("api_key", "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5")
                .build();
        return new FetchDefinition(builtUri, phrase);
    }

    @Override
    protected String parseResult(String json) {
        StringBuilder sb = new StringBuilder();

        try{
            JSONArray root = new JSONArray(json);
            for (int i = 0; i < root.length(); i++) {
                JSONObject syllable = (JSONObject) root.get(i);
                sb.append(syllable.getString("text"));

                if(i != root.length() - 1){
                    sb.append(DataUtil.DEFINITION_SEPARATOR);
                }
            }

            String result = sb.toString();
            Log.i(LOG_TAG, "Returning definitions " + result);
            return result;
        }catch (JSONException e){
            Log.e(LOG_TAG, "Couldn't parse response: " + json);
            return "";
        }
    }
}
