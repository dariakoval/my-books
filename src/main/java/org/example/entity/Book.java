package org.example.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Getter
@Setter
@Accessors(chain = true)
public class Book { // почему у сещности нет equals и hashcode? как их отличать друг от друга?

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
