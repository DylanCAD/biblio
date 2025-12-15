package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.Author;
import fr.ensitech.biblio.repository.IAuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService implements IAuthorService {

    @Autowired
    private IAuthorRepository authorRepository;

    @Override
    public List<Author> getAuthors(String firstName) {
        return authorRepository.findByFirstnameIgnoreCase(firstName);
    }

    @Override
    public List<Author> getAuthors(String firstName, String lastName) {
        return authorRepository.findAuthors(firstName, lastName);
    }
}
