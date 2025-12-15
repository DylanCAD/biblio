package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Author;

import java.util.List;

public interface IAuthorService {

    List<Author> getAuthors(String firstName);
    List<Author> getAuthors(String firstName, String lastName);
}
