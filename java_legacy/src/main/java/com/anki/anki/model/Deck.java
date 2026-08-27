package com.anki.anki.model;

import com.anki.anki.model.anki_element.Card;
import com.anki.anki.model.settings.DeckSettings;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Deck {
    public String name;
    public List<Card> cards;
    //private DeckSettings settings; Può essere utile ma va capito come usarlo

    public Deck(String name, List<Card> cards) {
        this.name = name;
        this.cards = cards;
    }
}
