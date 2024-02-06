package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.GenreDTO;
import org.example.entity.Genre;
import org.example.mapper.GenreMapper;
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

public class GenreService {
    private static final int ROWS_PER_PAGE = 10;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void showGenres(HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, ServletException {

        String currentPage = request.getParameter("page");
        int normalizedPage = currentPage == null ? 1 : Integer.parseInt(currentPage);

        List<Genre> genres;
        try {
            genres = GenresRepository.findEntities(normalizedPage - 1, ROWS_PER_PAGE);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        List<GenreDTO> genreDTOS = new ArrayList<>(genres).stream()
                .map(GenreMapper::toDTO)
                .toList();
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(genreDTOS);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }

    public static void showGenre(HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException, ServletException {

        String id = getId(request);
        Long normalizedId = Long.parseLong(Objects.requireNonNull(id));

        Genre genre;
        try {
            genre = GenresRepository.findById(normalizedId)
                    .orElseThrow(() -> new RuntimeException("Genre not found"));
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        var genreDTO = GenreMapper.toDTO(genre);
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(genreDTO);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }

    public static void createGenre(HttpServletRequest request,
                                   HttpServletResponse response)
            throws IOException, ServletException {

        String name = request.getParameter("name");
        Genre genre = new Genre(name);

        try {
            GenresRepository.save(genre);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        var genreDTO = GenreMapper.toDTO(genre);
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(genreDTO);
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }

    public static void updateGenre(HttpServletRequest request,
                                   HttpServletResponse response)
            throws IOException, ServletException {

        String id = getId(request);
        Long normalizedId = Long.parseLong(Objects.requireNonNull(id));

        String name = request.getParameter("name");
        Genre genre = new Genre(name);

        try {
            GenresRepository.update(genre, normalizedId);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        var genreDTO = GenreMapper.toDTO(genre);
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(genreDTO);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();

    }

    public static void destroyGenre(HttpServletRequest request,
                                    HttpServletResponse response)
            throws IOException, ServletException {

        String id = getId(request);
        Long normalizedId = Long.parseLong(Objects.requireNonNull(id));

        try {
            GenresRepository.deleteById(normalizedId);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
