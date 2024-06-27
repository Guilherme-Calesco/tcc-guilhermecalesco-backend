package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.documents.Question;
import server.documents.User;
import server.services.AuthService;
import server.services.QuestionService;

import java.util.List;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "server")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class GeneralController {

    @Autowired
    private AuthService authService;

    @Autowired
    private QuestionService questionService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        boolean isAuthenticated = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (isAuthenticated) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        User newUser = authService.register(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getName());
        if (newUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User registration failed");
        }
    }

    @PostMapping("/questions")
    public ResponseEntity<List<Question>> createQuestion(@RequestBody Question question) {
        List<Question> createdQuestions = questionService.generateQuestion(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestions);
    }

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }
}
