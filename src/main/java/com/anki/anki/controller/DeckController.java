package com.anki.anki.controller;

import com.anki.anki.model.Deck;
import com.anki.anki.mock.MockDataProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeckController {

    @GetMapping("/api/decks")
    public List<Deck> getDecks() {
        return MockDataProvider.getFakeDecks();
    }
}