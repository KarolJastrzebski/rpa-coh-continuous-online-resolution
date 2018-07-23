package uk.gov.hmcts.reform.coh.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.exceptions.NotificationException;
import uk.gov.hmcts.reform.coh.domain.EventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.events.EventTypes;

@Component
public class Notifier {
    private static final Logger log = LoggerFactory.getLogger(Notifier.class);

    private RestTemplate restTemplate;

    public Notifier(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }
    public Notifier(){
        this.restTemplate = new RestTemplate();
    }

    public boolean notifyQuestionsIssued(EventForwardingRegister eventForwardingRegister, OnlineHearing onlineHearing) throws HttpClientErrorException, IllegalArgumentException{
        NotificationRequest notificationRequest = constructNotification(onlineHearing, EventTypes.QUESTION_ROUND_ISSUED);
        try {
            log.info("Notification request successful: " + notificationRequest.toString());
            ResponseEntity responseEntity = restTemplate.postForEntity(eventForwardingRegister.getForwardingEndpoint(), notificationRequest, NotificationRequest.class);
            if(responseEntity.getStatusCode().is2xxSuccessful()) {
                return true;
            }else{
                throw new NotificationException("Bad response from request: " + responseEntity.getStatusCodeValue());
            }
        }catch(HttpClientErrorException|IllegalArgumentException hcee){
            log.error("Notification request failed: " + notificationRequest.toString() + ":" + hcee);
            throw hcee;
        }
    }

    private NotificationRequest constructNotification(OnlineHearing onlineHearing, EventTypes eventType) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setCaseId(onlineHearing.getCaseId());
        notificationRequest.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        notificationRequest.setEventType(eventType.getStateName());

        log.info(notificationRequest.toString());
        return notificationRequest;
    }
}
