/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eightblocksaway.android.practicepronunciation.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.model.PronunciationRecognitionResult;

import static com.eightblocksaway.android.practicepronunciation.data.PronunciationContract.AttemptEntry;
import static com.eightblocksaway.android.practicepronunciation.data.PronunciationContract.CONTENT_AUTHORITY;
import static com.eightblocksaway.android.practicepronunciation.data.PronunciationContract.PATH_ATTEMPT;
import static com.eightblocksaway.android.practicepronunciation.data.PronunciationContract.PATH_PHRASE;
import static com.eightblocksaway.android.practicepronunciation.data.PronunciationContract.PhraseEntry;

public class PronunciationProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String LOG_TAG = "PronunciationProvider";
    private PronunciationDbHelper dbHelper;

    static final int PHRASE = 100;
    static final int PHRASE_ID = 101;
    static final int ATTEMPT = 200;
    static final int ATTEMPTS_WITH_PHRASE = 201;

    private static final SQLiteQueryBuilder attemptsByPhraseQueryBuilder;

    static{
        attemptsByPhraseQueryBuilder = new SQLiteQueryBuilder();
        attemptsByPhraseQueryBuilder.setTables(
                AttemptEntry.TABLE_NAME + " INNER JOIN " +
                        PhraseEntry.TABLE_NAME +
                        " ON " + AttemptEntry.TABLE_NAME +
                        "." + AttemptEntry.COLUMN_PHRASE_KEY +
                        " = " + PhraseEntry.TABLE_NAME +
                        "." + PhraseEntry._ID);
    }

    public static final String phraseByTextSelector =
            PhraseEntry.TABLE_NAME+
                    "." + PhraseEntry.COLUMN_TEXT + " = ? ";

    private Cursor getAttemptsByPhrase(Uri uri, String[] projection, String sortOrder) {
        String[] selectionArgs = new String[]{AttemptEntry.getPhraseFromUri(uri)};
        String selection = phraseByTextSelector;

        return attemptsByPhraseQueryBuilder.query(dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // PronunciationContract to help define the types to the UriMatcher.
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_PHRASE, PHRASE);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_ATTEMPT, ATTEMPT);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_ATTEMPT + "/*", ATTEMPTS_WITH_PHRASE);

        // 3) Return the new matcher!
        return uriMatcher;
    }

    /*
        Students: We've coded this for you.  We just create a new PronunciationDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        dbHelper = new PronunciationDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case PHRASE:
                return PhraseEntry.CONTENT_ITEM_TYPE;
            case ATTEMPT:
                return AttemptEntry.CONTENT_ITEM_TYPE;
            case ATTEMPTS_WITH_PHRASE:
                return AttemptEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "attempts/*"
            case ATTEMPTS_WITH_PHRASE: {
                retCursor = getAttemptsByPhrase(uri, projection, sortOrder);
                break;
            }
            // "phrase"
            case PHRASE: {
                retCursor = dbHelper.getReadableDatabase().query(
                        PhraseEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "attempt"
            case ATTEMPT: {
                retCursor = dbHelper.getReadableDatabase().query(
                        AttemptEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case PHRASE: {
                long _id = db.insert(PhraseEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    Log.i(LOG_TAG, "Inserted phrase " + values);
                    returnUri = PhraseEntry.builPhraseUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ATTEMPTS_WITH_PHRASE: {
                normalizeDate(values);
                String phraseFromUri = AttemptEntry.getPhraseFromUri(uri);

                Cursor cursor = null;
                try{
                    cursor = db.query(PhraseEntry.TABLE_NAME, new String[]{PhraseEntry._ID}, phraseByTextSelector, new String[]{phraseFromUri}, null, null, null);
                    if(cursor.moveToFirst()){
                        values.put(AttemptEntry.COLUMN_PHRASE_KEY, cursor.getInt(0)); //Zero because we are only getting 1 column in our projection

                        long _id = db.insert(AttemptEntry.TABLE_NAME, null, values);
                        if ( _id > 0 ) {
                            Log.i(LOG_TAG, "Inserted attempt " + values);
                            returnUri = AttemptEntry.buildAttemptUri(_id);
                        } else
                            throw new android.database.SQLException("Failed to insert row into " + uri);
                    } else {
                        Log.i(LOG_TAG, "Couldn't find phrase " + phraseFromUri + " in the DB. Not persisting attempt");
                    }
                } finally {
                    if(cursor != null && !cursor.isClosed())
                        cursor.close();
                }

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null); //Use the parent uri, not the returnUri to notify!
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if(null ==  selection) {
            selection = "1";
        }

        switch (match) {
            case PHRASE: {
                rowsDeleted = db.delete(PhraseEntry.TABLE_NAME, selection, selectionArgs);
                Log.i(LOG_TAG, "Removed " + rowsDeleted + " rows");
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null); //Use the parent uri, not the returnUri to notify!
        return rowsDeleted;

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.

        // Student: return the actual rows deleted
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(AttemptEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(AttemptEntry.COLUMN_DATE);
            values.put(AttemptEntry.COLUMN_DATE, DataUtil.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        if(null ==  selection) {
            throw new IllegalArgumentException("selection argument was missing");
        }

        switch (match) {
            case PHRASE: {
                Cursor cursor = null;

                if(values.containsKey(PhraseEntry.COLUMN_MASTERY_LEVEL)){
                    Integer attemptResult = values.getAsInteger(PhraseEntry.COLUMN_MASTERY_LEVEL);

                    try{
                        //Calculate points
                        cursor = db.query(PhraseEntry.TABLE_NAME, new String[]{PhraseEntry.COLUMN_MASTERY_LEVEL}, selection, selectionArgs, null, null, null);
                        if(cursor.moveToFirst()){
                            int currentMasteryLevel = cursor.getInt(0);
                            int updatedResult = currentMasteryLevel + attemptResult;

                            int minScore = getContext().getResources().getInteger(R.integer.min_points);
                            if(updatedResult < minScore){
                                updatedResult = minScore;
                            }

                            int maxScore = getContext().getResources().getInteger(R.integer.max_points);
                            if(updatedResult > maxScore) {
                                updatedResult = maxScore;
                            }

                            values.put(PhraseEntry.COLUMN_MASTERY_LEVEL, updatedResult);

                            rowsUpdated = db.update(PhraseEntry.TABLE_NAME, values, selection, selectionArgs);
                            Log.i(LOG_TAG, "Updated " + rowsUpdated + " phrase. With values " + values);
                        } else {
                            Log.i(LOG_TAG, "Couldn't find phrase in the DB. Not updating");
                            rowsUpdated = 0;
                        }

                    } finally {
                        if(cursor != null && !cursor.isClosed())
                            cursor.close();
                    }
                } else {
                    Log.i(LOG_TAG, "Couldn't find value for Column Mastery Level");
                    rowsUpdated = 0;
                }


                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null); //Use the parent uri, not the returnUri to notify!
        return rowsUpdated;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }
}
