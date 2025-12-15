package fr.ensitech.biblio.repository;

import fr.ensitech.biblio.entity.Book;
import fr.ensitech.biblio.entity.Reservation;
import fr.ensitech.biblio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReservationRepository extends JpaRepository<Reservation, Long> {

    long countByUserAndStatus(User user, String status);

    Reservation findByUserAndBookAndStatus(User user, Book book, String status);

    long countByBookAndStatus(Book book, String status);

    List<Reservation> findByUserAndStatus(User user, String status);
}
