package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import com.eightblocksaway.android.practicepronunciation.data.DataUtil;
import com.eightblocksaway.android.practicepronunciation.model.Syllable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FetchHyphenation extends FetchCommand<List<Syllable>>{

    private static final String LOG_TAG = "FetchPronunciation";

    private FetchHyphenation(Uri uri, String phrase) {
        super(uri, phrase);
    }

    public static FetchHyphenation create(String phrase){
        String normalizedPhrase = phrase.toLowerCase();
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(normalizedPhrase)
                .appendPath("hyphenation")
                .appendQueryParameter("useCanonical", "false")
                .appendQueryParameter("limit", "15")
                .appendQueryParameter("api_key", "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5")
                .build();
        return new FetchHyphenation(builtUri, phrase);
    }

    @Override
    protected List<Syllable> parseResult(String json) {
        List<Syllable> result = new ArrayList<>();

        try{
            JSONArray root = new JSONArray(json);
            for (int i = 0; i < root.length(); i++) {
                JSONObject syllable = (JSONObject) root.get(i);
                String text = syllable.getString("text");
                String typeValue = syllable.getString("type");
                Syllable.Stress type = Syllable.Stress.fromString(typeValue);
                result.add(new Syllable(text, type));
            }
            Log.i(LOG_TAG, "Returning hyphenation " + result);
            return result;
        }catch (JSONException e){
            Log.e(LOG_TAG, "Couldn't parse response: " + json);
            return result;
        }
    }
}
