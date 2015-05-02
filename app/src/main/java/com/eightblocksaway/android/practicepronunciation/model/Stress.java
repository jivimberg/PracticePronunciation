package com.eightblocksaway.android.practicepronunciation.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public enum Stress implements Parcelable {
    PRIMARY_STRESS("*1"),
    SECONDARY_STRESS("*2"),
    NONE("*0");

    private final String symbol;

    private Stress(@NotNull String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Stress fromString(String stressType){
        switch (stressType){
            case "stress":
                return PRIMARY_STRESS;
            case "secondary stress":
                return SECONDARY_STRESS;
            default:
                return NONE;
        }
    }

    public static Stress fromSymbol(String symbol){
        for (Stress stress : Stress.values()) {
            if(stress.getSymbol().equals(symbol)){
                return stress;
            }
        }

        throw new IllegalArgumentException("No Stress found for symbol " + symbol);
    }

    public static final Parcelable.Creator<Stress> CREATOR = new Parcelable.Creator<Stress>() {

        public Stress createFromParcel(Parcel in) {
            String symbol = in.readString();
            return Stress.fromSymbol(symbol);
        }

        public Stress[] newArray(int size) {
            return new Stress[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(symbol);
    }
}