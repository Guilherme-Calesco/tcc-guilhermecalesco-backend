package server;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import server.documents.Question;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
}
