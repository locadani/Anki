package com.anki.anki.controller;


import com.anki.anki.model.Deck;
import com.anki.anki.service.DeckService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

// NOTE
/*
- Decks have ids, but in many api calls are identified by names, so we use names to identify them
 */
@RestController
@RequestMapping("/decks")
class DeckController {

    // TODO: da rimuovere?
    @Autowired
    private DeckService deckService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Deck> getAllDecks() {
        return deckService.getAllDecks();
    }

    // If the deckName has a space, we need to substitute it with %20
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{deckName}")
    public Optional<Deck> getDeck(@PathVariable String deckName) {
        return Optional.ofNullable(deckService.getDeckByName(deckName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found.")));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createDeck(@RequestBody Deck deck) {
        if(!deckService.createDeck(deck)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Deck already existing. Use APIs to add card to " + deck.name);
        }
        List<Deck> decks = getAllDecks();
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{deckName}")
    public void deleteDeck(@PathVariable String deckName) {
        if (!deckService.deleteDeck(deckName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find deck called " + deckName);
        }
    }

    @PostMapping("/uploadApkgFile")
    @ResponseStatus(HttpStatus.OK)
    public String uploadApkg(@RequestParam("file") MultipartFile file) {
        try {
            deckService.importApkg(file);
            return "Import successful";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Import failed: " + e.getMessage(), e);
        }
    }
}
