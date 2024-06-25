package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.documents.User;
import server.services.AuthService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "server")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class HelloWorldController {

    @Autowired
    private AuthService authService;

    @GetMapping("/")
    public String hello() {
        System.out.println("DEU CERTO!!!!");
        return "Hello, World!";
    }

    @PostMapping("/chatgpt")
    public ResponseEntity<Object> chatgpt(@RequestBody Map<String, Object> request) {
        try {
            String apiKey = "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";

            if (!request.containsKey("input")) {
                return new ResponseEntity<>(Map.of("error", "Invalid input"), HttpStatus.BAD_REQUEST);
            }

            String userInput = request.get("input").toString();

            // Configurar o corpo da requisição para a API do OpenAI
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-3.5-turbo");
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "Você irá trabalhar como uma API, " +
                            "e o seu retorno deve ser sempre no seguinte formato: {\n" +
                            "  \"question\": \"PERGUNTA_N\",\n" +
                            "  \"type\": \"OBJETIVA/DISSERTATIVA\",\n" +
                            "  \"tags\": [\"TAG_1\", \"TAG_2\", \"TAG3\", ...],\n" +
                            "  \"materia\": \"NOME_DA_MATERIA\",\n" +
                            "  \"curso\": \"NOME_DO_CURSO\",\n" +
                            "  \"year\": \"ANO_LETIVO\"\n" +
                            "}\n"),
                    Map.of("role", "user", "content", userInput)
            ));
            body.put("max_tokens", 500);

            HttpResponse<JsonNode> response = Unirest.post("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(new ObjectMapper().writeValueAsString(body))
                    .asJson();

            if (response.getStatus() != 200) {
                return new ResponseEntity<>(Map.of("error", "Failed to get response from OpenAI"), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String output = response.getBody().getObject()
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .strip();

            return ResponseEntity.ok(Map.of("response", output));
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
}
