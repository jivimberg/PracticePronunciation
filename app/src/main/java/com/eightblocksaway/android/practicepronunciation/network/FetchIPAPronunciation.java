package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import com.eightblocksaway.android.practicepronunciation.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static com.eightblocksaway.android.practicepronunciation.R.id.result;

class FetchIPAPronunciation extends FetchCommand<String>{

    private static final String LOG_TAG = "FetchIPAPronunciation";
    private static final String BASE_URI = "https://od-api.oxforddictionaries.com/api/v1/entries/en/";

    private FetchIPAPronunciation(Uri uri, String phrase, Map<String, String> headers) {
        super(uri, phrase, headers);
    }

    public static FetchIPAPronunciation create(String phrase){
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", " application/json");
        headers.put("app_id", BuildConfig.OXFORD_APP_ID);
        headers.put("app_key", BuildConfig.OXFORD_APP_KEY);

        String normalizedPhrase = phrase.toLowerCase();
        Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                .appendPath(phrase)
                .appendPath("pronunciations")
                .build();
        return new FetchIPAPronunciation(builtUri, phrase, headers);
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
