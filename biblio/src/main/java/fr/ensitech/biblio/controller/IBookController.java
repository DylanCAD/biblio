package fr.ensitech.biblio.controller;

import fr.ensitech.biblio.entity.Book;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IBookController {

    ResponseEntity<Book> createBook(Book book);
    ResponseEntity<Book> getBookById(long id);
    ResponseEntity<Book> updateBook(Book book);
    ResponseEntity<String> deleteBook(long id);
    ResponseEntity<List<Book>> getAllBooks();
}
