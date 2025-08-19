package com.anki.anki;

import org.springframework.stereotype.Service;

@Service
public class CardService {

    public String getCardById(int id) {
        return "Card " + id + "!";
    }
}