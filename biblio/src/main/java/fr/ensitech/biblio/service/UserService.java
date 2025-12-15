package fr.ensitech.biblio.service;

import fr.ensitech.biblio.entity.PasswordHistory;
import fr.ensitech.biblio.entity.SecurityQuestion;

import fr.ensitech.biblio.entity.User;

import fr.ensitech.biblio.repository.IPasswordHistoryRepository;
import fr.ensitech.biblio.repository.ISecurityQuestionRepository;

import fr.ensitech.biblio.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


import java.util.Date;
import java.util.List;
import java.util.Optional;

import java.util.UUID;



@Service
public class UserService implements IUserService {

    private static final long PASSWORD_EXPIRE_WEEKS = 12L;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ISecurityQuestionRepository securityQuestionRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private IPasswordHistoryRepository passwordHistoryRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void createUser(User user) throws Exception {
        userRepository.save(user);
    }

    @Override
    public User getUserById(long id) throws Exception {

        Optional<User> optional = userRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public List<User> getUsersByBirthdate(Date dateInf, Date dateSup) throws Exception {

        return userRepository.findByBirthdateBetween(dateInf, dateSup);
    }

    @Override
    public void register(User user) throws Exception {
        if (user == null) throw new Exception("Données utilisateur manquantes");
        if (user.getEmail() == null || user.getEmail().isBlank()) throw new Exception("Email requis");
        String email = user.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email) != null) {
            throw new Exception("Email déjà utilisé");
        }

        if (user.getSecretAnswerHash() == null || user.getSecretAnswerHash().isBlank()) {
            throw new Exception("Réponse secrète obligatoire");
        }
        String plainAnswer = user.getSecretAnswerHash().trim();
        if (plainAnswer.length() > 32) {
            throw new Exception("La réponse secrète doit faire au maximum 32 caractères");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new Exception("Mot de passe requis");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        user.setPasswordLastUpdated(new Date());

        user.setSecretAnswerHash(passwordEncoder.encode(plainAnswer));

        if (user.getSecurityQuestion() != null && user.getSecurityQuestion().getId() != null) {
            Long qid = user.getSecurityQuestion().getId();
            Optional<SecurityQuestion> q = securityQuestionRepository.findById(qid);
            if (q.isEmpty()) throw new Exception("Question de sécurité invalide");
            user.setSecurityQuestion(q.get());
        } else {
            throw new Exception("Question de sécurité obligatoire");
        }

        String token = UUID.randomUUID().toString();
        user.setActivationToken(token);
        user.setStatus("INACTIVE");
        user.setActive(false);

        userRepository.save(user);

        try {
            String activationLink = "http://localhost:8080/api/users/activate?token=" + token;
            sendEmail(user.getEmail(), "Activation de votre compte",
                    "Merci de vous être inscrit. Cliquez sur ce lien pour activer votre compte : " + activationLink);
        } catch (Exception e) {
                System.err.println("Erreur envoi email d'activation: " + e.getMessage());
        }
    }

    @Override
    public void activateUser(String token) throws Exception {
        User user = userRepository.findByActivationToken(token);

        if (user == null)
            throw new Exception("Token invalide");

        user.setStatus("ACTIVE");
        user.setActive(true);

        user.setActivationToken(null);
        userRepository.save(user);

        sendEmail(user.getEmail(), "Compte activé", "Votre compte est maintenant actif.");
    }

    @Override
    public void login(String email, String password) throws Exception {
        if (email == null || password == null) throw new Exception("Email et mot de passe requis");

        User user = userRepository.findByEmail(email);

        if (user == null)
            throw new Exception("Identifiants invalides");

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new Exception("Identifiants invalides");

        if (!"ACTIVE".equals(user.getStatus()))
            throw new Exception("Votre compte n'est pas activé");
    }

    @Override
    public void unsubscribe(String email) throws Exception {
        User user = userRepository.findByEmail(email);

        if (user == null)
            throw new Exception("Utilisateur introuvable");

        userRepository.delete(user);

        sendEmail(email, "Désinscription confirmée",
                "Votre compte a bien été supprimé.");
    }

    @Override
    public void updateProfile(long id, User profile) throws Exception {
        Optional<User> optional = userRepository.findById(id);
        User user = optional.orElseThrow(() -> new Exception("Utilisateur non trouvé"));

        user.setFirstname(profile.getFirstname());
        user.setLastname(profile.getLastname());
        user.setBirthdate(profile.getBirthdate());

        userRepository.save(user);
    }

    @Override
    public void changePassword(long id, String oldPwd, String newPwd) throws Exception {
        Optional<User> optional = userRepository.findById(id);
        User user = optional.orElseThrow(() -> new Exception("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new Exception("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPwd));
        userRepository.save(user);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Erreur envoi email: " + e.getMessage());
        }
    }

    @Override
    public void loginStep1(String email, String password) throws Exception {
        if (email == null || password == null) throw new Exception("Email et mot de passe requis");

        User user = userRepository.findByEmail(email);
        if (user == null) throw new Exception("Identifiants invalides");
        if (!passwordEncoder.matches(password, user.getPassword())) throw new Exception("Identifiants invalides");
        if (!"ACTIVE".equals(user.getStatus())) throw new Exception("Votre compte n'est pas activé");

        if (user.getPasswordLastUpdated() != null) {
            Instant updated = user.getPasswordLastUpdated().toInstant();
            Instant limit = Instant.now().minus(PASSWORD_EXPIRE_WEEKS * 7, ChronoUnit.DAYS);
            if (updated.isBefore(limit)) {
                throw new Exception("Mot de passe expiré. Vous devez le renouveler via /api/users/" + user.getEmail() + "/password/renew");
            }
        } else {
            throw new Exception("Mot de passe non initialisé. Veuillez le renouveler.");
        }
    }

    @Override
    public boolean verifySecretAnswer(String email, String answer) throws Exception {
        if (email == null || answer == null) throw new Exception("Email et réponse requis");

        User user = userRepository.findByEmail(email);
        if (user == null) throw new Exception("Utilisateur introuvable");

        if (user.getSecretAnswerHash() == null) throw new Exception("Aucune réponse secrète définie");

        return passwordEncoder.matches(answer, user.getSecretAnswerHash());
    }

    @Override
    public void renewPassword(String email, String oldPwd, String newPwd) throws Exception {
        if (email == null || oldPwd == null || newPwd == null) throw new Exception("Email, ancien et nouveau mot de passe requis");

        User user = userRepository.findByEmail(email);
        if (user == null) throw new Exception("Utilisateur introuvable");

        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new Exception("Ancien mot de passe incorrect");
        }

        List<PasswordHistory> lastFive = passwordHistoryRepository.findTop5ByUserOrderByCreatedAtDesc(user);
        for (PasswordHistory ph : lastFive) {
            if (passwordEncoder.matches(newPwd, ph.getPasswordHash())) {
                throw new Exception("Nouveau mot de passe interdit : il figure parmi les 5 derniers mots de passe utilisés");
            }
        }
        if (passwordEncoder.matches(newPwd, user.getPassword())) {
            throw new Exception("Nouveau mot de passe interdit : identique au mot de passe actuel");
        }

        PasswordHistory hist = new PasswordHistory();
        hist.setUser(user);
        hist.setPasswordHash(user.getPassword());
        hist.setCreatedAt(new Date());
        passwordHistoryRepository.save(hist);

        List<PasswordHistory> all = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        if (all.size() > 5) {
            for (int i = 5; i < all.size(); i++) {
                passwordHistoryRepository.delete(all.get(i));
            }
        }

        user.setPassword(passwordEncoder.encode(newPwd));
        user.setPasswordLastUpdated(new Date());
        userRepository.save(user);

    }

}