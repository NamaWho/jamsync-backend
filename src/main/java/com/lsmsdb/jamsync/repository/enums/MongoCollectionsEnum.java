package com.lsmsdb.jamsync.repository.enums;

import lombok.Getter;

@Getter
public enum MongoCollectionsEnum {
    MUSICIAN("musician"),
    BAND("band"),
    OPPORTUNITY("opportunity"),
    ANALYTICS_OPPORTUNITY("analyticsopportunity"),
    GENRES("genre"),
    INSTRUMENTS("instrument");

    private final String name;

    MongoCollectionsEnum(String name) {
        this.name = name;
    }
}
