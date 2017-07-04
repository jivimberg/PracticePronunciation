package com.eightblocksaway.android.practicepronunciation.view;


import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.data.DataUtil;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationContract;
import com.eightblocksaway.android.practicepronunciation.data.PronunciationProvider;
import com.eightblocksaway.android.practicepronunciation.model.Definition;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.model.Stress;
import com.eightblocksaway.android.practicepronunciation.model.Syllable;

import org.jetbrains.annotations.NotNull;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PHRASE = "phrase";
    private static final int LOADER_ID = 3;

    private Phrase phrase;
    private PhraseSelectCallback callback;

    @InjectView(R.id.detail_phrase_text) TextView phraseText;
    @InjectView(R.id.hyphenation_list) LinearLayout hyphenation;
    @InjectView(R.id.definition_list) LinearLayout definitionList;
    @InjectView(R.id.detail_points_label) TextView detailPointsLabel;
    @InjectView(R.id.detail_points_layout) LinearLayout detailPointsLayout;
    @InjectView(R.id.detail_points_text) TextView detailPointsText;
    @InjectView(R.id.detail_points_bar) ProgressBar detailPointsBar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    public static DetailFragment newInstance(@NotNull Phrase phrase) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(PHRASE, phrase);
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
            phrase = getArguments().getParcelable(PHRASE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.detail_fragment, container, false);
        ButterKnife.inject(this, root);

        if(phrase != null){

            //set phrase
            phraseText.setText(phrase.getPhrase());
            phraseText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onPhraseSelected(phrase);
                }
            });

            if(phrase.isPersisted()){
                showPoints();
                getLoaderManager().initLoader(LOADER_ID, null, this);
            }

            //set hyphenation
            for (Syllable syllable : phrase.getHyphenation()) {
                TextView syllableTV = new TextView(getActivity());
                syllableTV.setText(syllable.getText());
                syllableTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                syllableTV.setTextColor(Color.BLACK);
                if(syllable.getStress().equals(Stress.PRIMARY_STRESS)){
                    syllableTV.setTypeface(Typeface.DEFAULT_BOLD);
                    syllableTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                }
                hyphenation.addView(syllableTV);

                //TODO move to a layout and inflate?
                TextView separator = new TextView(getActivity());
                separator.setText("●");
                separator.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);
                separator.setPadding(20, 20, 20, 20);
                separator.setTextColor(Color.BLACK);
                hyphenation.addView(separator);
            }

            //remove last separator
            if(hyphenation.getChildCount() > 0) {
                hyphenation.removeViewAt(hyphenation.getChildCount() - 1);
            }


            //set definitions
            int definitionCount = 1;
            for (Definition definition : phrase.getDefinitions()) {
                View definitionLayout = getLayoutInflater(null).inflate(R.layout.definition, definitionList, false);

                TextView definitionNumber = ButterKnife.findById(definitionLayout, R.id.definition_number);
                definitionNumber.setText(Integer.toString(definitionCount++));

                TextView definitionTV = ButterKnife.findById(definitionLayout, R.id.definition);
                definitionTV.setText(Html.fromHtml("<i>" + definition.getPartOfSpeech() + ".</i> "
                        + "<font color=\"black\">" + definition.getDefinition() + "</font>"));

                definitionList.addView(definitionLayout);
            }
        }

        return root;

    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void showPoints() {
        detailPointsLabel.setVisibility(View.VISIBLE);
        detailPointsLayout.setVisibility(View.VISIBLE);

        int points = phrase.getPoints();
        detailPointsText.setText(points + "/" + getActivity().getResources().getInteger(R.integer.max_points));

        detailPointsBar.setProgress(points);
    }

    public void onPhraseAdded(@NotNull Phrase phrase){
        this.phrase = phrase;

        if(phrase.isPersisted()){
            showPoints();
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (PhraseSelectCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = PronunciationContract.PhraseEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), uri,
                null,
                PronunciationProvider.phraseByTextSelector,
                new String[] { phrase.getPhrase() }, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            this.phrase = DataUtil.fromCursor(data);
            showPoints();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.phrase = null;
        //TODO should I do anything else here?
    }
}
