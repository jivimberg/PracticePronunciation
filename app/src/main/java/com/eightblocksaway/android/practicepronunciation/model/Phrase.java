package com.eightblocksaway.android.practicepronunciation.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class Phrase implements Parcelable{

    private final String phrase;
    private final List<Definition> definitions;
    private final List<Syllable> hyphenation;
    private final String pronunciation;

    public Phrase(@NotNull String phrase,
                  @NotNull String pronunciation,
                  @NotNull List<Definition> definitions,
                  @NotNull List<Syllable> hyphenation) {
        this.phrase = phrase;
        this.pronunciation = pronunciation;
        this.definitions = definitions;
        this.hyphenation = hyphenation;
    }

    @NotNull
    public String getPhrase() {
        return phrase;
    }

    @NotNull
    public List<Definition> getDefinitions() {
        return definitions;
    }

    @NotNull
    public List<Syllable> getHyphenation() {
        return hyphenation;
    }

    @NotNull
    public String getPronunciation() {
        return pronunciation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Phrase phrase1 = (Phrase) o;

        if (!phrase.equals(phrase1.phrase)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return phrase.hashCode();
    }

    @Override
    public String toString() {
        return "Phrase{" +
                "phrase='" + phrase + '\'' +
                ", definitions=" + definitions +
                ", hyphenation=" + hyphenation +
                ", pronunciation='" + pronunciation + '\'' +
                '}';
    }

    protected Phrase(Parcel in) {
        phrase = in.readString();
        if (in.readByte() == 0x01) {
            definitions = new ArrayList<Definition>();
            in.readList(definitions, Definition.class.getClassLoader());
        } else {
            definitions = null;
        }
        if (in.readByte() == 0x01) {
            hyphenation = new ArrayList<Syllable>();
            in.readList(hyphenation, Syllable.class.getClassLoader());
        } else {
            hyphenation = null;
        }
        pronunciation = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(phrase);
        if (definitions == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(definitions);
        }
        if (hyphenation == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(hyphenation);
        }
        dest.writeString(pronunciation);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Phrase> CREATOR = new Parcelable.Creator<Phrase>() {
        @Override
        public Phrase createFromParcel(Parcel in) {
            return new Phrase(in);
        }

        @Override
        public Phrase[] newArray(int size) {
            return new Phrase[size];
        }
    };
}
