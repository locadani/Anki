package com.anki.anki.model.creators;

import java.util.List;

// Given a string, create a card to add to the a deck
public class CardCreator extends ContentCreator{
    private String frontString;
    private List<String> destinationLanguages;

    public CardCreator (String frontString, List<String> destionationLanguages) {
        this.frontString = frontString;
        this.destinationLanguages = destionationLanguages;
    }
}
