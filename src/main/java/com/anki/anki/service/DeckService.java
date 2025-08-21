package com.anki.anki.service;

import com.anki.anki.model.Deck;
import com.anki.anki.repository.MockDeckRepositoryRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeckService {

    // Adding autowired doesnt allow to set final
    private final MockDeckRepositoryRepository deckRepository;

    public DeckService(MockDeckRepositoryRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    // Later load from file or DB
    public Optional<Deck> getDeckByName(String name) {
        return deckRepository.getDeckByName(name);
    }

    public List<Deck> getAllDecks() {
        return new ArrayList<>(deckRepository.getAllDecks());
    }

    public boolean createDeck(Deck deck) {
        if (deckRepository.getDeckByName(deck.name).isEmpty()) {
            deckRepository.addDeck(deck);
            return true;
        }
        return false;
    }

    public boolean deleteDeck(String name) {
        return deckRepository.deleteDeck(name);
    }
}
