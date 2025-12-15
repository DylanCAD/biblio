    package fr.ensitech.biblio.entity;

    import jakarta.persistence.*;
    import lombok.*;

    import java.util.Date;

    @Entity
    @Table(name = "reservation",
            uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "book_id"})}
    )
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
    public class Reservation {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "book_id", nullable = false)
        private Book book;

        @Column(name = "reservation_date", nullable = false)
        @Temporal(TemporalType.TIMESTAMP)
        private Date reservationDate = new Date();

        @Column(name = "status", nullable = false, length = 10)
        private String status = "ACTIVE";
    }
