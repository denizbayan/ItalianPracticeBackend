package com.itpractice.backend.game.entity;

import com.itpractice.backend.common.enums.*;

import java.util.List;
import java.util.UUID;

public class WordEntity {

    private UUID id;
    private String italianSingular;
    private String italianPlural;
    private String englishSingular;
    private String englishPlural;

    private ItalianArticlesEnum articleSingular;
    private ItalianArticlesEnum articlePlural;

    private GenderEnum gender;
    private WordTypeEnum wordType;

    private List<ExampleSentence> exampleSentences;


    private Boolean isIrregularPlural;
    private String notes;
    private ItalianLevelEnum level;

    public static class ExampleSentence{
        private String italianExample;
        private String englishExample;
        private GrammaticalNumberEnum grammaticalNumber;
    }
}

