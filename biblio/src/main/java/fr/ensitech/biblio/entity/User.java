package fr.ensitech.biblio.entity;

import jakarta.persistence.*;
import lombok.*;

import jakarta.persistence.*;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "users", catalog = "biblio_databasem2")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "firstname", nullable = false, length = 48)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 48)
    private String lastname;

    @Column(name = "email", nullable = false, length = 48, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Column(name = "role", nullable = false, length = 1)
    private String role;

    @Column(name = "birthdate", nullable = true)
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date birthdate;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "status", nullable = false, length = 10)
    private String status = "INACTIVE";

    @Column(name = "activation_token", length = 100, nullable = true)
    private String activationToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_question_id", nullable = true)
    private SecurityQuestion securityQuestion;

    @Column(name = "secret_answer_hash", length = 128, nullable = true)
    private String secretAnswerHash;

    @Column(name = "password_last_updated", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordLastUpdated;


}
