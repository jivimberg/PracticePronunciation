package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class FetchCommand<T> {
    private static final String LOG_TAG = "FetchCommand";
    protected static final String BASE_URI = "http://api.wordnik.com:80/v4/word.json/";
    private final Uri uri;
    private final String phrase;

    public FetchCommand(Uri uri, String phrase){
        this.uri = uri;
        this.phrase = phrase;
    }

    public T fetchData(){
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        String normalizedPhrase = phrase.toLowerCase();
        Log.i(LOG_TAG, "Executing search for normalizedPhrase " + normalizedPhrase);

        try{
            URL url = new URL(uri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            return parseResult(buffer.toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Couldn't get pronunciation", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }

    protected abstract T parseResult(String json);
}
