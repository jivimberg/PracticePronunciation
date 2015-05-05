package com.eightblocksaway.android.practicepronunciation.view;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.model.Definition;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;
import com.eightblocksaway.android.practicepronunciation.model.Stress;
import com.eightblocksaway.android.practicepronunciation.model.Syllable;

import org.jetbrains.annotations.NotNull;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PHRASE = "phrase";

    private Phrase phrase;
    private PhraseSelectCallback callback;

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

        if(phrase != null){
            //set phrase
            TextView textView = (TextView) root.findViewById(R.id.phraseText);
            textView.setText(phrase.getPhrase());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onPhraseSelected(phrase);
                }
            });

            //set hyphenation
            LinearLayout hyphenation = (LinearLayout) root.findViewById(R.id.hyphenationList);
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
            hyphenation.removeViewAt(hyphenation.getChildCount() - 1);


            //set definitions
            LinearLayout definitionList = (LinearLayout) root.findViewById(R.id.definitionList);
            int definitionCount = 1;
            for (Definition definition : phrase.getDefinitions()) {
                View definitionLayout = getLayoutInflater(null).inflate(R.layout.definition, null);

                TextView definitionNumber = (TextView) definitionLayout.findViewById(R.id.definitionNumber);
                definitionNumber.setText(Integer.toString(definitionCount++));

                TextView definitionTV = (TextView) definitionLayout.findViewById(R.id.definition);
                definitionTV.setText(Html.fromHtml("<i>" + definition.getPartOfSpeech() + ".</i> "
                        + "<font color=\"black\">" + definition.getDefinition() + "</font>"));

                definitionList.addView(definitionLayout);
            }
        }

        return root;

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

}
