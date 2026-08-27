package com.anki.anki.service;

import com.anki.anki.model.Deck;
import com.anki.anki.model.anki_element.Card;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CardService {

    @Autowired
    private DeckService deckService;

    public String getCardById(int id) {
        return "Card " + id + "!";
    }

    // Fetch a card by its front value
    public Card getCardByFrontValue(String deckName, String frontValue) {
        Optional<Deck> deck = deckService.getDeckByName(deckName);
        if (deck.isEmpty()) throw new RuntimeException("Deck " + deckName + " not found");
        else {
            return deck.get().cards.stream()
                    .filter(c -> c.frontValue.equals(frontValue))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("In deck " + deckName + ", card " + frontValue + " not found"));
        }
    }

    // Delete a card given its front value
    public void deleteCardByFrontValue(String deckName, String frontValue) {
        Optional<Deck> deck = deckService.getDeckByName(deckName);
        if (deck.isEmpty()) throw new RuntimeException("Deck " + deckName + " not found");

        Deck foundDeck = deck.get();
        boolean removed = foundDeck.cards.removeIf(c -> c.frontValue.equals(frontValue));

        if (!removed) {
            throw new RuntimeException("In deck " + deckName + ", card " + frontValue + " not found");
        }
    }

    // Update the front value of a card
    public Card updateFrontAndBackOfCard(String deckName, String frontValue, String newFront, String newBack) {
        Optional<Deck> deck = deckService.getDeckByName(deckName);
        if (deck.isEmpty()) throw new RuntimeException("Deck " + deckName + " not found");

        Deck foundDeck = deck.get();
        Card card = foundDeck.cards.stream()
                .filter(c -> c.frontValue.equals(frontValue))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("In deck " + deckName + ", card " + frontValue + " not found"));

        // Update only the front and back, every other data is untouched
        card.frontValue = newFront;
        card.backValue = newBack;
        return card;
    }

    // Add a new card to the deck
    public void addCardToDeck(String deckName, Card card) {
        Optional<Deck> deck = deckService.getDeckByName(deckName);

        if (deck.isPresent()) {
            // Check if the card already exists based on the front value
            boolean cardExists = deck.get().cards.stream()
                    .anyMatch(existingCard -> existingCard.frontValue.equals(card.frontValue));

            if (cardExists) {
                throw new RuntimeException("Card with front value '" + card.frontValue + "' already exists in deck '" + deckName + "'.");
            } else {
                // Add the card if it doesn't already exist
                deck.get().cards.add(card);
            }
        } else {
            throw new RuntimeException("Deck not found");
        }
    }

}
