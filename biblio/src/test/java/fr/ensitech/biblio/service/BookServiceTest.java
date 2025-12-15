package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.repository.IBookRepository;
import fr.ensitech.biblio.repository.IReservationRepository;
import fr.ensitech.biblio.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldAddValidBook() throws Exception {
        // Création d'un livre valide
        Book book = new Book();
        book.setTitle("Test");
        book.setIsbn("ISBN123");
        book.setEditor("Editor");
        book.setDescription("Desc");
        book.setCategory("IT");
        book.setLanguage("FR");
        book.setNbPages((short)100);
        book.setPublished(true);
        book.setPublishedDate(new Date());

        bookService.addOrUpdateBook(book);

        verify(bookRepository).save(book);
    }
}
