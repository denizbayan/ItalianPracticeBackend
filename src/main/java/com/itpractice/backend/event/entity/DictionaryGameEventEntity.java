package com.itpractice.backend.event.entity;

import com.itpractice.backend.game.enums.DictionaryGameType;

import java.util.List;
import java.util.Map;

public class DictionaryGameEventEntity extends GameEventEntity {

    private DictionaryGameType gameType;
    private List<String> selectedWords;
    private Map<String,Boolean> levelResult;


}
