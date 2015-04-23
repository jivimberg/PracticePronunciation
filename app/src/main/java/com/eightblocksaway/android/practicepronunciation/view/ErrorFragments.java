package com.eightblocksaway.android.practicepronunciation.view;

import android.support.v4.app.Fragment;

import com.eightblocksaway.android.practicepronunciation.R;

public enum ErrorFragments {
    NO_WIFI(R.drawable.ic_device_signal_wifi_off),
    WORD_NOT_FOUND(R.drawable.ic_action_search),
    APP_ERROR(R.drawable.ic_action_bug_report);

    private Fragment fragmentInstance;

    private ErrorFragments(int drawableId) {
        this.fragmentInstance = DetailErrorFragment.newInstance(drawableId);
    }

    public Fragment getFragmentInstance() {
        return fragmentInstance;
    }
}
