package fr.ensitech.biblio.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "security_question")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class SecurityQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String question;
}
