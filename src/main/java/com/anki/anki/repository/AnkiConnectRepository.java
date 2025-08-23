package com.anki.anki.repository;

import com.anki.anki.model.Deck;
import com.anki.anki.model.anki_element.Card;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Repository
public class AnkiConnectRepository implements AnkiDeckRepository {

    private static final String ANKI_CONNECT_URL = "http://127.0.0.1:8765";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Helper method to send requests to AnkiConnect using Apache HttpClient
    private Map<String, Object> sendRequest(String action, Map<String, Object> params) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", action);
        requestBody.put("version", 6);
        requestBody.put("params", params);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(ANKI_CONNECT_URL);
            post.setHeader("Content-Type", "application/json");

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            post.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            System.out.println("Sending request to AnkiConnect: " + jsonBody);

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                Map<String, Object> body = objectMapper.readValue(responseString, Map.class);

                if (body == null || !body.containsKey("result")) {
                    throw new RuntimeException("Invalid response from AnkiConnect: " + body);
                }

                return body;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to AnkiConnect: " + e.getMessage(), e);
        }
    }
    @Override
    public List<Deck> getAllDecks() {
        Map<String, Object> response = sendRequest("deckNames", Collections.emptyMap());

        List<String> deckNames = (List<String>) response.get("result");
        List<Deck> decks = new ArrayList<>();

        for (String deckName : deckNames) {
            // Step 1: Find card IDs in this deck
            Map<String, Object> findParams = Map.of("query", "deck:" + deckName);
            Map<String, Object> findResponse = sendRequest("findCards", findParams);
            List<Integer> cardIds = (List<Integer>) findResponse.get("result");

            List<Card> cards = new ArrayList<>();

            if (cardIds != null && !cardIds.isEmpty()) {
                // Step 2: Get detailed card info
                Map<String, Object> infoParams = Map.of("cards", cardIds);
                Map<String, Object> infoResponse = sendRequest("cardsInfo", infoParams);
                List<Map<String, Object>> cardsInfo = (List<Map<String, Object>>) infoResponse.get("result");

                for (Map<String, Object> cardInfo : cardsInfo) {
                    Map<String, Object> fields = (Map<String, Object>) cardInfo.get("fields");

                    // Extract front and back values from "Fronte" and "Retro" keys
                    Map<String, Object> frontMap = (Map<String, Object>) fields.get("Fronte");
                    Map<String, Object> backMap = (Map<String, Object>) fields.get("Retro");

                    String front = frontMap != null ? (String) frontMap.get("value") : "";
                    String back = backMap != null ? (String) backMap.get("value") : "";

                    cards.add(new Card(front, back));
                }
            }

            decks.add(new Deck(deckName, cards));
        }

        return decks;
    }


    @Override
    public Optional<Deck> getDeckByName(String name) {
        return getAllDecks().stream()
                .filter(deck -> deck.name.equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public void addDeck(Deck deck) {
        sendRequest("createDeck", Map.of("deck", deck.name));
    }

    @Override
    public boolean deleteDeck(String name) {
        sendRequest("deleteDecks", Map.of("decks", List.of(name)));
        return true;
    }

    @Override
    public void updateDeck(Deck deck) {
        throw new UnsupportedOperationException("Update deck not supported via AnkiConnect");
    }

    @Override
    public void addCardToDeck(String deckName, Card card) {
        Map<String, String> fields = new HashMap<>();
        fields.put("Front", card.frontValue);
        fields.put("Back", card.backValue);

        Map<String, Object> note = new HashMap<>();
        note.put("deckName", deckName);
        note.put("modelName", "Basic");
        note.put("fields", fields);
        note.put("options", Map.of("allowDuplicate", false));
        note.put("tags", List.of());

        Map<String, Object> params = Map.of("note", note);
        Map<String, Object> response = sendRequest("addNote", params);

        if (response.get("result") == null) {
            throw new RuntimeException("Failed to add card: " + card.frontValue);
        }
    }

    @Override
    public void removeCardFromDeck(String deckName, String frontText) {
        Map<String, Object> findParams = Map.of("query", "deck:" + deckName + " front:" + frontText);
        Map<String, Object> findResponse = sendRequest("findCards", findParams);
        List<Integer> cardIds = (List<Integer>) findResponse.get("result");

        if (cardIds != null && !cardIds.isEmpty()) {
            sendRequest("deleteCards", Map.of("cards", cardIds));
        }
    }
}
