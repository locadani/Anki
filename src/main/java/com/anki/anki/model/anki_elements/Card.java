package com.anki.anki.model.anki_elements;

import java.util.List;

public class Card {
    // Always to be treated as HTML
    private String frontValue;
    // Always to be treated as HTML
    private String backValue;
    private String sourceLanguage;
    List<String> destinationLanguage;

    public String getFrontValue() {
        return frontValue;
    }

    public String getBackValue() {
        return backValue;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public List<String> getDestinationLanguage() {
        return destinationLanguage;
    }

    public Card(String frontValue, String backValue, String sourceLanguage, List<String> destinationLanguage) {
        this.frontValue = frontValue;
        this.backValue = backValue;
        this.sourceLanguage = sourceLanguage;
        this.destinationLanguage = destinationLanguage;
    }
}
