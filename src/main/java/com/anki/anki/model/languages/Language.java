package com.anki.anki.model.languages;

public enum Language {
    ENGLISH("english"),
    ITALIAN("italian"),
    GERMAN("german");

    public final String language;
    Language(String language) {
        this.language = language;
    }
}
