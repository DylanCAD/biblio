package fr.ensitech.biblio.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "author", catalog = "biblio_databasem2")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 48, nullable = false)
    private String firstname;

    @Column(length = 48, nullable = false)
    private String lastname;

    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<Book>();

}