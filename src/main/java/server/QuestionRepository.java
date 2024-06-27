package server;

import org.springframework.data.mongodb.repository.MongoRepository;
import server.documents.Question;
import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String> {
    List<Question> findAllByOrderByCreatedAtDesc();
}
