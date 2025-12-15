package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Author;
import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.entity.Reservation;
import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.repository.IBookRepository;
import fr.ensitech.biblio.repository.IReservationRepository;
import fr.ensitech.biblio.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookService implements IBookService {

    @Autowired
    private IBookRepository bookRepository;

    @Override
    public void addOrUpdateBook(Book book) throws Exception {
        if (book.getId() < 0) {
            throw new Exception("Book id must be greater than 0 !");
        }
        if (book.getId() ==0) {
            bookRepository.save(book);
        } else {
            Book _book = bookRepository.findById(book.getId()).get();
            if (_book == null) {
                throw new Exception("Book not found");
            }
            _book.setIsbn(book.getIsbn());
            _book.setTitle(book.getTitle());
            _book.setDescription(book.getDescription());
            _book.setEditor(book.getEditor());
            _book.setPublishedDate(book.getPublishedDate());
            _book.setCategory(book.getCategory());
            _book.setLanguage(book.getLanguage());
            _book.setNbPages(book.getNbPages());
            _book.setPublished(book.isPublished());
            bookRepository.save(_book);
        }
    }

    @Override
    public void deleteBook(long id) throws Exception {

    }

    @Override
    public List<Book> getBooks() throws Exception {
        return bookRepository.findAll();
    }

    @Override
    public Book getBook(long id) throws Exception {
        Optional<Book> optional = bookRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public List<Book> getBooksByTitle(String title) throws Exception {
        return bookRepository.findByTitleIgnoreCase(title);
    }

    @Override
    public List<Book> getBooksByAuthor(Author author) throws Exception {
        return null;
    }

    @Override
    public List<Book> getBooksBetweenYears(int startYear, int endYear) throws Exception {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.YEAR, startYear);
        startCalendar.set(Calendar.MONTH, Calendar.JANUARY);
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = startCalendar.getTime();

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.YEAR, endYear);
        endCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        endCalendar.set(Calendar.DAY_OF_MONTH, 31);
        Date endDate = endCalendar.getTime();

        return bookRepository.findByPublishedDateBetween(startDate, endDate);
    }

    @Override
    public List<Book> getBooksByPublished(boolean published) {
        return bookRepository.findByPublished(published);
    }

    @Override
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbnIgnoreCase(isbn);
    }

    @Override
    public List<Book> getBooksByTitleOrDescription(String title, String description) {
        return bookRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(title, description);
    }

    @Autowired
    private IReservationRepository reservationRepository;

    @Autowired
    private IUserRepository userRepository;

    @Override
    public String reserveBook(long bookId, String email) throws Exception {
        Optional<Book> optBook = bookRepository.findById(bookId);
        if (optBook.isEmpty()) {
            throw new Exception("Livre introuvable");
        }
        Book book = optBook.get();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new Exception("Utilisateur introuvable");
        }

        Reservation existing = reservationRepository.findByUserAndBookAndStatus(user, book, "ACTIVE");
        if (existing != null) {
            throw new Exception("Vous avez déjà réservé ce livre");
        }

        long userReservations = reservationRepository.countByUserAndStatus(user, "ACTIVE");
        if (userReservations >= 3) {
            throw new Exception("Vous avez atteint la limite de 3 réservations actives");
        }

        long reservedCount = reservationRepository.countByBookAndStatus(book, "ACTIVE");
        if (reservedCount >= book.getStock()) {
            throw new Exception("Plus de copies disponibles à la réservation");
        }

        Reservation r = new Reservation();
        r.setUser(user);
        r.setBook(book);
        r.setReservationDate(new Date());
        r.setStatus("ACTIVE");
        reservationRepository.save(r);

        return "Réservation effectuée avec succès";
    }
}
