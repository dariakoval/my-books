package org.example.entity;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class Review {

    private Long id;

    private String content;

    private Book book;

    private Timestamp createdAt;

    public Review(String content, Book book) {
        this.content = content;
        this.book = book;
    }
}
