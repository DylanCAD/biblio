package fr.ensitech.biblio.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "book", catalog = "biblio_databasem2")
@Setter @Getter @ToString @NoArgsConstructor @AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false,  length = 100)
    private String editor;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    //@DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(pattern="dd/MM/yyyy", timezone="Europe/Zagreb")
    private Date publishedDate;

    @Column(nullable = false, length = 20, unique = true)
    private String isbn;

    @Column(nullable = false)
    private short nbPages;

    @Column(nullable = false)
    private int stock = 1;

    @Column(nullable = false, length = 48)
    private String category;

    @Column(nullable = false, length = 3)
    private String language;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "author_book",
            joinColumns = @JoinColumn(name = "author_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    private Set<Author> authors = new HashSet<Author>();
}
