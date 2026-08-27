package com.anki.anki.repository;

import com.anki.anki.model.Deck;
import com.anki.anki.model.anki_element.Card;

import java.util.List;
import java.util.Optional;

public interface AnkiDeckRepository {

    // Deck functions
    List<Deck> getAllDecks();
    Optional<Deck> getDeckByName(String name);
    void addDeck(Deck deck);
    boolean deleteDeck(String name);
    void updateDeck(Deck deck);

    // Card functions
    void addCardToDeck(String deckName, Card card);
    void removeCardFromDeck(String deckName, String frontText); // or card ID if you use one
}
