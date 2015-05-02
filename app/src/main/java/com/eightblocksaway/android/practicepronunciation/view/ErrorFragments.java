package com.eightblocksaway.android.practicepronunciation.view;

import android.support.v4.app.Fragment;

import com.eightblocksaway.android.practicepronunciation.R;

public enum ErrorFragments {
    NO_WIFI(R.drawable.ic_device_signal_wifi_off, R.string.no_wifi_text),
    WORD_NOT_FOUND(R.drawable.ic_action_search, R.string.word_not_found_text),
    APP_ERROR(R.drawable.ic_action_bug_report, R.string.app_error_text);

    private Fragment fragmentInstance;

    private ErrorFragments(int drawableId, int stringId) {
        this.fragmentInstance = DetailErrorFragment.newInstance(drawableId, stringId);
    }

    public Fragment getFragmentInstance() {
        return fragmentInstance;
    }
}
