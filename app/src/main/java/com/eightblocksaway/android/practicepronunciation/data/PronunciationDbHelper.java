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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.eightblocksaway.android.practicepronunciation.data.PronunciationContract.AttemptEntry;
import static com.eightblocksaway.android.practicepronunciation.data.PronunciationContract.PhraseEntry;

/**
 * Manages a local database for weather data.
 */
public class PronunciationDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "pronunciation.db";

    public PronunciationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PHRASE_TABLE = "CREATE TABLE " + PhraseEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                PhraseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                PhraseEntry.COLUMN_TEXT + " TEXT NOT NULL, " +
                PhraseEntry.COLUMN_PRONUNCIATION + " TEXT, " +
                PhraseEntry.COLUMN_MASTERY_LEVEL + " INTEGER NOT NULL, " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + PhraseEntry.COLUMN_TEXT + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_PHRASE_TABLE);

        final String SQL_CREATE_ATTEMPT_TABLE = "CREATE TABLE " + AttemptEntry.TABLE_NAME + " (" +
                AttemptEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AttemptEntry.COLUMN_PHRASE_KEY + " INTEGER NOT NULL, " +
                AttemptEntry.COLUMN_RESULT_ID + " TEXT NOT NULL, " +
                AttemptEntry.COLUMN_DATE + " INTEGER NOT NULL," +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + AttemptEntry.COLUMN_PHRASE_KEY + ") REFERENCES " +
                PhraseEntry.TABLE_NAME + " (" + PhraseEntry._ID + ")" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_ATTEMPT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //do nothing
    }
}
