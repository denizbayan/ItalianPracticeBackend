package com.itpractice.backend.game.entity;


import com.itpractice.backend.common.enums.ItalianLevelEnum;
import com.itpractice.backend.common.enums.ItalianVerbMoodEnum;
import com.itpractice.backend.common.enums.ItalianVerbPersonEnum;
import com.itpractice.backend.common.enums.ItalianVerbTenseEnum;

import java.util.List;
import java.util.UUID;

public class VerbEntity {

    private UUID id;

    private String infinitiveVerb;
    private ItalianVerbMoodEnum mood;
    private ItalianVerbTenseEnum tense;
    private ItalianVerbPersonEnum person;
    private String conjugation;

    private List<WordEntity.ExampleSentence> exampleSentences;

    private Boolean isIrregular;
    private String notes;
    private ItalianLevelEnum level;

    public static class ExampleSentence{
        private String italianExample;
        private String englishExample;
    }
}
