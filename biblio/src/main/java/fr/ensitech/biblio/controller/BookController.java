package fr.ensitech.biblio.controller;

import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.service.IBookService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/books")
public class BookController implements IBookController {

    @Autowired
    private IBookService bookService;

    // => http://localhost:8080/api/books/infos
    @GetMapping("/infos")
    public ResponseEntity<String> getInfos() {
        return new ResponseEntity<String>("API REST Bibliothèque Ready.", HttpStatus.OK);
    }

    // => http://localhost:8080/api/books/date-format?dateformat=07/11/2025
    @PostMapping("/date-format")
    public void date(@RequestParam("dateformat")
                     @DateTimeFormat(pattern = "dd/MM/yyyy") Date dateformat) {
        System.out.println("dateformat : " + dateformat);
    }

//    @PostMapping("/date")
//    public void date(@RequestParam("date")
//                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
//        System.out.println("date: " + date);
//    }

    @PostMapping("/local-date")
    public void localDate(@RequestParam("localDate")
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate localDate) {
        System.out.println("localDate: " + localDate);
    }

    @PostMapping("/local-date-time")
    public void dateTime(@RequestParam("localDateTime")
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime localDateTime) {
        System.out.println("localDateTime: " + localDateTime);
    }

    // => http://localhost:8080/api/books/create
    @PostMapping("/create")
    @Override
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        if (!isBookOk(book)) {
            return new ResponseEntity<Book>(HttpStatus.BAD_REQUEST);
        }
        try {
            bookService.addOrUpdateBook(book);
            return new ResponseEntity<Book>(book, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<Book>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
// => http://localhost:8080/api/books/book?id=3
    @GetMapping("/book")
    public ResponseEntity<Book> getBookById(@RequestParam(value = "id", required = true) long id) {

// => http://localhost:8080/api/books/book/3
//    @GetMapping("/book/{id}")
//    public ResponseEntity<Book> getBookById(@PathVariable(value = "id", required = true) long id) {
        System.out.println("id = " + id);
        if (id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Book book = bookService.getBook(id);
            return new ResponseEntity<Book>(book, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Book>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // => http://localhost:8080/api/books/update
    @Override
    @PutMapping("/update")
    public ResponseEntity<Book> updateBook(@RequestBody Book book) {
        if (!isBookOk(book) || book.getId() <= 0) {

            return new ResponseEntity<Book>(HttpStatus.BAD_REQUEST);
        }

        try {
            bookService.addOrUpdateBook(book);
            Book _book = bookService.getBook(book.getId());
            return new ResponseEntity<Book>(_book, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<Book>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // => http://localhost:8080/api/books/remove/5
    @Override
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable("id") @RequestParam(required = true) long id) {
        if (id <= 0) {
            return new ResponseEntity<String>("book id must be greater than 0", HttpStatus.BAD_REQUEST);
        }
        try {
            bookService.deleteBook(id);
            String message = "Book delete with success [id = ".concat(String.valueOf(id)).concat("]");
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception e) {
            String error = "Erreur interne : " + e.getMessage();
            return new ResponseEntity<String>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/all")
    @Override
    public ResponseEntity<List<Book>> getAllBooks() {

        try {
            List<Book> books = bookService.getBooks();
            if (books == null || books.isEmpty()) {
                return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isBookOk(Book book) {
        return book != null
                && book.getIsbn() != null && !book.getIsbn().isBlank()
                && book.getTitle() != null && !book.getTitle().isBlank()
                && book.getDescription() != null && !book.getDescription().isBlank()
                && book.getEditor() != null && !book.getEditor().isBlank()
                && book.getPublishedDate() != null
                && book.getCategory() != null && !book.getCategory().isBlank()
                && book.getLanguage() != null && !book.getLanguage().isBlank()
                && book.getNbPages() > 0;
    }

    @PutMapping("/reserver/{book_id}/{email}")
    public ResponseEntity<?> reserverBook(@PathVariable("book_id") long bookId,
                                          @PathVariable("email") String email) {
        try {
            String msg = bookService.reserveBook(bookId, email);
            return ResponseEntity.ok("{\"message\":\"" + msg + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

}
