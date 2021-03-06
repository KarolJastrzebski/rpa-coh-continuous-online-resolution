package uk.gov.hmcts.reform.coh.schedule.notifiers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.auth.checker.core.service.ServiceRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;

import java.io.IOException;
import java.util.Objects;

@Component
@Qualifier("BasicJsonNotificationForwarder")
public class BasicJsonNotificationForwarder implements NotificationForwarder<NotificationRequest> {

    private static final Logger log = LoggerFactory.getLogger(BasicJsonNotificationForwarder.class);

    private static final String PLACEHOLDER_HOST = "${base-urls.test-url}";

    private static final ObjectMapper mapper = new ObjectMapper();

    private static HttpHeaders URL_ENCODED_HEADER;

    private static final String IDAM_SERVICE_TOKEN = "test_idam_service";

    static {
        URL_ENCODED_HEADER = new HttpHeaders();
        URL_ENCODED_HEADER.add("Content-Type", "application/json");
        URL_ENCODED_HEADER.add(UserRequestAuthorizer.AUTHORISATION, IDAM_SERVICE_TOKEN);
    }

    @Value("${base-urls.test-url}")
    private String baseUrl;

    private final AuthTokenGenerator authTokenGenerator;
    private final RestTemplateBuilder restTemplateBuilder;

    @Autowired
    public BasicJsonNotificationForwarder(
        AuthTokenGenerator authTokenGenerator,
        RestTemplateBuilder restTemplateBuilder
    ) {
        this.authTokenGenerator = Objects.requireNonNull(authTokenGenerator);
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Override
    public ResponseEntity sendEndpoint(SessionEventForwardingRegister register, NotificationRequest notificationRequest) throws NotificationException {

        generateS2SHeader();

        String endpoint = refactorEndpoint(register.getForwardingEndpoint());

        ResponseEntity response = null;
        try {
            log.info("Sending request to {}", endpoint);
            RestTemplate restTemplate = getRestTemplate();
            HttpEntity<String> request = new HttpEntity<>( mapper.writeValueAsString(notificationRequest), URL_ENCODED_HEADER);
            response = restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);
            log.info("Endpoint responded with {}", response.getStatusCodeValue());
        } catch (IOException ioe) {
            throw new NotificationException(ioe.getMessage());
        }  catch (HttpClientErrorException hcee) {
            throw new NotificationException("HTTP error. Endpoint responded with " + hcee.getRawStatusCode() + " and response body " + hcee.getResponseBodyAsString());
        } catch (RestClientException rce){
            throw new NotificationException(rce.getMessage());
        } catch (Exception e){
            throw new NotificationException(e.getMessage());
        }

        return response;
    }

    private void generateS2SHeader() {
        URL_ENCODED_HEADER.set(ServiceRequestAuthorizer.AUTHORISATION, authTokenGenerator.generate());
    }

    public String getLastServiceAuthorization() {
        return URL_ENCODED_HEADER.getFirst(ServiceRequestAuthorizer.AUTHORISATION);
    }

    public String refactorEndpoint(String endpoint) {
        if (endpoint.contains(PLACEHOLDER_HOST)) {
            endpoint = endpoint.replace("${base-urls.test-url}", getBaseUrl());
        }

        return endpoint;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public RestTemplate getRestTemplate() {
        return restTemplateBuilder.build();
    }
}