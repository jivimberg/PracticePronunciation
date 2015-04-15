package com.eightblocksaway.android.practicepronunciation.view;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

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
    private Callback callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.phrase_list_fragment, container, false);

        DynamicListView phraseList = (DynamicListView) rootView.findViewById(R.id.phrase_list_fragment);
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
                //TODO change this to send only URI and use loaders?
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                String phrase = cursor.getString(cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_TEXT));
                callback.onPhraseSelected(phrase);
            }
        });

        // Needs to be after the phraseCursorAdapter creation
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onPhraseSelected(String phrase);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

}
