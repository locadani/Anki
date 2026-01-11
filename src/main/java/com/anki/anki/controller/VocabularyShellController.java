package com.anki.anki.controller;

import com.anki.anki.model.anki_element.Card;
import com.anki.anki.service.CardService;
import com.anki.anki.service.VocabularyService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
public class VocabularyShellController {

    private final VocabularyService vocabularyService;
    private final CardService cardService;

    // Constructor injection for all services
    public VocabularyShellController(VocabularyService vocabularyService,
                                     CardService cardService) {
        this.vocabularyService = vocabularyService;
        this.cardService = cardService;
    }

    @ShellMethod(
            key = {"aw", "add-word"},
            value = "Translate a word, fetch example, and add it to Anki deck"
    )
    public void addWord(
            @ShellOption(help = "Word to translate") String word
    ) {
        String language = "Turkish"; // Fetch the language from settings
        String deckName = "Turkish101"; // Fetch from settings

        // Talking about the word, don't trim, make lowercase, etc., as we allow multiple cards for the same word
        Card existingCard = null;
        try {
            existingCard = cardService.getCardByFrontValue(deckName, word);
        } catch (RuntimeException e) {
            System.out.println("Card not found, adding it now.");
        }

        if (existingCard == null) {
            vocabularyService.processWord(word, deckName);
        } else {
            System.out.println(word + " card already exists in deck " + deckName);
        }
        System.out.println();
    }

    @ShellMethod("List all Anki decks")
    public void listDecks() {
        List<String> decks = vocabularyService.getAllDecks(); // Get a list of deck names
        if (decks.isEmpty()) {
            System.out.println("No decks found in Anki.");
        } else {
            System.out.println("Anki decks:");
            decks.forEach(deck -> System.out.println(" - " + deck));
        }
    }
}
