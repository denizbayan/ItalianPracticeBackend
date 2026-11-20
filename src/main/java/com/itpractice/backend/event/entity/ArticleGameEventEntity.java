package com.itpractice.backend.event.entity;

import com.itpractice.backend.game.enums.ArticleGameDifficulty;
import com.itpractice.backend.game.enums.ArticleGameType;

import java.util.Map;
import java.util.UUID;

public class ArticleGameEventEntity extends GameEventEntity{

    private UUID wordID;
    private ArticleGameDifficulty selectedDifficulty;
    private ArticleGameType selectedGameType;

    private Map<String,Boolean> levelResult;
}
