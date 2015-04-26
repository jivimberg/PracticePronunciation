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
import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PHRASE = "phrase";
    private static final int LOADER_ID = 2;

    private Phrase phrase;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    public static DetailFragment newInstance(Phrase phrase) {
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
            TextView textView = (TextView) root.findViewById(R.id.textview);
            textView.setText(phrase.getDefinitions().toString());
        }

        return root;

    }


    //TODO move this to use bundle!
    public void setPhrase(@NotNull Phrase phrase) {
        TextView textView = (TextView) getActivity().findViewById(R.id.textview);
        textView.setText(phrase.getDefinitions().toString());
    }
}
