package org.example.entity;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class Genre {

    private Long id;

    private String name;

    private Timestamp createdAt;

    public Genre(String name) {
        this.name = name;
    }
}
