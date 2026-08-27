package com.anki.anki.model.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AvaliableLanguages {
    // For now I just use an enum, but we should retrieve it from a general list
    public static List<String> getAvaliableLanguages() {
        // from all values of the enum Language, get the language names (that are strings)
        return new ArrayList<>(Arrays.stream(Language.values()).map((language) -> language.language).toList());
    }
}
