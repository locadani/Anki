package com.anki.anki.model;

import java.nio.file.Path;
import java.util.List;

public class Document {
    private String name;
    private String sourceLanguage;
    private Path path;

    public Document(String name, String sourceLanguage, Path path) {
        this.name = name;
        this.sourceLanguage = sourceLanguage;
        this.path = path;
    }
}
