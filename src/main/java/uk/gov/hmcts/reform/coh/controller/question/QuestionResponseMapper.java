package uk.gov.hmcts.reform.coh.controller.question;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.AnswerRepository;
import uk.gov.hmcts.reform.coh.repository.AnswerStateRepository;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.AnswerStateService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum QuestionResponseMapper {

    QUESTION_ID((Question q) -> { return q.getQuestionId().toString(); }, QuestionResponse::setQuestionId),
    QUESTION_ROUND((Question q) -> {return q.getQuestionRound().toString(); }, QuestionResponse::setQuestionRound),
    QUESTION_ORDINAL((Question q) -> {return Integer.toString(q.getQuestionOrdinal()); }, QuestionRequest::setQuestionOrdinal),
    QUESTION_HEADER_TEXT(Question::getQuestionHeaderText, QuestionRequest::setQuestionHeaderText),
    QUESTION_BODY_TEXT(Question::getQuestionText, QuestionRequest::setQuestionBodyText),
    OWNER_REFERENCE(Question::getOwnerReferenceId, QuestionRequest::setOwnerReference),
    QUESTION_STATE(q -> {return q.getQuestionState().getState();}, (qr, s) -> { qr.getCurrentState().setName(s);}),
    STATE_TIME(q -> {
        if (!q.getQuestionStateHistories().isEmpty()){
            return q.getQuestionStateHistories().get(q.getQuestionStateHistories().size()-1).getDateOccurred().toString();
        }
        return null;
        }, (qr, s) -> { qr.getCurrentState().setDatetime(s);});

    private Function<Question, String> getter;
    private BiConsumer<QuestionResponse, String> setter;


    QuestionResponseMapper (Function<Question, String> getter, BiConsumer<QuestionResponse, String> setter){
        this.getter = getter;
        this.setter = setter;
    }

    public static void map(Question question, QuestionResponse response) {
        for (QuestionResponseMapper m : QuestionResponseMapper.class.getEnumConstants()) {
            m.set(question, response);
        }
        if (question.getDeadlineExpiryDate() != null) {
            response.setDeadlineExpiryDate(question.getDeadlineExpiryDate());
        }
    }

    public static void map(Question question, QuestionResponse response, Answer answer) {
        for (QuestionResponseMapper m : QuestionResponseMapper.class.getEnumConstants()) {
            m.set(question, response);
        }
        if(question.getDeadlineExpiryDate()!=null) {
            response.setDeadlineExpiryDate(question.getDeadlineExpiryDate());
        }
        response.setAnswer(answer);
    }

    public void set(Question question, QuestionResponse questionResponse) {
        setter.accept(questionResponse, getter.apply(question));
    }

}
