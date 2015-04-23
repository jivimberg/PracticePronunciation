package com.eightblocksaway.android.practicepronunciation.model;

import org.jetbrains.annotations.NotNull;

public final class Syllable {
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

    public enum Stress {
        PRIMARY_STRESS, SECONDARY_STRESS, NONE;

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
    }
}
