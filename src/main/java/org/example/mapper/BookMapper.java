package org.example.mapper;

import org.example.dto.BookDTO;
import org.example.entity.Book;

public class BookMapper {
    public static BookDTO toDTO(Book book) {
        var bookDTO = new BookDTO();
        bookDTO.setId(book.getId());
        bookDTO.setTitle(book.getTitle());
        bookDTO.setAuthor(book.getAuthor());
        bookDTO.setGenreName(book.getGenre().getName());

        return bookDTO;
    }
}
