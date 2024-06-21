package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Question> getQuestionById(@PathVariable String id) {
        return questionRepository.findById(id);
    }

    @PostMapping
    public Question createQuestion(@RequestBody Question question) {
        return questionRepository.save(question);
    }

    @PutMapping("/{id}")
    public Question updateQuestion(@PathVariable String id, @RequestBody Question questionDetails) {
        Optional<Question> optionalQuestion = questionRepository.findById(id);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            question.setQuestion(questionDetails.getQuestion());
            question.setType(questionDetails.getType());
            question.setTags(questionDetails.getTags());
            question.setMateria(questionDetails.getMateria());
            question.setCurso(questionDetails.getCurso());
            question.setYear(questionDetails.getYear());
            return questionRepository.save(question);
        } else {
            throw new RuntimeException("Question not found with id " + id);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable String id) {
        questionRepository.deleteById(id);
    }
}
