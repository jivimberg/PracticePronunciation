package com.eightblocksaway.android.practicepronunciation.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class Phrase implements Parcelable{

    private final String phrase;
    private final List<Definition> definitions;
    private final List<Syllable> hyphenation;
    private final String pronunciation;
    private boolean persisted;
    private int points;

    private Phrase(@NotNull String phrase,
                   @NotNull String pronunciation,
                   @NotNull List<Definition> definitions,
                   @NotNull List<Syllable> hyphenation,
                   boolean persisted,
                   int points) {
        this.phrase = phrase;
        this.pronunciation = pronunciation;
        this.definitions = definitions;
        this.hyphenation = hyphenation;
        this.points = points;
        this.persisted = persisted;
        this.points = points;
    }

    public static Phrase createPersisted(@NotNull String phrase,
                                         @NotNull String pronunciation,
                                         @NotNull List<Definition> definitions,
                                         @NotNull List<Syllable> hyphenation,
                                         int points) {
        return new Phrase(phrase, pronunciation, definitions, hyphenation, true, points);
    }

    public static Phrase createNotPersisted(@NotNull String phrase,
                                         @NotNull String pronunciation,
                                         @NotNull List<Definition> definitions,
                                         @NotNull List<Syllable> hyphenation) {
        return new Phrase(phrase, pronunciation, definitions, hyphenation, false, 0);
    }

    public static Phrase toPersisted(@NotNull Phrase currentPhrase) {
        return createPersisted(currentPhrase.getPhrase(), currentPhrase.getPronunciation(),
                currentPhrase.getDefinitions(), currentPhrase.getHyphenation(), 0);
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

    public boolean isPersisted() {
        return persisted;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Phrase phrase1 = (Phrase) o;

        //noinspection RedundantIfStatement
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
                ", persisted=" + persisted +
                ", points=" + points +
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
        persisted = in.readByte() != 0x00;
        points = in.readInt();
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
        dest.writeByte((byte) (persisted ? 0x01 : 0x00));
        dest.writeInt(points);
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
