package com.anki.anki.service;

import java.util.List;

// Factory pattern
// Given a string, create a card to add to the a deck
public class CardCreator extends ContentCreator{
    private List<String> destinationLanguages;

    public CardCreator (List<String> destionationLanguages) {
        this.destinationLanguages = destionationLanguages;
    }
}
