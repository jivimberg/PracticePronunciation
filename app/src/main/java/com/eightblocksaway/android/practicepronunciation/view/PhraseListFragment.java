package com.eightblocksaway.android.practicepronunciation.view;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.data.PhrasesCursorAdapter;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationContract;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PhraseListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{

    private static final int LOADER_ID = 1;
    private static final String LOG_TAG = "PhraseListFragment";

    private PhrasesCursorAdapter phrasesCursorAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.phrase_list_fragment, container, false);
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        DynamicListView phraseList = (DynamicListView) rootView.findViewById(R.id.phrase_list);
//            phraseList.enableSwipeToDismiss(
//                    new OnDismissCallback() {
//                        @Override
//                        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
//                            final String phrase = getCurrentPhrase();
//                            for (int position : reverseSortedPositions) {
//                                Cursor cursor = (Cursor) phrasesCursorAdapter.getItem(position);
//                                String removingPhrase = cursor.getString(cursor.getColumnIndex(PhraseEntry.COLUMN_TEXT));
//                                getActivity().getContentResolver().delete(PhraseEntry.CONTENT_URI, PronunciationProvider.phraseByTextSelector, new String[]{removingPhrase});
//
//                                if(phrase.equals(removingPhrase)){
//                                    editText.setText("");
//                                }
//                            }
//                        }
//                    }
//            );
        phrasesCursorAdapter = new PhrasesCursorAdapter(getActivity(), R.layout.phrase_list_item, null, 0);
        phraseList.setAdapter(phrasesCursorAdapter);
        phraseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
//                editText.setText(cursor.getString(cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_TEXT)));
                Toast.makeText(getActivity(), "I felt a click", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = PronunciationContract.PhraseEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), uri, null, null, null, PronunciationContract.PhraseEntry._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        phrasesCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        phrasesCursorAdapter.swapCursor(null);
    }

}
