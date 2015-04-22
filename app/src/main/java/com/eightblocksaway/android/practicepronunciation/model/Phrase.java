package com.eightblocksaway.android.practicepronunciation.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class Phrase {

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
}
