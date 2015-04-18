package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FetchHyphenation extends FetchCommand<String>{

    private static final String LOG_TAG = "FetchPronunciation";
    public static final String STRESS_SYMBOL = "*";
    public static final String SECONDARY_STRESS_SYMBOL = "**";
    public static final String SYLLABLE_SEPARATOR = "-";

    private FetchHyphenation(Uri uri, String phrase) {
        super(uri, phrase);
    }

    public static FetchHyphenation create(String phrase){
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(phrase)
                .appendPath("hyphenation")
                .appendQueryParameter("useCanonical", "false")
                .appendQueryParameter("limit", "50")
                .appendQueryParameter("api_key", "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5")
                .build();
        return new FetchHyphenation(builtUri, phrase);
    }

    @Override
    protected String parseResult(String json) {
        StringBuilder sb = new StringBuilder();

        try{
            JSONArray root = new JSONArray(json);
            for (int i = 0; i < root.length(); i++) {
                JSONObject syllable = (JSONObject) root.get(i);
                sb.append(syllable.getString("text"));
                if(syllable.has("type")){
                    switch (syllable.getString("type")){
                        case "stress":
                            sb.append(STRESS_SYMBOL);
                            break;
                        case "secondary stress":
                            sb.append(SECONDARY_STRESS_SYMBOL);
                            break;
                        default:
                            break;
                    }
                }

                if(i != root.length() - 1){
                    sb.append(SYLLABLE_SEPARATOR);
                }
            }

            String result = sb.toString();
            Log.i(LOG_TAG, "Returning hyphenation " + result);
            return result;
        }catch (JSONException e){
            Log.e(LOG_TAG, "Couldn't parse response: " + json);
            return "";
        }
    }
}
