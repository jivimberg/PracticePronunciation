package com.eightblocksaway.android.practicepronunciation.network;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
    protected String parseResult(String json) throws JSONException, EmptyResponseException {
        JSONObject root = new JSONObject(json);
        if(root.length() <= 0){
            throw new EmptyResponseException();
        }

        try {
            String xml = root.getString("entryContent");
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expression = xpath.compile("//PRON/text()");
            InputSource is = new InputSource(new StringReader(xml));
            String result = (String) expression.evaluate(is, XPathConstants.STRING);
            Log.i(LOG_TAG, "Returning pronunciation " + result);
            return result;
        } catch (XPathExpressionException e) {
            throw new JSONException(e.getMessage());
        }
    }
}
