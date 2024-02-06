package org.example.mapper;

import org.example.dto.GenreDTO;
import org.example.entity.Genre;

public class GenreMapper {
    public static GenreDTO toDTO(Genre genre) {
        var genreDTO = new GenreDTO();
        genreDTO.setId(genre.getId());
        genreDTO.setName(genre.getName());

        return genreDTO;
    }
}
