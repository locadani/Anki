package com.anki.anki.controller;

import com.anki.anki.model.anki_element.Card;
import com.anki.anki.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/decks/{deckName}")
public class CardController {

    @Autowired
    private CardService cardService;

    // Fetch a card by front value (already implemented)
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{frontValue}")
    public Card getCard(
            @PathVariable String deckName,
            @PathVariable String frontValue) {
        try {
            return cardService.getCardByFrontValue(deckName, frontValue);
        }
            catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Delete a card from the deck
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{frontValue}")
    public void deleteCard(
            @PathVariable String deckName,
            @PathVariable String frontValue) {
        try{
            cardService.deleteCardByFrontValue(deckName, frontValue);
        }
            catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Modify the front value of a card
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{frontValue}")
    public Card updateFrontAndBackOfCard(
            @PathVariable String deckName,
            @PathVariable String frontValue,
            @RequestBody Card newCard) {
        try {
            return cardService.updateFrontAndBackOfCard(deckName, frontValue, newCard.frontValue, newCard.backValue);
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void addCard(
            @PathVariable String deckName,
            @RequestBody Card card) {
        try {
            cardService.addCardToDeck(deckName, card);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
