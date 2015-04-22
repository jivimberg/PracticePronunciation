package com.eightblocksaway.android.practicepronunciation.model;

import org.jetbrains.annotations.NotNull;

public final class Definition {
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
}
