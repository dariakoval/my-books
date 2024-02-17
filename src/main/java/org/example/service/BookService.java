package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.BookDTO;
import org.example.entity.Book;
import org.example.mapper.BookMapper;
import org.example.repository.BooksRepository;
import org.example.repository.GenresRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.example.util.RequestUtil.getId;

public class BookService {
    private static final int ROWS_PER_PAGE = 10;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void showBooks(HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {

        String currentPage = request.getParameter("page");
        int normalizedPage = currentPage == null ? 1 : Integer.parseInt(currentPage);

        String searchAuthor = request.getParameter("author"); //  в константу
        String searchGenre = request.getParameter("genre");

        List<Book> books;
        try {
            if (searchAuthor != null) {
                books = BooksRepository.findEntitiesByAuthor(searchAuthor, normalizedPage - 1, ROWS_PER_PAGE);
            } else if (searchGenre != null) {
                books = BooksRepository.findEntitiesByGenre(searchGenre, normalizedPage - 1, ROWS_PER_PAGE);
            } else {
                books = BooksRepository.findEntities(normalizedPage - 1, ROWS_PER_PAGE);
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        List<BookDTO> bookDTOS = new ArrayList<>(books).stream().map(BookMapper::toDTO).toList();
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(bookDTOS);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }

    public static void showBook(HttpServletRequest request,
                                HttpServletResponse response)
            throws IOException, ServletException {

        String id = getId(request);
        Long normalizedId = Long.parseLong(Objects.requireNonNull(id));

        Book book;
        try {
            book = BooksRepository.findById(normalizedId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        var bookDTO = BookMapper.toDTO(book);
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(bookDTO);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }


    public static void createBook(HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, ServletException {

        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String genreName = request.getParameter("genreName");

        Book book;
        try {
            var genre = GenresRepository.findByName(genreName)
                    .orElseThrow(() -> new RuntimeException("Genre not found"));
            book = new Book(title, author, genre);
            BooksRepository.save(book);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        var bookDTO = BookMapper.toDTO(book);
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(bookDTO);
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();

    }

    public static void updateBook(HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, ServletException {

        String id = getId(request);
        Long normalizedId = Long.parseLong(Objects.requireNonNull(id));

        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String genreName = request.getParameter("genreName");

        Book book;
        try {
            var genre = GenresRepository.findByName(genreName)
                    .orElseThrow(() -> new RuntimeException("Genre not found"));
            book = new Book(title, author, genre);
            BooksRepository.update(book, normalizedId);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        var bookDTO = BookMapper.toDTO(book);
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(bookDTO);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }

    public static void destroyBook(HttpServletRequest request,
                                   HttpServletResponse response)
            throws IOException, ServletException {

        String id = getId(request);
        Long normalizedId = Long.parseLong(Objects.requireNonNull(id));

        try {
            BooksRepository.deleteById(normalizedId);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
