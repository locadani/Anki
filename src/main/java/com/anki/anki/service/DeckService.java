package com.anki.anki.service;

import com.anki.anki.model.Deck;
import com.anki.anki.model.anki_element.Card;
import com.anki.anki.repository.MockDeckRepositoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DeckService {

    // Adding autowired doesnt allow to set final
    private final MockDeckRepositoryRepository deckRepository;

    //@Autowired
    private CardService cardService;

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
        deckRepository.getDeckByName(deck.name).ifPresentOrElse(existingDeck -> deck.cards.stream()
                .filter(card -> existingDeck.cards.stream()
                        .noneMatch(existingCard -> existingCard.frontValue.equals(card.frontValue)))
                .forEach(card -> deckRepository.addCardToDeck(deck.name, card)), () -> {
            deckRepository.addDeck(deck);
        });

        return true;
    }

    public boolean deleteDeck(String name) {
        return deckRepository.deleteDeck(name);
    }

    public void importApkg(MultipartFile file) throws Exception {
        File tempFile = File.createTempFile("anki", ".db");

        // Step 1: Extract collection.anki2
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            boolean found = false;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("collection.anki2")) {
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("collection.anki2 not found in apkg file.");
            }
        }

        // Step 2: Connect to SQLite and read notes
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + tempFile.getAbsolutePath())) {
            Statement stmt = conn.createStatement();

            // Name the deck based on uploaded file
            String deckName = Objects.requireNonNull(file.getOriginalFilename()).replace(".apkg", "");
            // 1. Create the new deck
            Deck newDeck = new Deck(deckName, new ArrayList<>());

            // 2. Parse notes and use CardService to add cards
            ResultSet rs = stmt.executeQuery("SELECT * FROM notes");
            while (rs.next()) {
                String fields = rs.getString("flds");
                String[] parts = fields.split("\u001F");

                String front = parts.length > 0 ? parts[0] : "";
                String back = parts.length > 1 ? parts[1] : "";

                Card card = new Card(front, back);
                newDeck.cards.add(card);  // ✅ Reuse existing logic
            }
            createDeck(newDeck);  // Use your own createDeck method

        } catch (SQLException e) {
            System.err.println("Error reading SQLite DB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to read deck contents.", e);
        } finally {
            tempFile.delete(); // Clean up
        }
    }
}
