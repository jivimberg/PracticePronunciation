package com.eightblocksaway.android.practicepronunciation.network;

import android.support.v7.app.ActionBar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public enum PronunciationTypeFormat {
    AHD("ahd-legacy"),
    ARPABET("arpabet");

    private String typeName;

    PronunciationTypeFormat(String typeName) {
        this.typeName = typeName;
    }

    @Nullable
    static PronunciationTypeFormat fromTypeName(@NotNull String typeName) {
        EnumSet<PronunciationTypeFormat> all = EnumSet.allOf(PronunciationTypeFormat.class);
        for (PronunciationTypeFormat ptf :all) {
            if(ptf.typeName.equals(typeName)){
                return ptf;
            }
        }

        return null;
    }

}
