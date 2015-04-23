package com.eightblocksaway.android.practicepronunciation.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eightblocksaway.android.practicepronunciation.R;
import com.eightblocksaway.android.practicepronunciation.model.Phrase;

import org.jetbrains.annotations.NotNull;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link com.eightblocksaway.android.practicepronunciation.view.DetailErrorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailErrorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String IMG_SRC_ID = "param2";

    // TODO: Rename and change types of parameters
    private int img_src_id = R.drawable.ic_device_signal_wifi_off;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailErrorFragment newInstance(int drawableId) {
        DetailErrorFragment fragment = new DetailErrorFragment();
        Bundle args = new Bundle();
        args.putInt(IMG_SRC_ID, drawableId);
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
            img_src_id = getArguments().getInt(IMG_SRC_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.detail_error_fragment, container, false);

        ImageView icon = (ImageView) rootView.findViewById(R.id.error_icon);
        icon.setImageDrawable(getResources().getDrawable(img_src_id));

        return rootView;
    }

}
