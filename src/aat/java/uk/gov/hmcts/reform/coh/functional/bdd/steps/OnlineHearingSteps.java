package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponse;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.*;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.RelistingHistory;
import uk.gov.hmcts.reform.coh.domain.RelistingState;
import uk.gov.hmcts.reform.coh.functional.bdd.requests.CohEntityTypes;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;
import uk.gov.hmcts.reform.coh.idam.IdamAuthentication;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static junit.framework.TestCase.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class OnlineHearingSteps extends BaseSteps {

    @Autowired
    public OnlineHearingSteps(TestContext testContext, IdamAuthentication idamAuthentication) {
        super(testContext, idamAuthentication);
    }

    @Before
    public void setup() throws Exception {
        super.setup();
    }

    @Given("^a standard online hearing$")
    public void a_standard_online_hearing() throws IOException {
        OnlineHearingRequest onlineHearingRequest = JsonUtils
            .toObjectFromTestName("online_hearing/standard_online_hearing", OnlineHearingRequest.class);
        testContext.getScenarioContext().setCurrentOnlineHearingRequest(onlineHearingRequest);
    }

    @Given("^the case id is '(.*)'")
    public void the_case_id_is(String caseId) {
        testContext.getScenarioContext().getCurrentOnlineHearingRequest().setCaseId(caseId);
    }

    @When("^a (.*) request is sent for the saved online hearing$")
    public void a_get_request_is_sent_to(String method) throws Throwable {
        send_request_online_hearing(method);
    }

    @When("^a get request is sent to ' \"([^\"]*)\"' for the online hearing$")
    public void a_filter_get_request_is_sent_to(String endpoint) throws Throwable {
        HttpEntity<String> request = new HttpEntity<>("", header);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + endpoint, HttpMethod.GET, request, String.class);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
    }

    @When("^a (.*) request is sent for online hearing$")
    public void send_request_online_hearing(String method) throws Exception {

        try {
            ResponseEntity responseEntity = sendRequest(CohEntityTypes.ONLINE_HEARING.getString(), method, getOnlineHearingRequest(method));
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(responseEntity);

            if ("POST".equals(method)) {
                CreateOnlineHearingResponse createOnlineHearingResponse = JsonUtils.toObjectFromJson(responseEntity.getBody().toString(), CreateOnlineHearingResponse.class);
                testContext.getScenarioContext().setCurrentOnlineHearing(createOnlineHearingFromResponse(createOnlineHearingResponse));
                testContext.getScenarioContext().addCaseId(testContext.getScenarioContext().getCurrentOnlineHearingRequest().getCaseId());
            }

        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    @When("^a (.*) request is sent for a conversation")
    public void send_request(String method) {

        try {
            ResponseEntity<String> response = sendRequest(CohEntityTypes.CONVERSATIONS, method, "");
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(response);
        } catch (HttpClientErrorException hcee) {
            testContext.getHttpContext().setResponseBodyAndStatesForResponse(hcee);
        }
    }

    public String getPutRequest() throws Exception {
        return JsonUtils.toJson(testContext.getScenarioContext().getUpdateOnlineHearingRequest());
    }

    @And("^the request contains a random UUID$")
    public void the_request_contains_a_random_UUID() throws Exception {
        testContext.getScenarioContext().getCurrentOnlineHearing().setOnlineHearingId(UUID.randomUUID());
    }

    @And("^the relist reason is '(.*)'$")
    public void theRelistReasonIs(String reason) throws Exception {
        testContext.getScenarioContext().getUpdateOnlineHearingRequest().setReason(reason);
    }

    @Then("^the response contains (\\d) online hearings$")
    public void the_response_contains_no_online_hearings(int count) throws IOException {
        OnlineHearingsResponse response = JsonUtils
            .toObjectFromJson(testContext.getHttpContext().getRawResponseString(), OnlineHearingsResponse.class);
        assertEquals(count, response.getOnlineHearingResponses().size());
    }

    @Then("^the response contains online hearing with case '(.*)'$")
    public void the_response_contains_online_hearing_with_case(String caseId) throws IOException {
        OnlineHearingsResponse response = JsonUtils
            .toObjectFromJson(testContext.getHttpContext().getRawResponseString(), OnlineHearingsResponse.class);
        assertTrue(response.getOnlineHearingResponses().stream().anyMatch(o -> caseId.equalsIgnoreCase(o.getCaseId())));
    }

    @Then("^the online hearing state is '(.*)'$")
    public void the_online_hearing_state_is(String state) throws Exception {
        assertEquals(state, getOnlineHearingResponse().getCurrentState().getName());
    }

    @And("^the conversation response contains an online hearing$")
    public void theResponseContainsAnOnlineHearing() throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNotNull(response.getOnlineHearing());
    }

    @And("^the conversation response contains an online hearing with state desc of '(.*)'$")
    public void theConversationResponseContainsAnOnlineHearingWithStateDesc(String desc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedOnlineHearingUri(response.getOnlineHearing().getOnlineHearingId());
        assertEquals(desc, getConversationResponse().getOnlineHearing().getCurrentState().getStateDesc());
    }

    @And("^the conversation response contains an online hearing with the correct uri$")
    public void theConversationResponseContainsAnOnlineHearingWithAUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedOnlineHearingUri(response.getOnlineHearing().getOnlineHearingId());
        assertTrue(getConversationResponse().getOnlineHearing().getUri().equals(uri));
    }

    @And("^the conversation response contains an online hearing with (\\d) history entries$")
    public void theResponseContainsAnOnlineHearingWithHistory(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertEquals(count, response.getOnlineHearing().getHistories().size());
    }

    @And("^the conversation response contains an online hearing with at least (\\d) history entries$")
    public void theResponseContainsAnOnlineHearingWithAtLeastHistory(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertTrue(count <= response.getOnlineHearing().getHistories().size());
    }

    @And("^the conversation response contains an online hearing with 1 history entry  with state desc of '(.*)'$")
    public void theResponseContainsAnOnlineHearingWithHistory(String stateName) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertTrue(response.getOnlineHearing().getHistories().stream().anyMatch(h -> h.getStateDesc().equals(stateName)));
    }

    @And("^the conversation response contains a decision$")
    public void theResponseContainsADecision() throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNotNull(response.getOnlineHearing().getDecisionResponse());
    }

    @And("^the conversation response contains no decision$")
    public void theResponseContainsNoDecision() throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNull(response.getOnlineHearing().getDecisionResponse());
    }

    @And("^the conversation response contains a decision with the correct uri$")
    public void theConversationResponseContainsADecisionWithAUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedDecisionUri(response.getOnlineHearing().getOnlineHearingId());
        assertEquals(uri, getConversationResponse().getOnlineHearing().getDecisionResponse().getUri());
    }

    @And("^the conversation response contains a decision with state desc of '(.*)'$")
    public void theConversationResponseContainsADecisionWithStateDesc(String stateDesc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedDecisionUri(response.getOnlineHearing().getOnlineHearingId());
        assertEquals(stateDesc, getConversationResponse().getOnlineHearing().getDecisionResponse().getDecisionState().getStateDesc());
    }

    @And("^the conversation response contains a decision with 1 history entry with state desc of '(.*)'$")
    public void theResponseContainsADecisionWithHistory(String stateDesc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertTrue(response.getOnlineHearing().getDecisionResponse().getHistories().stream().anyMatch(h -> h.getStateDesc().equals(stateDesc)));
    }

    @And("^the conversation response contains a decision with (\\d) history entries$")
    public void theResponseContainsADecisionWithHistory(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertEquals(count, response.getOnlineHearing().getDecisionResponse().getHistories().size());
    }

    @And("^the conversation response contains (\\d+) decision replies$")
    public void theConversationResponseContainsDecisionReplies(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertEquals(count, response.getOnlineHearing().getDecisionResponse().getDecisionReplyResponses().size());
    }

    @And("^the conversation response contains a decision reply with the correct uri$")
    public void theConversationResponseContainsADecisionReplyWithTheCorrectUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedDecisionReplyUri(response.getOnlineHearing().getOnlineHearingId(),
            UUID.fromString(getDecisionReplyFromConversationResponse(0).getDecisionReplyId())
        );
        assertEquals(uri, getDecisionReplyFromConversationResponse(0).getUri());
    }

    @And("^the conversation response contains (\\d) question$")
    public void theResponseContainsAQuestion(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNotNull(response.getOnlineHearing().getQuestions());
        assertEquals(count, response.getOnlineHearing().getQuestions().size());
    }

    @And("^the conversation response contains a question with the correct uri$")
    public void theConversationResponseContainsAQuestionWithAUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedQuestionUri(response.getOnlineHearing().getOnlineHearingId(),
            UUID.fromString(getQuestionFromConversationResponse(0).getQuestionId()));
        assertEquals(uri, getConversationResponse().getOnlineHearing().getQuestions().get(0).getUri());
    }

    @And("^the conversation response contains a question with state desc of '(.*)'$")
    public void theConversationResponseContainsAQuestionWithAStateDesc(String stateDesc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedQuestionUri(response.getOnlineHearing().getOnlineHearingId(), UUID.fromString(getQuestionFromConversationResponse(0).getQuestionId()));
        assertEquals(stateDesc, getConversationResponse().getOnlineHearing().getQuestions().get(0).getCurrentState().getStateDesc());
    }

    @And("^the conversation response contains a question with 1 history entry with state desc of '(.*)'$")
    public void theResponseContainsAQuestionWithHistory(String stateName) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertTrue(getQuestionFromConversationResponse(0).getHistories().stream().anyMatch(h -> h.getStateDesc().equals(stateName)));
    }

    @And("^the conversation response contains a question with (\\d) history entries$")
    public void theResponseContainsAQuestionWithHistory(int count) throws Throwable {
        assertEquals(count, getQuestionFromConversationResponse(0).getHistories().size());
    }

    @And("^the conversation response contains a question with at least (\\d) history entries$")
    public void theResponseContainsAQuestionWithAtLeastHistory(int count) throws Throwable {
        assertTrue(count <= getQuestionFromConversationResponse(0).getHistories().size());
    }

    @And("^the conversation response contains (\\d) answer$")
    public void theResponseContainsAnAnswer(int count) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertNotNull(response.getOnlineHearing().getQuestions());
        assertEquals(count, response.getOnlineHearing().getQuestions().get(0).getAnswers().size());
    }

    @And("^the conversation response contains an answer with the correct uri$")
    public void theConversationResponseContainsAnAnswerWithAUri() throws Throwable {
        ConversationResponse response = getConversationResponse();
        String uri = getExpectedAnswerUri(response.getOnlineHearing().getOnlineHearingId(),
            UUID.fromString(getQuestionFromConversationResponse(0).getQuestionId()),
            UUID.fromString(getAnswerFromConversationResponse(0).getAnswerId())
        );
        assertEquals(uri, getAnswerFromConversationResponse(0).getUri());
    }

    @And("^the conversation response contains an answer with state desc of '(.*)'$")
    public void theConversationResponseContainsAnAnswerWithAStateDesc(String stateDesc) throws Throwable {
        ConversationResponse response = getConversationResponse();
        assertEquals(stateDesc, getAnswerFromConversationResponse(0).getStateResponse().getStateDesc());
    }

    @And("^the conversation response contains an answer with (\\d) history entries$")
    public void theResponseContainsAnAnswerWithHistory(int count) throws Throwable {
        assertEquals(count, getQuestionFromConversationResponse(0).getAnswers().get(0).getHistories().size());
    }

    @And("^the conversation response contains an answer with 1 history entry with state desc of '(.*)'$")
    public void theResponseContainsAnAnswerWithHistoryStateDesc(String stateDesc) throws Throwable {
        assertEquals(stateDesc, getQuestionFromConversationResponse(0).getAnswers().get(0).getHistories().get(0).getStateDesc());
    }

    @And("^the online hearing end date is not null$")
    public void theOnlineHearingExpiryDateIsNotNull() throws Throwable {
        assertNotNull(getOnlineHearingResponse().getEndDate());
    }

    @And("^the online hearing reason is '(.*)'$")
    public void theOnlineHearingReasonIsReason(String reason) throws Throwable {
        assertEquals(reason, getOnlineHearingResponse().getRelisting().getReason());
    }

    @When("^(drafting|issuing) the relist(?: with reason '(.*)')*$")
    public void settingStateOfTheRelistTo(String action, String reason) throws Throwable {
        RelistingState state = RelistingState.DRAFTED;
        if ("issuing".equals(action)) {
            state = RelistingState.ISSUED;
        }
        RelistingRequest relistingRequest = new RelistingRequest(reason, state);
        UUID onlineHearingId = testContext.getScenarioContext().getCurrentOnlineHearing().getOnlineHearingId();
        String path = CohUriBuilder.buildRelistingGet(onlineHearingId);
        sendRequest(baseUrl + path, HttpMethod.PUT, JsonUtils.toJson(relistingRequest));

        refreshOnlineHearing();
    }

    @Given("^the relist reason is set to '(.*)'$")
    public void theRelistReasonIsSetTo(String reason) throws Throwable {
        testContext.getScenarioContext().getCurrentOnlineHearing().setRelistReason(reason);
    }

    @Then("^the relist state should be '(.+)'$")
    public void theRelistStateShouldBe(String expected) throws Throwable {
        RelistingState expectedState = RelistingState.valueOf(expected.toUpperCase());
        RelistingState actualState = testContext.getScenarioContext().getCurrentOnlineHearing().getRelistState();

        assertEquals(expectedState, actualState);
    }

    private void refreshOnlineHearing() throws IOException {
        UUID onlineHearingId = testContext.getScenarioContext().getCurrentOnlineHearing().getOnlineHearingId();
        String path = baseUrl + getExpectedOnlineHearingUri(onlineHearingId);
        ResponseEntity<String> responseEntity = sendRequest(path, HttpMethod.GET);
        testContext.getHttpContext().setResponseBodyAndStatesForResponse(responseEntity);

        OnlineHearing onlineHearing = new OnlineHearing();

        OnlineHearingResponse entity
            = JsonUtils.toObjectFromJson(responseEntity.getBody(), OnlineHearingResponse.class);

        Optional.ofNullable(entity).ifPresent(response -> {
            onlineHearing.setOnlineHearingId(response.getOnlineHearingId());
            onlineHearing.setCaseId(response.getCaseId());
            onlineHearing.setRelistReason(response.getRelisting().getReason());
            onlineHearing.setRelistState(response.getRelisting().getState());
            OnlineHearingState onlineHearingState = new OnlineHearingState();
            onlineHearingState.setState(response.getCurrentState().getName());
            onlineHearing.setOnlineHearingState(onlineHearingState);
            List<RelistingHistory> collect = response.getRelistingHistory().stream()
                .map(relistingHistoryResponse -> {
                    return new RelistingHistory(
                        onlineHearing,
                        relistingHistoryResponse.getReason(),
                        relistingHistoryResponse.getState(),
                        relistingHistoryResponse.getDateOccurred()
                    );
                })
                .collect(Collectors.toList());
            onlineHearing.setRelistingHistories(new HashSet<>(collect));
        });

        testContext.getScenarioContext().setCurrentOnlineHearing(onlineHearing);
    }

    @And("^the online hearing state is refreshed$")
    public void theOnlineHearingStateIsRefreshed() throws Throwable {
        refreshOnlineHearing();
    }

    @Then("^the online hearing relist history has (\\d+) entries$")
    public void theOnlineHearingRelistHistoryHasEntries(int expectedNumberOfEntries) throws Throwable {
        assertThat(
            testContext.getScenarioContext().getCurrentOnlineHearing().getRelistingHistories(),
            hasSize(expectedNumberOfEntries)
        );
    }

    private ConversationResponse getConversationResponse() throws IOException {
        return JsonUtils
            .toObjectFromJson(testContext.getHttpContext().getRawResponseString(), ConversationResponse.class);
    }

    private QuestionResponse getQuestionFromConversationResponse(int index) throws IOException {
        ConversationResponse response = getConversationResponse();

        return response.getOnlineHearing().getQuestions().get(index);
    }

    private AnswerResponse getAnswerFromConversationResponse(int index) throws IOException {
        ConversationResponse response = getConversationResponse();

        return response.getOnlineHearing().getQuestions().get(index).getAnswers().get(index);
    }

    private DecisionReplyResponse getDecisionReplyFromConversationResponse(int index) throws IOException {
        ConversationResponse response = getConversationResponse();

        return response.getOnlineHearing().getDecisionResponse().getDecisionReplyResponses().get(index);
    }

    private String getExpectedOnlineHearingUri(UUID onlineHearingId) {
        return CohUriBuilder.buildOnlineHearingGet(onlineHearingId);
    }

    private String getExpectedDecisionUri(UUID decisionId) {
        return CohUriBuilder.buildDecisionGet(decisionId);
    }

    private String getExpectedQuestionUri(UUID onlineHearingId, UUID questionId) {
        return CohUriBuilder.buildQuestionGet(onlineHearingId, questionId);
    }

    private String getExpectedAnswerUri(UUID onlineHearingId, UUID questionId, UUID answerId) {
        return CohUriBuilder.buildAnswerGet(onlineHearingId, questionId, answerId);
    }

    private OnlineHearingResponse getOnlineHearingResponse() throws Exception {
        String rawResponseString = testContext.getHttpContext().getRawResponseString();
        return JsonUtils.toObjectFromJson(rawResponseString, OnlineHearingResponse.class);
    }

    private String getExpectedDecisionReplyUri(UUID onlineHearingId, UUID decisionReplyId) {
        return CohUriBuilder.buildDecisionReplyGet(onlineHearingId, decisionReplyId);
    }

    private OnlineHearing createOnlineHearingFromResponse(CreateOnlineHearingResponse response) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(UUID.fromString(response.getOnlineHearingId()));

        return onlineHearing;
    }

    private String getOnlineHearingRequest(String method) throws  Exception {
        String json = "";

        if ("PUT".equalsIgnoreCase(method)) {
            json = getPutRequest();
        } else if ("POST".equalsIgnoreCase(method)) {
            json = JsonUtils.toJson(testContext.getScenarioContext().getCurrentOnlineHearingRequest());
        }

        return json;
    }
}