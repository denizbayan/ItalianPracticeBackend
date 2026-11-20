package com.itpractice.backend.event.entity;

import com.itpractice.backend.game.enums.LevelEndAction;

import java.util.UUID;

public class GameEventEntity extends BaseEventEntity {

    private UUID levelID;
    private Long levelDurationMS;
    private LevelEndAction levelEndAction;

}
