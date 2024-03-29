DROP TABLE IF EXISTS books CASCADE;

CREATE TABLE books (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  title VARCHAR(255) UNIQUE NOT NULL,
  author VARCHAR(255) NOT NULL,
  genre_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT PK_books PRIMARY KEY (id)
);

DROP TABLE IF EXISTS genres;

CREATE TABLE genres (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name varchar(255) UNIQUE NOT NULL,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT PK_genres PRIMARY KEY (id)
);

ALTER TABLE books
  ADD FOREIGN KEY (genre_id) REFERENCES genres (id);

DROP TABLE IF EXISTS reviews;

CREATE TABLE reviews (
  book_id BIGINT UNIQUE NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL
);

ALTER TABLE reviews
  ADD CONSTRAINT FK_reviews_books FOREIGN KEY (book_id)
  REFERENCES books (id) ON DELETE CASCADE;