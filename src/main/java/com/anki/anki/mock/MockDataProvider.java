package com.anki.anki.mock;

import com.anki.anki.model.Deck;
import com.anki.anki.model.anki_elements.Card;
import com.anki.anki.model.languages.Language;
import com.anki.anki.model.settings.DeckSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MockDataProvider {

    public static List<Deck> getFakeDecks() {
        Card card1 = new Card("Hallo", "Hello", Language.GERMAN.language, Collections.singletonList(Language.ENGLISH.language));
        Card card2 = new Card("Auf Wiedersehen", "Goodbye", Language.GERMAN.language, Collections.singletonList(Language.ENGLISH.language));

        DeckSettings settings = new DeckSettings();
        Deck germanDeck = new Deck("German", Arrays.asList(card1, card2), settings);

        return List.of(germanDeck);
    }
}
