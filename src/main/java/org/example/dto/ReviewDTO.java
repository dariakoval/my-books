package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {

    private Long id;

    private String bookTitle;

    private String bookAuthor;

    private String content;
}
