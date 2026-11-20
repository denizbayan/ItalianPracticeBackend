package com.itpractice.backend.event.entity;

import com.itpractice.backend.event.enums.EventType;

import java.util.UUID;

public class BaseEventEntity {

    private UUID user_id;
    private String platform;
    private String country;
    private String device;
    private Long sent_timestamp;
    private Long created_timestamp;
    private EventType eventType;

}
