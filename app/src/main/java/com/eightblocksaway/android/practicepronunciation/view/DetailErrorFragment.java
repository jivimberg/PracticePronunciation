package com.eightblocksaway.android.practicepronunciation.view;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.R;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link com.eightblocksaway.android.practicepronunciation.view.DetailErrorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailErrorFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String IMG_SRC_ID = "img_src_id";
    private static final String TEXT_STRING_ID = "text_string_id";

    private Drawable errorIcon;
    private String errorText;

    @InjectView(R.id.error_icon) ImageView icon;
    @InjectView(R.id.error_text) TextView text;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    public static DetailErrorFragment newInstance(int drawableId, int stringId) {
        DetailErrorFragment fragment = new DetailErrorFragment();
        Bundle args = new Bundle();
        args.putInt(IMG_SRC_ID, drawableId);
        args.putInt(TEXT_STRING_ID, stringId);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailErrorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            errorIcon = getResources().getDrawable(getArguments().getInt(IMG_SRC_ID));
            errorText = getResources().getString(getArguments().getInt(TEXT_STRING_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.detail_error_fragment, container, false);
        ButterKnife.inject(this, rootView);

        if(errorIcon != null){
            icon.setImageDrawable(errorIcon);
        }

        if(errorText != null){
            text.setText(errorText);
        }

        return rootView;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
