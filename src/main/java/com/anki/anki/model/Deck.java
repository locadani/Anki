package com.anki.anki.model;

import com.anki.anki.model.anki_element.Card;
import com.anki.anki.model.settings.DeckSettings;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Deck {
    private String name;
    private List<Card> cards;
    @JsonIgnore // for now, ignore converting it to json when sending to frontend, just because the settings were not yet defined
    private DeckSettings settings;

    public Deck(String name, List<Card> cards, DeckSettings settings) {
        this.name = name;
        this.cards = cards;
        this.settings = settings;
    }
}
