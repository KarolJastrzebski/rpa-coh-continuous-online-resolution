package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@Component
public class OnlineHearingService {

    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    public OnlineHearingService(OnlineHearingRepository onlineHearingRepository) {
        this.onlineHearingRepository = onlineHearingRepository;
    }

    public OnlineHearing createOnlineHearing(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.save(onlineHearing);
    }

    public Optional<OnlineHearing> retrieveOnlineHearing(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.findById(onlineHearing.getOnlineHearingId());
    }

    public OnlineHearing retrieveOnlineHearingByCaseId(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.findByCaseId(onlineHearing.getCaseId()).orElse(null);
    }

    public void deleteOnlineHearing(final OnlineHearing onlineHearing) {
        onlineHearingRepository.deleteById(onlineHearing.getOnlineHearingId());
    }

    public void deleteByCaseId(String caseId) {
        onlineHearingRepository.deleteByCaseId(caseId);
    }

    //public OnlineHearing updateOnlineHearingState(String caseId, OnlineHearing body) {
     //   OnlineHearing onlineHearing = retrieveOnlineHearingByCaseId(caseId);
    //}

//    public Question editQuestion(Long questionId, Question body) {
//        Question question = retrieveQuestionById(questionId);
//        question.addState(questionStateService.retrieveQuestionStateById(QuestionState.ISSUED));
//        return questionRepository.save(question);
//    }
}