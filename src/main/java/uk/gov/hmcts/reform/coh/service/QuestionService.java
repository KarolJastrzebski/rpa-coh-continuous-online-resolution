package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.Notification.QuestionNotification;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;

@Service
@Component
public class QuestionService {

    private QuestionRepository questionRepository;
    private final QuestionStateService questionStateService;
    private QuestionNotification questionNotification;
    private OnlineHearingService onlineHearingService;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, QuestionStateService questionStateService, QuestionNotification questionNotification, OnlineHearingService onlineHearingService) {
        this.questionRepository = questionRepository;
        this.questionStateService = questionStateService;
        this.questionNotification = questionNotification;
        this.onlineHearingService = onlineHearingService;
    }

    public Question retrieveQuestionById(final Long question_id){
        return questionRepository.findById(question_id).orElse(null);
    }

    public Question createQuestion(final Question question, UUID onlineHearingId) {
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingId);

        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.retrieveOnlineHearing(onlineHearing);
        if(!optionalOnlineHearing.isPresent()){
            throw new EntityNotFoundException();
        }

        question.setOnlineHearing(optionalOnlineHearing.get());
        question.setQuestionState(questionStateService.retrieveQuestionStateById(QuestionState.DRAFTED));

        return questionRepository.save(question);
    }

    public Question editQuestion(Long questionId, Question body) {
        Question question = retrieveQuestionById(questionId);
        question.addState(questionStateService.retrieveQuestionStateById(QuestionState.ISSUED));
        return questionRepository.save(question);
    }

    public void deleteQuestion(Question question) {
        questionRepository.delete(question);
    }

    public Question updateQuestion(Question question){
        QuestionState questionState = question.getQuestionState();
        if(questionState.getQuestionStateId() != QuestionState.ISSUED){
            issueQuestion(question);
        }else{
            questionRepository.save(question);
        }
        return question;
    }

    protected void issueQuestion(Question question) {
        QuestionState issuedQuestionState = questionStateService.retrieveQuestionStateById(QuestionState.ISSUED);
        
        question.addState(issuedQuestionState);
        boolean result = questionNotification.notifyQuestionState(question);
        if (result){
            System.out.println("Successfully issued question round and sent notification to jurisdiction");
            questionRepository.save(question);
        }else{
            System.out.println("Error: Request to jurisdiction was unsuccessful");
        }
    }
}
