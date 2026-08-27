package com.anki.anki.service;

import com.anki.anki.model.settings.TextCreationSettings;

// Use this to create a text to practice the words learned from the deck
public class TextCreator extends ContentCreator{
    private TextCreationSettings settings;

    public TextCreator (TextCreationSettings settings) {
        this.settings = settings;
    }
}
