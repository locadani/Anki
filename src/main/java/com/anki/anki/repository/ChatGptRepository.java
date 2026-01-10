package com.anki.anki.repository;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Repository;

@Repository
public class ChatGptRepository {

    private final ChatClient chatClient;

    public ChatGptRepository(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String analyzeWord(String turkishWord) {
        String prompt = """
                You are a Turkish language tutor.
                I will give you a Turkish word. For that word:
                1. Provide its English translation.
                2. Construct a Turkish example sentence using the provided word (split suffixes using dash, split words with a space Ex: araba-y-a git.). The sentence must make sense (possibly with a relative clause)
                3. Provide the English translation of that sentence.
                4. Explain the grammar of the sentence, focusing on how words and particles work.
                Add a brief explanation of: the suffixes used (don't explain plurals) and how the relative clause was built, plus add the meaning of the root of each word, when not trivial.
                Don't add further explanations. Keep it concise (max 20 sec reading time).
                
                Don't divide the explanation in bullet points.
                The response must be:
                [Translation]:
                [English]: 
                [Turkish]:
                [Rest]:
                Word: %s
                """.formatted(turkishWord);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
