package uk.gov.hmcts.reform.coh.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.QuestionStateRepository;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@ContextConfiguration
@SpringBootTest
public class QuestionSteps {

    private String baseUrl = "http://localhost:8080";

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private ResponseEntity<String> response;

    private String endpoint;

    private Long questionId;

    private UUID onlineHearingId;

    private Long answerId;

    private Map<String, String> endpoints = new HashMap<String, String>();

    private AnswerRequest answerRequest;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private OnlineHearingService onlineHearingService;

    @Autowired
    private QuestionStateRepository questionStateRepository;

    @Before
    public void setup() {
        endpoints.put("answer", "/online-hearings/1/questions/question_id/answers");
        questionId = null;
        answerId = null;
    }

    @Given("^a valid online hearing$")
    /**
     * Creates an online hearing to be used for testing with a question
     */
    public void an_existing_question_with_id() {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setExternalRef("foo");
        onlineHearing.setExternalRef("123e4567-e89b-12d3-a456-426655440000");

        onlineHearing = onlineHearingService.createOnlineHearing(onlineHearing);
        this.onlineHearingId = onlineHearing.getOnlineHearingId();
    }

    @When("^a (.*) request is sent$")
    public void send_request(String type) throws IOException {

        String json = JsonUtils.getJsonInput("question/create_question");

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


//    @Given("^a standard answer$")
//    public void a_standard_answer() throws IOException {
//        JsonUtils utils = new JsonUtils();
//        this.answerRequest = (AnswerRequest)utils.toObject("answer/standard_answer", AnswerRequest.class);
//    }
//
//    @Given("^a valid answer$")
//    public void a_valid_answer() {
//        answer_text_is("foo");
//    }
//
//    @Given("^the answer text is empty$")
//    public void answer_text_is_empty() {
//        answer_text_is(null);
//    }
//
//    @Given("^the answer text is '(.*)'$")
//    public void answer_text_is(String text) {
//        answerRequest.getAnswer().setAnswer(text);
//    }
//
//    @Given("^an unknown answer identifier$")
//    public void an_unknown_answer_identifier$() throws Throwable {
//        // Write code here that turns the phrase above into concrete actions
//        answerId = 0L;
//    }
//
//    @Given("^the endpoint is for submitting an (.*)$")
//    public void the_endpoint_is_for_submitting_an_answer(String entity) throws Throwable {
//        if (endpoints.containsKey(entity)) {
//            // See if we need to fix the endpoint
//            this.endpoint = endpoints.get(entity);
//            endpoint = endpoint.replaceAll("question_id", questionId == null ? "0" : questionId.toString());
//        }
//
//        if ("answer".equalsIgnoreCase(entity) && answerId != null) {
//            endpoint += "/" + answerId;
//        }
//    }
//
//    @Given("^the endpoint is for submitting all (.*)$")
//    public void the_endpoint_is_for_submitting_all_answer(String entity) throws Throwable {
//        if (endpoints.containsKey(entity)) {
//            // See if we need to fix the endpoint
//            this.endpoint = endpoints.get(entity);
//            endpoint = endpoint.replaceAll("question_id", questionId == null ? "0" : questionId.toString());
//        }
//    }
//
//    @Given("^an update to the answer is required$")
//    public void an_update_to_the_answer_is_required() {
//        try {
//            AnswerResponse answerResponse = (AnswerResponse) convertToClass(response.getBody().toString());
//            this.endpoint = endpoint + "/" + answerResponse.getAnswerId();
//        } catch (Exception e) {
//            System.out.println("Exception " + e.getMessage());
//        }
//    }
//
//    @When("^a (.*) request is sent$")
//    public void send_request(String type) throws IOException {
//
//        String json = convertToJson(answerRequest);
//
//        HttpHeaders header = new HttpHeaders();
//        header.add("Content-Type", "application/json");
//
//        if ("GET".equalsIgnoreCase(type)) {
//            response = restTemplate.getForEntity(baseUrl + endpoint, String.class);
//        } else if ("POST".equalsIgnoreCase(type)) {
//            HttpEntity<String> request = new HttpEntity<>(json, header);
//            response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.POST, request, String.class);
//        } else if ("PATCH".equalsIgnoreCase(type)) {
//            /**
//             * This is a workaround for https://jira.spring.io/browse/SPR-15347
//             *
//             **/
//            HttpEntity<String> request = new HttpEntity<>(json, header);
//            response = restTemplate.exchange(baseUrl + endpoint + "?_method=patch", HttpMethod.POST, request, String.class);
//        }
//    }
//
//    @Then("^the response code is (\\d+)$")
//    public void the_response_code_is(int responseCode) throws Throwable {
//        assertEquals("Response status code", responseCode, response.getStatusCode().value());
//    }
//
//    @Then("^there are (\\d+) answers$")
//    public void there_are_count_answers(int count) throws Throwable {
//        ObjectMapper mapper = new ObjectMapper();
//        String json =response.getBody();
//        Answer[] myObjects = mapper.readValue(json, Answer[].class);
//
//        assertEquals("Response status code", myObjects.length, count);
//    }

    private String convertToJson(Object obj) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(obj);
    }

    private Object convertToClass(String json) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(json, AnswerResponse.class);
    }
}