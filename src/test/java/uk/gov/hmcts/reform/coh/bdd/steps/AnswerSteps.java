package uk.gov.hmcts.reform.coh.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class AnswerSteps {

    private String baseUrl = "http://localhost:8080";

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private ResponseEntity<String> response;

    private AnswerRequest request = new AnswerRequest();

    private String endpoint;

    @Autowired
    private QuestionService questionService;

    @Given("^an existing question with id of (.*)$")
    public void an_existing_question_with_id(Long questionId) {
        /** Call out to question endpoint to create question **/
    }

    @Given("^a valid answer$")
    public void a_valid_answer() {
        answer_text_is("foo");
    }

    @Given("^the answer text is empty$")
    public void answer_text_is_empty() {
        answer_text_is(null);
    }

    @Given("^the answer text is '(.*)'$")
    public void answer_text_is(String text) {
        request.setAnswerText(text);
    }

    @Given("^the endpoint is '(.*)'$")
    public void the_endpoint_is(String endpoint) {
        this.endpoint = endpoint;
    }

    @Given("^an update to the answer is required$")
    public void an_update_to_the_answer_is_required() {
        try {
            AnswerResponse answerResponse = (AnswerResponse) convertToClass(response.getBody().toString());
            this.endpoint = endpoint + "/" + answerResponse.getAnswerId();
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage());
        }
    }

    @When("^a (.*) request is sent$")
    public void send_request(String type) throws IOException {

        String json = convertToJson(request);

        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        if ("GET".equalsIgnoreCase(type)) {
            response = restTemplate.getForEntity(baseUrl + endpoint, String.class);
        } else if ("POST".equalsIgnoreCase(type)) {
            HttpEntity<String> request = new HttpEntity<>(json, header);
            response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
        } else if ("PATCH".equalsIgnoreCase(type)) {
            /**
             * This is a workaround for https://jira.spring.io/browse/SPR-15347
             *
             **/
            HttpEntity<String> request = new HttpEntity<>(json, header);
            response = restTemplate.exchange(baseUrl + endpoint + "?_method=patch", HttpMethod.POST, request, String.class);
        }
    }

    @Then("^the response code is (\\d+)$")
    public void the_response_code_is(int responseCode) throws Throwable {
        assertEquals("Response status code", responseCode, response.getStatusCode().value());
    }

    private String convertToJson(Object obj) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(obj);
    }

    private Object convertToClass(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(json, AnswerResponse.class);
    }
}