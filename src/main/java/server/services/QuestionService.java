package server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.QuestionRepository;
import server.documents.Question;

import java.util.*;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    private static final String API_KEY = "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";
    private static final int MAX_ATTEMPTS = 3;
    private static final int TIMEOUT_MS = 30000; // 30 segundos

    public List<Question> generateQuestion(Question question) {
        try {
            String prompt = generatePrompt(question);
            System.out.println("Generated Prompt: " + prompt);
            String apiResponse = callOpenAiApi(prompt);
            System.out.println("API Response: " + apiResponse);
            List<Question> questions = processApiResponse(apiResponse);
            return saveQuestions(questions);
        } catch (Exception e) {
            System.err.println("Error generating questions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAllByOrderByCreatedAtDesc();
    }

    private String generatePrompt(Question question) {
        return String.format(
                "Crie %d perguntas objetivas e %d perguntas dissertativas para uma prova de %s com os assuntos [%s] para o curso %s do ano letivo %s.",
                question.getQuestionObjet(),
                question.getQuestionDissert(),
                question.getMateria(),
                question.getTopic(),
                question.getCurso(),
                question.getYear()
        );
    }

    private String callOpenAiApi(String prompt) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4-turbo-2024-04-09");
        body.put("messages", List.of(
                Map.of("role", "system", "content", "Você irá trabalhar como uma API de gerar questões para provas. " +
                        "As questões devem ser pertinentes ao curso e ao ano letivo que a prova será aplicada " +
                        "e sempre ter os tópicos como assunto da questão, ainda dentro da matéria, curso e ano letivo"+
                        "O seu retorno deve ser sempre no formato a seguir, importante, retorne apenas o JSONArray, nada mais: [{\n" +
                        "  \"question\": \"PERGUNTA_N\",\n" +
                        "  \"type\": \"OBJETIVA/DISSERTATIVA\",\n" +
                        "  \"tags\": [\"TAG_1\", \"TAG_2\", \"TAG3\", ...],\n" +
                        "  \"materia\": \"NOME_DA_MATERIA\",\n" +
                        "  \"curso\": \"NOME_DO_CURSO\",\n" +
                        "  \"year\": \"ANO_LETIVO\"\n" +
                        "  \"options\": [\"OPT1\", \"OPT2\", \"OPT3\"] //quando objetiva\n" +
                        "}]\n"),
                Map.of("role", "user", "content", prompt)
        ));
        body.put("max_tokens", 1000);

        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_ATTEMPTS) {
            try {
                HttpResponse<JsonNode> response = Unirest.post("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .body(new ObjectMapper().writeValueAsString(body))
                        .socketTimeout(TIMEOUT_MS)
                        .connectTimeout(TIMEOUT_MS)
                        .asJson();

                if (response.getStatus() != 200) {
                    throw new RuntimeException("Failed to get response from OpenAI: " + response.getStatus() + " " + response.getStatusText());
                }

                JsonNode responseBody = response.getBody();
                if (responseBody == null || !responseBody.getObject().has("choices")) {
                    throw new RuntimeException("Invalid response format from OpenAI");
                }

                return responseBody.getObject()
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .strip();
            } catch (UnirestException e) {
                lastException = e;
                System.err.println("Attempt " + (attempts + 1) + " failed: " + e.getMessage());
                attempts++;
                Thread.sleep(2000); // Esperar 2 segundos antes de tentar novamente
            }
        }

        throw lastException;
    }

    private List<Question> processApiResponse(String apiResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> questionMaps = objectMapper.readValue(apiResponse, List.class);

        List<Question> questions = new ArrayList<>();
        for (Map<String, Object> q : questionMaps) {
            Question newQuestion = new Question();
            newQuestion.setQuestion((String) q.get("question"));
            newQuestion.setType((String) q.get("type"));
            newQuestion.setTags((List<String>) q.get("tags"));
            newQuestion.setMateria((String) q.get("materia"));
            newQuestion.setCurso((String) q.get("curso"));
            newQuestion.setYear((String) q.get("year"));
            newQuestion.setTopic(String.join(", ", (List<String>) q.get("tags")));
            newQuestion.setOptions(Objects.nonNull(q.get("options"))?(List<String>) q.get("options"):null);
            newQuestion.setCreatedAt(new Date(System.currentTimeMillis()));
            System.out.println("Processed Question: " + newQuestion);

            questions.add(newQuestion);
        }
        return questions;
    }

    private List<Question> saveQuestions(List<Question> questions) {
        List<Question> savedQuestions = new ArrayList<>();
        for (Question question : questions) {
            savedQuestions.add(questionRepository.save(question));
        }
        return savedQuestions;
    }
}
