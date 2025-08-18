package com.anki.anki.model.anki_element;

import java.util.List;

public class Card {
    // Always to be treated as HTML
    public String frontValue;
    // Always to be treated as HTML
    public String backValue;

    public Card(String frontValue, String backValue, String sourceLanguage, List<String> destinationLanguage) {
        this.frontValue = frontValue;
        this.backValue = backValue;
    }
}
