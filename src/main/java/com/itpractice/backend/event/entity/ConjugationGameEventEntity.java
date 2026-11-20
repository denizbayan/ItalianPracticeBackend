package com.itpractice.backend.event.entity;

import com.itpractice.backend.game.enums.ConjugationGameDifficulty;

import java.util.List;
import java.util.UUID;

public class ConjugationGameEventEntity extends GameEventEntity {

    private UUID verbID;
    private List<String> selectedTenses;
    private ConjugationGameDifficulty selectedDifficulty;

    private Integer totalGuessCount;
    private Integer correctGuessCount;
    private Integer wrongGuessCount;
    private List<String> correctGuessTenses;
    private List<String> wrongGuessTenses;



}
