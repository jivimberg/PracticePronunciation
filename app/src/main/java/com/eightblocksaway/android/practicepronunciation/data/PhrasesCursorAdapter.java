package com.eightblocksaway.android.practicepronunciation.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.R;

public class PhrasesCursorAdapter extends ResourceCursorAdapter {

    public PhrasesCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView phraseText = (TextView) view.findViewById(R.id.phrase_text);
        int columnTextIndex = cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_TEXT);
        String text = cursor.getString(columnTextIndex);
        phraseText.setText(text);

        TextView masteryLevelTextView = (TextView) view.findViewById(R.id.mastery_level);
        int columnMasteryLevelIndex = cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_MASTERY_LEVEL);
        int masteryLevel = cursor.getInt(columnMasteryLevelIndex);
        masteryLevelTextView.setText(Integer.toString(masteryLevel));
    }
}
