package com.eightblocksaway.android.practicepronunciation.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.format.Time;

import com.eightblocksaway.android.practicepronunciation.model.Definition;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.model.Stress;
import com.eightblocksaway.android.practicepronunciation.model.Syllable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.eightblocksaway.android.practicepronunciation.data.PronunciationContract.PhraseEntry;

public class DataUtil {

    public static final String SYLLABLE_SEPARATOR = "-";
    public static final String DEFINITION_SEPARATOR = "###";
    public static final String INTERNAL_DEFINITION_SEPARATOR = "::";

    public static final String STRESS_SYMBOL = "*1";
    public static final String SECONDARY_STRESS_SYMBOL = "*2";
    public static final String NONE_STRESS_SYMBOL = "*0";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.setToNow();
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static ContentValues toContentValues(@NotNull Phrase phrase) {
        ContentValues result = new ContentValues();
        result.put(PhraseEntry.COLUMN_TEXT, phrase.getPhrase());
        result.put(PhraseEntry.COLUMN_MASTERY_LEVEL, 0);
        result.put(PhraseEntry.COLUMN_PRONUNCIATION, phrase.getPronunciation());
        result.put(PhraseEntry.COLUMN_DEFINITIONS, encodeDefinitions(phrase.getDefinitions()));
        result.put(PhraseEntry.COLUMN_HYPHENATION, encodeHyphenation(phrase.getHyphenation()));
        return result;
    }

    private static String encodeDefinitions(@NotNull List<Definition> definitions) {
        StringBuilder sb = new StringBuilder();
        for (Definition definition : definitions) {
            sb.append(definition.getPartOfSpeech());
            sb.append(INTERNAL_DEFINITION_SEPARATOR);
            sb.append(definition.getDefinition());
            sb.append(DEFINITION_SEPARATOR);
        }

        //remove last separator
        sb.delete(sb.length() - DEFINITION_SEPARATOR.length(), sb.length());
        return sb.toString();
    }

    private static String encodeHyphenation(@NotNull List<Syllable> hyphenation) {
        StringBuilder sb = new StringBuilder();
        for (Syllable syllable : hyphenation) {
            sb.append(syllable.getText());
            switch (syllable.getStress()){
                case PRIMARY_STRESS:
                    sb.append(STRESS_SYMBOL);
                    break;
                case SECONDARY_STRESS:
                    sb.append(SECONDARY_STRESS_SYMBOL);
                    break;
                case NONE:
                    sb.append(NONE_STRESS_SYMBOL);
                    break;
            }
            sb.append(SYLLABLE_SEPARATOR);
        }

        //remove last separator
        sb.delete(sb.length() - SYLLABLE_SEPARATOR.length(), sb.length());

        return sb.toString();
    }

    public static Phrase fromCursor(@NotNull Cursor cursor) {
        String phrase = cursor.getString(cursor.getColumnIndex(PhraseEntry.COLUMN_TEXT));
        String pronunciation = cursor.getString(cursor.getColumnIndex(PhraseEntry.COLUMN_PRONUNCIATION));
        String definitionsString = cursor.getString(cursor.getColumnIndex(PhraseEntry.COLUMN_DEFINITIONS));
        List<Definition> definitions = decodeDefinitions(definitionsString);
        String hyphenationString = cursor.getString(cursor.getColumnIndex(PhraseEntry.COLUMN_HYPHENATION));
        List<Syllable> hyphenation = decodeHyphenation(hyphenationString);
        int points = cursor.getInt(cursor.getColumnIndex(PhraseEntry.COLUMN_MASTERY_LEVEL));
        return Phrase.createPersisted(phrase, pronunciation, definitions, hyphenation, points);
    }

    private static List<Definition> decodeDefinitions(@NotNull String definitionsString) {
        List<Definition> result = new ArrayList<>();

        //TODO optimize with compiled patterns?
        List<String> definitions = Arrays.asList(definitionsString.split(DEFINITION_SEPARATOR));
        for (String definitionString : definitions) {
            String[] definitionArray = definitionString.split(INTERNAL_DEFINITION_SEPARATOR);
            result.add(new Definition(definitionArray[1], definitionArray[0]));
        }

        return result;
    }

    private static List<Syllable> decodeHyphenation(@NotNull String hyphenationString) {
        List<Syllable> result = new ArrayList<>();

        List<String> syllables = Arrays.asList(hyphenationString.split(SYLLABLE_SEPARATOR));
        for (String syllableString : syllables) {
            String symbol = syllableString.substring(syllableString.length() - STRESS_SYMBOL.length());
            String syllable = syllableString.substring(0, syllableString.length() - STRESS_SYMBOL.length());
            result.add(new Syllable(syllable, Stress.fromSymbol(symbol)));
        }

        return result;
    }
}
