package uk.gov.hmcts.reform.coh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/online-hearings/{onlineHearingId}/questions/{questionId}/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private QuestionService questionService;

    public AnswerController() {
    }

    public AnswerController(AnswerService answerService, QuestionService questionService) {
        this.answerService = answerService;
        this.questionService = questionService;
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@PathVariable Long onlineHearingId, @PathVariable Long questionId, @RequestBody AnswerRequest request) {

        ValidationResult validationResult = validate(request);
        if (!validationResult.isValid()) {
            return new ResponseEntity<AnswerResponse>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        AnswerResponse answerResponse = new AnswerResponse();
        try {
            Answer answer = new Answer();
            Question question = questionService.retrieveQuestionById(questionId);

            if (question == null) {
                return new ResponseEntity<AnswerResponse>(HttpStatus.FAILED_DEPENDENCY);
            }

            answer.setAnswerText(request.getAnswer().getAnswer());
            answer.setQuestion(question);
            answer = answerService.createAnswer(answer);
            answerResponse.setAnswerId(answer.getAnswerId());
        } catch (Exception e) {
            System.out.println("Exception in createAnswer: " + e.getMessage());
            return new ResponseEntity<AnswerResponse>(HttpStatus.FAILED_DEPENDENCY);
        }

        return ResponseEntity.ok(answerResponse);
    }

    @GetMapping(value = "{answerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Answer> retrieveAnswer(@PathVariable long answerId) {

        Optional<Answer> answer = answerService.retrieveAnswerById(answerId);
        if (!answer.isPresent()) {
            return new ResponseEntity<Answer>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(answer.get());
    }

    @GetMapping(value = "")
    public ResponseEntity<List<Answer>> retrieveAnswers(@PathVariable Long questionId) {

        // Nothing to return if question doesn't exist
        Question question = questionService.retrieveQuestionById(questionId);
        if (question == null) {
            return new ResponseEntity<List<Answer>>(HttpStatus.FAILED_DEPENDENCY);
        }

        List<Answer> answers = answerService.retrieveAnswersByQuestion(question);

        return ResponseEntity.ok(answers);
    }

    @PatchMapping(value = "{answerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnswerResponse> updateAnswer(@PathVariable Long questionId, @PathVariable long answerId, @RequestBody AnswerRequest request) {

        AnswerResponse answerResponse = new AnswerResponse();
        try {
            Question question = new Question();
            question.setQuestionId(questionId);
            Answer answer = new Answer().answerId(answerId)
                    .answerText(request.getAnswer().getAnswer());
            answer.setQuestion(question);

            Optional<Answer> optionalAnswer = answerService.retrieveAnswerById(answerId);
            if (!optionalAnswer.isPresent()) {
                return new ResponseEntity<AnswerResponse>(HttpStatus.NOT_FOUND);
            }

            answerService.updateAnswerById(answer);
            answerResponse.setAnswerId(answer.getAnswerId());
        } catch (Exception e) {
            return new ResponseEntity<AnswerResponse>(HttpStatus.FAILED_DEPENDENCY);
        }

        return ResponseEntity.ok(answerResponse);
    }

    private ValidationResult validate(AnswerRequest request) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        if (request.getAnswer() == null || StringUtils.isEmpty(request.getAnswer().getAnswer())) {
            result.setValid(false);
            result.setReason("Answer text cannot be empty");
        } else if (request.getAnswerState() == null || StringUtils.isEmpty(request.getAnswerState().getStateName())) {
            result.setValid(false);
            result.setReason("Answer state cannot be empty");
        }

        return result;
    }

    private class ValidationResult {

        private boolean isValid;
        private String reason;

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
