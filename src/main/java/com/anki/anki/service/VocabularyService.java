package com.anki.anki.service;

import com.anki.anki.model.Deck;
import com.anki.anki.repository.AnkiConnectRepository;
import com.anki.anki.repository.ChatGptRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class VocabularyService {

    private final ChatGptRepository chatGptRepository;
    private final AnkiConnectRepository ankiConnectRepository;

    public VocabularyService(ChatGptRepository chatGptRepository,
                             AnkiConnectRepository ankiConnectRepository) {
        this.chatGptRepository = chatGptRepository;
        this.ankiConnectRepository = ankiConnectRepository;
    }

    public void processWord(String turkishWord, String deckName) {
        String analysis;
        if (true) { // Use ChatGpt
            analysis = chatGptRepository.analyzeWord(turkishWord);
        }
        else
        {
            analysis = "[English]: Sana - emanet - ettiğim - kitap - çok - önemli.\n" +
                    "[Turkish]: The book that I entrusted to you is very important.\n" +
                    "[Rest]: In this sentence, \"emanet\" (trust/deposit) is the noun. \"Ettiğim\" is the past tense form of \"etmek\" (to do), with the suffix \"-im\" indicating \"I\" as the subject, forming a relative clause that describes \"kitap\" (book). The suffix \"-in\" in \"emanet ettiğim\" indicates possession, meaning \"that I entrusted.\" The sentence structure shows the relationship between the book and the action of entrusting it.";
        }
        // Initialize default values
        String englishWord = "[Unknown]";
        String exampleEnglishSentence = "[No Example]";
        String exampleTurkishSentence = "[No Example]";
        String explanation = "[No Explanation]";

        // Split the response into lines
        String[] lines = analysis.split("\n");

        // Loop through each line to extract relevant information
        for (String line : lines) {
            // Extract English word from "English:" line
            if (line.startsWith("[Translation]:")) {
                englishWord = line.split(":", 2)[1].trim();
            }
            // Extract English sentence from "Example English sentence:" line
            else if (line.startsWith("[English]:")) {
                exampleEnglishSentence = line.split(":", 2)[1].trim();
            }
            // Extract Turkish sentence from "Example Turkish sentence:" line
            else if (line.startsWith("[Turkish]:")) {
                exampleTurkishSentence = line.split(":", 2)[1].trim();
            }
            // Extract explanation from "Explanation:" line
            else if (line.startsWith("[Rest]:")) {
                explanation = line.split(":", 2)[1].trim();
            }
        }

        // Format the result for the `analysis` field
        String formattedAnalysis = String.format(
                "[English]: %s\n[Turkish]: %s\n[Rest]: %s",
                exampleEnglishSentence,
                exampleTurkishSentence,
                explanation
        );



        // Add two Anki cards
        ankiConnectRepository.addCard(deckName, englishWord, turkishWord, formattedAnalysis);
        ankiConnectRepository.addCard(deckName, turkishWord, englishWord, formattedAnalysis);

        System.out.println("✅ Created 2 cards for '" + turkishWord + "' (" + "englishWord" + ")");
        System.out.println("\n--- ChatGPT Analysis ---\n" + "analysis");
    }

    // Directly call sendRequest to get deck names
    public List<String> getAllDecks() {
        List<Deck> response = ankiConnectRepository.getAllDecks();

        // Extract result list
        System.out.println(response);
        return List.of();
    }

}
