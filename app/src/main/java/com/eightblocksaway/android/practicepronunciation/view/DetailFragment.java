package com.eightblocksaway.android.practicepronunciation.view;


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
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationContract;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;

import org.jetbrains.annotations.NotNull;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PHRASE_URI = "param1";
    private static final String[] DETAIL_COLUMNS = {
            PronunciationContract.PhraseEntry.COLUMN_DEFINITIONS,
            PronunciationContract.PhraseEntry.COLUMN_HYPHENATION,
    };
    private static final int LOADER_ID = 2;

    private Uri phraseUri;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    public static DetailFragment newInstance(Uri uri) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(PHRASE_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phraseUri = getArguments().getParcelable(PHRASE_URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.detail_fragment, container, false);

        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        return root;

    }


    //TODO move this to use bundle!
    public void setPhrase(@NotNull Phrase phrase) {
        TextView textView = (TextView) getActivity().findViewById(R.id.textview);
        textView.setText(phrase.getDefinitions().toString());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if ( null != phraseUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    phraseUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            TextView textView = (TextView) getActivity().findViewById(R.id.textview);
            String definitions = cursor.getString(cursor.getColumnIndex(PronunciationContract.PhraseEntry.COLUMN_DEFINITIONS));
            textView.setText(definitions);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {}
}
