package com.anki.anki.repository;

import com.anki.anki.model.Deck;
import com.anki.anki.model.anki_element.Card;
import com.anki.anki.model.language.Language;
import com.anki.anki.model.settings.DeckSettings;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MockDeckRepositoryRepository implements AnkiDeckRepository {

    private final List<Deck> decks = new ArrayList<>();
    // Makes the method run after the injection (?)
    @PostConstruct
    public void init() {
        // Sample cards
        List<Card> frenchCards = List.of(
                new Card("Bonjour", "Hello"),
                new Card("Merci", "Thank you")
        );

        List<Card> spanishCards = List.of(
                new Card("Hola", "Hello"),
                new Card("Gracias", "Thank you")
        );

        // Sample decks
        Deck frenchDeck = new Deck("French Basics" , frenchCards
                //, new DeckSettings()
                );
        Deck spanishDeck = new Deck("Spanish Basics", spanishCards
                //, new DeckSettings()
                );

        // Add to map
        decks.add(frenchDeck);
        decks.add(spanishDeck);
    }

    public List<Deck> getAllDecks() {
        return new ArrayList<>(decks);
    }

    @Override
    public Optional<Deck> getDeckByName(String name) {
        // Search the list for the deck by name
        return decks.stream().filter(deck -> deck.name.equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public void addDeck(Deck deck) {
        decks.add(deck);
    }

    @Override
    public boolean deleteDeck(String name) {
        // Remove deck by name
        return decks.removeIf(deck -> deck.name.equalsIgnoreCase(name));
    }

    @Override
    public void updateDeck(Deck deck) {
        // Find and replace the deck by name
        int index = findDeckIndexByName(deck.name);
        if (index != -1) {
            decks.set(index, deck);
        }
    }

    @Override
    public void addCardToDeck(String deckName, Card card) {
        Deck deck = getDeckByName(deckName).orElse(null);
        if (deck != null) {
            deck.cards.add(card);
        }
    }

    @Override
    public void removeCardFromDeck(String deckName, String frontText) {
        Deck deck = getDeckByName(deckName).orElse(null);
        if (deck != null) {
            deck.cards.removeIf(card -> card.frontValue.equalsIgnoreCase(frontText));
        }
    }

    // Helper method to find deck index by name
    private int findDeckIndexByName(String name) {
        for (int i = 0; i < decks.size(); i++) {
            if (decks.get(i).name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }
}
