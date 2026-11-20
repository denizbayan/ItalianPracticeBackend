package com.itpractice.backend.common.enums;

public enum ItalianArticlesEnum {
    IL("il"),
    LO("lo"),
    LA("la"),
    L("l'"),
    I("i"),
    GLI("gli"),
    LE("le");

    private final String value;

    ItalianArticlesEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
