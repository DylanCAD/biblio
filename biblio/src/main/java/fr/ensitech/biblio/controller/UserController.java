package fr.ensitech.biblio.controller;

import fr.ensitech.biblio.entity.SecurityQuestion;
import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.repository.ISecurityQuestionRepository;
import fr.ensitech.biblio.repository.IUserRepository;
import fr.ensitech.biblio.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8080")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ISecurityQuestionRepository securityQuestionRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        try {
            User user = new User();
            user.setFirstname((String) body.get("firstname"));
            user.setLastname((String) body.get("lastname"));
            user.setEmail((String) body.get("email"));
            user.setPassword((String) body.get("password"));
            user.setRole((String) body.get("role"));

            Integer qid = (Integer) body.get("securityQuestionId");
            String secretAnswer = (String) body.get("secretAnswer");

            if (qid == null || secretAnswer == null) {
                throw new Exception("Question de sécurité et réponse obligatoires");
            }
            SecurityQuestion q = securityQuestionRepository.findById(qid.longValue())
                    .orElseThrow(() -> new Exception("Question de sécurité invalide"));
            user.setSecurityQuestion(q);

            user.setSecretAnswerHash(secretAnswer);

            userService.register(user);

            return ResponseEntity.ok("{\"message\":\"Utilisateur créé. Vérifier votre email pour activer votre compte.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String token) {
        try {
            userService.activateUser(token);
            return ResponseEntity.ok("{\"message\":\"Compte activé avec succès.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login-step1")
    public ResponseEntity<?> loginStep1(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");
            userService.loginStep1(email, password);

            User user = userRepository.findByEmail(email);
            if (user.getSecurityQuestion() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"Aucune question de sécurité associée.\"}");
            }
            return ResponseEntity.ok("{\"question\":\"" + user.getSecurityQuestion().getQuestion() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login-verify")
    public ResponseEntity<?> loginVerify(@RequestBody Map<String, String> data) {
        try {
            String email = data.get("email");
            String answer = data.get("answer");
            boolean ok = userService.verifySecretAnswer(email, answer);
            if (ok) {
                return ResponseEntity.ok("{\"message\":\"Authentification 2FA réussie.\"}");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Réponse secrète incorrecte.\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestParam String email) {
        try {
            userService.unsubscribe(email);
            return ResponseEntity.ok("{\"message\":\"Compte supprimé avec succès.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable long id, @RequestBody User profile) {
        try {
            userService.updateProfile(id, profile);
            return ResponseEntity.ok("{\"message\":\"Profil mis à jour\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}/{oldPwd}/{newPwd}")
    public ResponseEntity<?> changePassword(
            @PathVariable long id,
            @PathVariable String oldPwd,
            @PathVariable String newPwd) {
        try {
            userService.changePassword(id, oldPwd, newPwd);
            return ResponseEntity.ok("{\"message\":\"Mot de passe mis à jour\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{email}/password/renew")
    public ResponseEntity<?> renewPassword(@PathVariable String email, @RequestBody Map<String, String> body) {
        try {
            String oldPwd = body.get("oldPassword");
            String newPwd = body.get("newPassword");
            if (oldPwd == null || newPwd == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"oldPassword et newPassword requis\"}");
            }
            userService.renewPassword(email, oldPwd, newPwd);
            return ResponseEntity.ok("{\"message\":\"Mot de passe renouvelé avec succès\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

}
