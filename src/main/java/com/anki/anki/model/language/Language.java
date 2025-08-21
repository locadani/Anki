package com.anki.anki.model.language;

public enum Language {
    ENGLISH("english"),
    ITALIAN("italian"),
    FRENCH("french"),
    SPANISH("spanish"),
    GERMAN("german");

    public final String language;
    Language(String language) {
        this.language = language;
    }
}
