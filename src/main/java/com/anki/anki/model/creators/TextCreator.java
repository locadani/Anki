package com.anki.anki.model.creators;

import com.anki.anki.model.Deck;
import com.anki.anki.model.settings.TextCreationSettings;

// Use this to create a text to practice the words learned from the deck
public class TextCreator extends ContentCreator{
    private Deck startingDeck;
    private TextCreationSettings settings;

    public TextCreator (Deck startingDeck, TextCreationSettings settings) {
        this.startingDeck = startingDeck;
        this.settings = settings;
    }
}
