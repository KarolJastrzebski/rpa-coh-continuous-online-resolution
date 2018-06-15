package uk.gov.hmcts.reform.coh.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.QuestionService;

@RestController
@RequestMapping("/online-hearings/{oh_id}")
public class QuestionController {

    private QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }


    @ApiOperation("Get a question")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success", response = Question.class)})
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<Question> getQuestion(@PathVariable Integer oh_id, @PathVariable Integer questionId) {
        return ResponseEntity.ok(questionService.retrieveQuestionById(questionId));
    }

    @ApiOperation("Add a new question")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Question.class)})
    @PostMapping(value = "/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Question> createQuestion(@PathVariable Integer oh_id, @RequestBody Question body) {
        return ResponseEntity.ok(questionService.createQuestion(oh_id, body));
    }

    @ApiOperation("Edit a question")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success", response = Question.class)})
    @PatchMapping(value = "/questions/{questionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Question> editQuestion(@PathVariable Integer questionId, @RequestBody Question body) {
        return ResponseEntity.ok(questionService.editQuestion(questionId, body));
    }

}
