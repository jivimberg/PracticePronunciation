package com.eightblocksaway.android.practicepronunciation.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public final class Definition implements Parcelable {
    private final String partOfSpeech;
    private final String definition;

    public Definition(@NotNull String definition, @NotNull String partOfSpeech) {
        this.definition = definition;
        this.partOfSpeech = partOfSpeech;
    }

    @NotNull
    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    @NotNull
    public String getDefinition() {
        return definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Definition that = (Definition) o;

        if (!definition.equals(that.definition)) return false;
        if (!partOfSpeech.equals(that.partOfSpeech)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = partOfSpeech.hashCode();
        result = 31 * result + definition.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Definition{" +
                "partOfSpeech='" + partOfSpeech + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }

    protected Definition(Parcel in) {
        partOfSpeech = in.readString();
        definition = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(partOfSpeech);
        dest.writeString(definition);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Definition> CREATOR = new Parcelable.Creator<Definition>() {
        @Override
        public Definition createFromParcel(Parcel in) {
            return new Definition(in);
        }

        @Override
        public Definition[] newArray(int size) {
            return new Definition[size];
        }
    };
}