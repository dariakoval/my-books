package org.example.entity;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class Book {

    private Long id;

    private String title;

    private String author;

    private Genre genre;

    private Timestamp createdAt;

    public Book(String title, String author, Genre genre) {
        this.title = title;
        this.author = author;
        this.genre = genre;
    }
}
