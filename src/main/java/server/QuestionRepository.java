package server;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestionRepository extends MongoRepository<Question, String> {
    // Métodos de consulta personalizados (se necessário)
}
