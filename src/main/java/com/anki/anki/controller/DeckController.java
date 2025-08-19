package com.anki.anki;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
class DeckController {

    @Autowired
    private CardService cardService;

    @GetMapping("/card/{id}")
    String card(@PathVariable int id) {
        return cardService.getCardById(id);
    }
}