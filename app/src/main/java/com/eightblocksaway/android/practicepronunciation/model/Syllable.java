package com.eightblocksaway.android.practicepronunciation.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public final class Syllable implements Parcelable {
    private final String text;
    private final Stress stress;

    public Syllable(@NotNull String text, @NotNull Stress stress) {
        this.text = text;
        this.stress = stress;
    }

    @NotNull
    public String getText() {
        return text;
    }

    @NotNull
    public Stress getStress() {
        return stress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Syllable syllable = (Syllable) o;

        if (stress != syllable.stress) return false;
        if (!text.equals(syllable.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + stress.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Syllable{" +
                "text='" + text + '\'' +
                ", stress=" + stress +
                '}';
    }

    protected Syllable(Parcel in) {
        text = in.readString();
        stress = (Stress) in.readValue(Stress.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeValue(stress);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Syllable> CREATOR = new Parcelable.Creator<Syllable>() {
        @Override
        public Syllable createFromParcel(Parcel in) {
            return new Syllable(in);
        }

        @Override
        public Syllable[] newArray(int size) {
            return new Syllable[size];
        }
    };
}