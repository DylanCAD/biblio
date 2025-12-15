package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.User;

import java.util.Date;
import java.util.List;

public interface IUserService {

    void createUser(User user) throws Exception;
    User getUserById(long id) throws Exception;
    List<User> getUsersByBirthdate(Date dateInf, Date dateSup) throws Exception;

    void register(User user) throws Exception;
    void activateUser(String token) throws Exception;
    void login(String email, String password) throws Exception;
    void unsubscribe(String email) throws Exception;

    void updateProfile(long id, User profile) throws Exception;
    void changePassword(long id, String oldPwd, String newPwd) throws Exception;

    void loginStep1(String email, String password) throws Exception;
    boolean verifySecretAnswer(String email, String answer) throws Exception;

    void renewPassword(String email, String oldPwd, String newPwd) throws Exception;

}
